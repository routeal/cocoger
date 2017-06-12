'use strict';

require('./app/utils/string.extension');

var init = require('./config/init')(),
    config = require('./config/config'),
    mongoose = require('mongoose'),
    chalk = require('chalk'),
    ObjectId = require('mongoose').Types.ObjectId,
    request = require('request'),
    geocoder = require('geocoder'),
    moment = require('moment-timezone'),
    path = require('path');

var db = mongoose.connect(config.db.address, function (err) {
	if (err) {
		console.error(chalk.red('Could not connect to MongoDB!'));
		console.log(chalk.red(err));
	}
});

config.getGlobbedFiles('./app/models/**/*.js').forEach(function(modelPath) {
	require(path.resolve(modelPath));
});

var User = mongoose.model('User');


var args = process.argv.slice(2);

if (args.length === 0) {
	console.error('minoru\s id is missing');
	process.exit(0);
}

/*******************************************************************************/

var MinoruID = args[0];

// create the TestUserCount number of users every this interval
var CreationInterval = 1000 * 30;

// create the TestUserCount number of users every this interval
var TestMainInterval = 1000 * 60 * 60;

var TestUserCount = 30;

var JapanLocationRange = [3, 6, 9, 15, 18];


/*******************************************************************************/

function random (low, high) {
	return Math.floor(Math.random() * (high - low) + low);
}

var createTestUser = function(i, next) {
	var parameters = {
		"email" : "nabe"+i+"@live.com",
		"password": "nabe1234",
		"name": "nabe"+i,
		"bod": random(1950, 2015),
		"gender": random(0, 2)
	};

	var device_parameters = {
		"device"	  : "iPhone5,3",
		"platform"	  : "node",
		"platformVersion" : "8.0",
		"lang"            : "ja",
		"country"         : "JP",
		"isSimulator"     : true,
	};

	parameters["device_content"] = device_parameters

	console.log('%j', parameters);

	var options = {
		url: "http://cocoger.com:3000/m/auth/signup",
		method: "POST",
		json: parameters,
		headers: {
			"content-type": "application/json",
		},
	};

	function callback(error, response, body) {
		if (!error && response.statusCode == 200) {
			var user = JSON.parse(JSON.stringify(body));
			//console.log(user);

			var parameters = {};
			parameters.user = MinoruID;
			parameters.range = JapanLocationRange[random(0, 5)];
			parameters.message = "from machine";

			console.log(parameters);

			var options = {
				url: "http://cocoger.com:3000/m/friends/invite",
				method: "POST",
				body: JSON.stringify(parameters),
				headers: {
					"content-type": "application/json",
					"Authorization": "JWT " + user.authToken
				},
			};

			console.log("nabe being invited by: %s", user.name);

			request(options, next);
		} else {
			console.log('signup failed: %s', body.message);

			next();
		}
	}

	request(options, callback);
};

var createTestUsers = function(done) {

	var numUsers = TestUserCount;
	var counter = numUsers;

	var next = function(err) {
		if (err) {
			console.log(err);
		}
		if (--counter == 0) {
			done();
		}
	};

	for (var i = 0; i < numUsers; i++) {
		createTestUser(i+10, next);
	}
};

/**
 * Run the test every the interval.  On each interval, scan the
 * database to find the simulator users and login to the server, then
 * move their locations, and update the database.
 */

var testMain = function(done) {
	User.find().populate('device').exec(function(err, users) {
		if (err) {
			return;
		}

		function createTestUser(user) {
			console.log("Creating a test user for %s...", user.name)
			new TestUser(user).signin(function(err, testuser) {
				if (err || !testuser) {
					console.error("%s failed to sign in", user.name)
				} else {
					testuser.move();
				}
			});
		}

		var i = 1;
		users.forEach(function(user) {
			if (user.device) {
				if (user.device.isSimulator && user.device.platform == 'node') {
					setTimeout(function() {
						createTestUser(user);
					}, CreationInterval * i++);
				}
			}
		});
	});
};

createTestUsers(function() {
	//schedule the repeated execution of callback every delay milliseconds
	console.log('start testing...');
	setInterval(testMain, TestMainInterval);
	testMain();
});


var TestUser = function(user) {
	this.user = user;
// tokyo
/*
	this.lat = 35.691006;
	this.lng = 139.700181;
*/
// kobe
	this.lat = 34.69;
	this.lng = 135.1956;

	this.radius = 5000;
	// should be received from the server upon the login success
	this.authToken = '';

	geocoder.selectProvider('google', {key:'AIzaSyBvDnrmxGLfRagCILf2FmYOdnl0ArJM6QA'});
}

TestUser.prototype.signin = function (done) {
	var parameters = {
		"email" : this.user.email,
		"password": "nabe1234"
	};

	var options = {
		url: "http://cocoger.com:3000/m/auth/signin",
		method: "POST",
		json: parameters,
		headers: {
			"content-type": "application/json",
		},
	};

	var self = this;

	function callback(error, response, body) {
		if (!error && response.statusCode == 200) {
			var obj = JSON.parse(JSON.stringify(body));
			self.authToken = obj.authToken;
			done(null, self);
		} else {
			done(error, null);
		}
	}

	request(options, callback);
}

TestUser.prototype.loop = function() {
	setInterval(this.move.bind(this), TestMainInterval);
	this.move();
};

TestUser.prototype.move = function() {
	// move to the position where is randomly calculated within the radius
	var r = this.radius/111300
	, y0 = this.lat
	, x0 = this.lng
	, u = Math.random()
	, v = Math.random()
	, w = r * Math.sqrt(u)
	, t = 2 * Math.PI * v
	, x = w * Math.cos(t)
	, y1 = w * Math.sin(t)
	, x1 = x / Math.cos(y0);

	var newLat = y0 + y1; // latitude
	var newLng = x0 + x1; // longitude

	console.log("latitude="+newLat+" longitude="+newLng);

	var self = this;

	geocoder.reverseGeocode(newLat, newLng, function (err, data) {
		if (err || data.status !== 'OK') {
			console.log('reverseGeocode failed: %s', err);
			return
		}

		//console.log("%j", data);

		// NOTE: looks like the first one is the most detail
		var result = data.results[0];
		if (result.types.indexOf('sublocality') < 0) {
			console.error('only type of sublocality supported: %s', result.types)
			return;
		}

		var sub5 = '';
		var sub4 = '';
		var sub3 = '';
		var sub2 = '';
		var sub1 = '';
		var ward = '';
		var zip = '';
		var country = '';
		var state = '';
		var county = '';
		var city = '';
		var town = '';
		var street = '';
		var code = '';

		var components = result.address_components;
		for (var addr in components) {
			console.log(components[addr]);
			if (components[addr].types.indexOf('sublocality_level_5') >= 0) {
				sub5 = components[addr].long_name;
			}
			else if (components[addr].types.indexOf('sublocality_level_4') >= 0) {
				sub4 = components[addr].long_name;
			}
			else if (components[addr].types.indexOf('sublocality_level_3') >= 0) {
				sub3 = components[addr].long_name;
			}
			else if (components[addr].types.indexOf('sublocality_level_2') >= 0) {
				sub2 = components[addr].long_name;
			}
			else if (components[addr].types.indexOf('sublocality_level_1') >= 0) {
				sub1 = components[addr].long_name;
			}
			else if (components[addr].types.indexOf('ward') >= 0) {
				ward = components[addr].long_name;
			}
			else if (components[addr].types.indexOf('locality') >= 0) {
				city = components[addr].long_name;
			}
			else if (components[addr].types.indexOf('administrative_area_level_1') >= 0) {
				state = components[addr].long_name;
			}
			else if (components[addr].types.indexOf('country') >= 0) {
				country = components[addr].long_name;
				code = components[addr].short_name;
			}
			else if (components[addr].types.indexOf('postal_code') >= 0) {
				zip = components[addr].long_name;
			}
		}

		if (code == "JP") {
			if (sub5) {
				street = sub5;
			}
			if (sub4) {
				if (street) {
					street = sub4 + '-' + street;
				} else {
					street = sub4;
				}
			}
			if (sub3) {
				if (street) {
					street = sub3 + '-' + street;
				} else {
					street = sub3;
				}
			}
			if (sub2) {
				town = sub1 + sub2;
			} else {
				town = sub1;
			}
			if (ward) {
				city = city + ward;
			} else {
				city = city;
			}
		} else if (code == "US") {
			if (sub3) {
				street = sub3 + '-' + sub4;
			} else {
				street = sub4;
			}
			if (sub2) {
				town = sub1 + ' ' + sub2;
			} else {
				town = sub1;
			}
			if (ward) {
				city = ward + ' ' + city;
			} else {
				city = city;
			}
		}

		var location = {};
		location.latitude = newLat;
		location.longitude = newLng;
		location.user = self.user.id;
		location.zip = zip;
		location.country = country.latinize();
		location.state = state.latinize();
		location.county = county.latinize();
		location.city = city.latinize();
		location.town = town.latinize();
		location.street = street.latinize();
		location.created = moment.utc().valueOf();

		var parameters = {};
		parameters.locations = [location];

		//console.log(self.authToken);
		console.log(JSON.stringify(parameters));

		var options = {
			url: "http://cocoger.com:3000/m/locations",
			method: "POST",
			body: JSON.stringify(parameters),
			headers: {
				"content-type": "application/json",
				"Authorization": "JWT " + self.authToken
			},
		};

		function callback(error, response, body) {
			if (!error && response.statusCode == 200) {
				console.log("location successfully uploaded");
			} else {
				console.log("error:%s", error);
			}
		}

		request(options, callback);
	}, {"language": "ja"});

}

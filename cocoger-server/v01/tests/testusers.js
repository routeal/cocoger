require('../models/user.server.model');
require('../models/location.server.model');
require('../utils/string.extension');

var mongoose = require('mongoose'),
    ObjectId = require('mongoose').Types.ObjectId;
var geocoder = require('geocoder');
var User     = mongoose.model('User');
var Location = mongoose.model('Location');

var standalone = true;

// center location of the address in string

var TestUser = function(user_id, lat, lng, range, interval) {
    this.user_id = user_id;
    this.lat = lat;
    this.lng = lng;
    this.radius = range;
    this.interval = interval;
};

TestUser.prototype.start = function() {
    setInterval(this.moveUsers.bind(this), this.interval);
    // initial move
    this.moveUsers();
};

var Move = function(){
return {
    'STREET'	: 0,
    'TOWN'	: 1,
    'CITY'	: 2,
    'COUNTY'	: 3,
    'STATE'	: 4,
    'COUNTRY'	: 5,
    'NONE'	: 6,
    }
}();

var checkMovement = function(c /*current_location*/, p /*previous_location*/, m /* moved */) {
    var move = Move.NONE;
    if (c.country !== p.country) {
	move = Move.COUNTRY;
	//console.log('Move.COUNTRY');
    } else if (c.state !== p.state) {
	move = Move.STATE;
	//console.log('Move.STATE');
    } else if (c.county !== p.county) {
	move = Move.COUNTY;
	//console.log('Move.COUNTY');
    } else if (c.city !== p.city) {
	move = Move.CITY;
	//console.log('Move.CITY');
    } else if (c.street !== p.street) {
	move = Move.STREET;
	//console.log('Move.STREET');
    }
    process.nextTick(function() {
	m(move);
    });
}

var notifyMovement = function(user, move, complete) {
    //console.log(user.contacts);
    var completed = 0;
    var notified = 0;
    var errored = false;

    function done(err, friend) {
	if (err) {
	    console.error("User unable to be found: %s", err);
	    errored = true;

	// NOTE: TESTING
	} else if (friend.name == 'nabe') {
	    console.log('notify from ' + user.name + ' to ' + friend.name + ' for movement of ' + move);
	}

	if (++completed == notified) {
	    console.log("ayasii");
	    complete(errored ? err : null);
	}
    }

    for (var i = 0; i < user.contacts.length; i++) {
	var allowed = user.contacts[i].allowed;
	if (allowed <= move) {
	    notified++;
	}
    }

    if (notified === 0) {
	complete(null);
	return;
    }

    user.contacts.forEach(function(contact) {
	var allowed = contact.allowed;
	var id = contact.user;

	if (allowed <= move) {
	    User.findById(new ObjectId(id), done);
	}
    });

}

TestUser.prototype.moveUsers = function() {
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

    //console.log("latitude="+newLat+" longitude="+newLng)

    var self = this;

    // get the information of the new location
    geocoder.reverseGeocode(newLat, newLng, function ( err, data ) {
	if (err || data.status !== 'OK') {
	    console.log('reverseGeocode failed: %s', err);
	    return
	}

	// TODO: looks like the first one is the most detail
	var result = data.results[0];
	if (result.types.indexOf('sublocality') < 0) {
	    console.error('only type of sublocality supported: %s', result.types)
	    return;
	}

	var zip = '';
	var country = '';
	var state = '';
	var county = '';
	var city = '';
	var town = '';
	var street = '';

	var address_component;

	for (address_component in result.address_components) {
	    //console.log(result.address_components[address_component]);
	    if (result.address_components[address_component].types.indexOf('sublocality_level_1') >= 0) {
		street = result.address_components[address_component].long_name;
		//console.log('street:' + street.latinize());
	    }
	    else if (result.address_components[address_component].types.indexOf('ward') >= 0) {
		// FIXME:
		town = result.address_components[address_component].long_name;
		//console.log('town:' + town.latinize().strip());
	    }
	    else if (result.address_components[address_component].types.indexOf('locality') >= 0) {
		city = result.address_components[address_component].long_name;
		//console.log('city:' + city.latinize().strip());
	    }
	    else if (result.address_components[address_component].types.indexOf('administrative_area_level_1') >= 0) {
		state = result.address_components[address_component].long_name;
		//console.log('state:' + state.latinize().strip());
	    }
	    else if (result.address_components[address_component].types.indexOf('country') >= 0) {
		country = result.address_components[address_component].long_name;
		//console.log('country:' + country.latinize().strip());
	    }
	}

	var location = new Location({
	    coordinates	: [newLng, newLat],
	    user	: self.user_id,
	    accuracy    : 65,
	    zip		: zip.latinize().strip(),
	    country	: country.latinize().strip(),
	    state	: state.latinize().strip(),
	    county	: county.latinize().strip(),
	    city	: city.latinize().strip(),
	    town	: town.latinize().strip(),
	    street	: street.latinize().strip()
	});

	// get the user object
	User.findById(new ObjectId(self.user_id), function(err, user) {
	    if (err) {
		console.error("User unable to be found: %s", err);
		return
	    }

	    // save the new location
	    location.save(function(err) {
		if (err) {
		    console.error("Location unable to be saved: %s", err);
		    return
		}

		if (user.location === undefined) {
                    //console.info('FIRST TIME: NO LOCATION IN DB');
		} else {
                    //console.log('LAST LOCATION IN DB');

		    // get the last location to see the user moved
		    Location.findById(new ObjectId(user.location), function(err, lastLocation) {
			if (err) {
			    console.error("Unable to find a location from the database: %s", err);
			    return;
			}

			// compare the two locations
			checkMovement(location, lastLocation, function(move) {
			    if (move != Move.NONE) {
				notifyMovement(user, move, function(err) {
				    if (err) {
					console.log("notify error: %s", err)
				    }
				});
			    } else {
				console.log("%s: no movement detected", user.name)
			    }
			});
		    });
		}

		user.location = location._id;
		user.save(function(err) {
		    if (err) console.error("Unable to save a location to the database: %s", err);
		});
	    });

	});
    });
}

var TestUserManager = function(size, lat, lng, range, interval) {
    this.size = size;
    this.users = [];
    this.lat = lat;
    this.lng = lng;
    this.range = range;
    this.interval = interval;
}

module.exports = TestUserManager;

TestUserManager.prototype.start = function() {
    var self = this;
    var completed = 0;
    var errored = false;

    // 5. start the test users
    function real_start(err) {
	if (err) {
	    errored = true;
	    console.log(err);
	}
	if (++completed == (self.size + 1) && !errored) {
	    console.log('test user starting...');

	    self.users.forEach(function(tuser) {
		tuser.start();
	    });
	}
    }

    // 4. relate the contacts to all the users
    function nabe_loaded(nabe) {
	console.log("nabe loaded");

	completed = 0;
	errored = false;

	User.find({ roles: 'test' }, function(err, users) {
	    for (var i = 0; i < users.length; i++) {
		var name = 'test' + i;

		if (users[i].name == name) {
		    for (var j = 0; j < i; j++) {
			//var name1 = 'test' + j;
			//console.log("S "+name+"    "+name1);
			users[i].contacts.push({allowed: Move.STREET, user: users[j]._id});
		    }
		    for (var j = i+1; j < users.length; j++) {
			//var name2 = 'test' + j;
			//console.log("P "+name+"    "+name2);
			users[i].contacts.push({allowed: Move.STREET, user: users[j]._id});
		    }
		}

		users[i].contacts.push({allowed: Move.STREET, user: nabe._id});

		users[i].save(real_start);

		nabe.contacts.push({allowed: Move.STREET, user: users[i]._id});
	    }

	    nabe.password = "nabe1234";
	    nabe.save(real_start);
	});
    }

    // 3. load 'nabe'
    function create_complete() {
	console.log("created new test users");

	User.findOne({email: 'nabe@live.com'}, function(err, nabe) {
	    if (err) {
		console.log("nabe not found: %s", err);
	    } else if (nabe) {
		nabe_loaded(nabe);
	    }
	});
    }

    // 2. create the new test users
    function remove_complete(err) {
	if (err) {
	    console.log("failed to reset nabe's contacts: %s", err);
	    return;
	}

	console.log("reset nabe's contacts");

	completed = 0;
	errored = false;

	// add the test users
	for (var i = 0; i < self.size; i++) {
	    var name = 'test' + i;
	    var email = name + '@live.com';

	    // create a new user and save it to the db
	    var user  = new User({name: name, email: email, password: 'test1234', provider: 'local', roles: ['test']});
	    user.save(function(err, user) {
		if (err) {
		    console.log(err);
		    errored = true;
		} else {
		    var tuser = new TestUser(user._id, self.lat, self.lng, self.range, self.interval);
		    self.users.push(tuser);
		}
		if (++completed == self.size && !errored) {
		    create_complete();
		}
	    });
	}
    }

    // 1.2 reset nabe's contracts
    function reset_nabe_contacts() {
	console.log("removed all the test users")

	User.findOne({name: 'nabe'}, function(err, nabe) {
	    if (err) {
		console.log("nabe not found:%s", err);
	    } else if (nabe) {
		console.log("reset nabe's contacts")
		nabe.contacts = [];
		// TODO: don't know how to avoid
		nabe.password = "nabe1234";
		nabe.save(remove_complete);
	    }
	});
    }

    // 1. clean up all the test users from the db
    User.find({ 'roles': 'test' }).remove().exec(reset_nabe_contacts);
}

if (standalone) {

    mongoose.connect('mongodb://localhost/sokoranow-dev', function(err) {
	if (err) {
	    console.error('Could not connect to MongoDB: %s', err);
	}
    });

    var interval = 1000 * 60 * 30;
    var mgr = new TestUserManager(5, 34.69, 135.1956, 5000, interval);
    mgr.start();

}

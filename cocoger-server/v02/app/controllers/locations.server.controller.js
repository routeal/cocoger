'use strict';

/**
 * Module dependencies.
 */
var mongoose = require('mongoose'),
    geolib = require('geolib'),
    errorHandler = require('./errors.server.controller'),
    moment = require('moment-timezone'),
    Location = mongoose.model('Location'),
    User = mongoose.model('User'),
    Friend = mongoose.model('Friend'),
    ObjectId = require('mongoose').Types.ObjectId,
    config = require('../../config/config'),
    apn = require('../utils/apn.wrapper'),
    logger = require('winston'),
    _ = require('lodash');

var getLastLocation = function(id, done) {
  if (typeof id !== 'object') {
    id = new ObjectId(id);
  }

  Location.findOne({user: id}).sort({created: -1}).exec(function(err, lastLocation) {
    done(err, lastLocation);
  });
};

var saveLocation = function(s, p, done) {
  if (p) {
    console.log(p);
    /*
      var distance = geolib.getDistance(
      {latitude: p.latitude, longitude: p.longitude},
      {latitude: s.latitude, longitude: s.longitude}
      );
      console.log(distance);
      //logger.info('too short to save the movement: %f', distance);
      if (distance < 250) {
      logger.info('too short to save the movement: %d', distance);
      done(null, 0);
      return;
      }
    */
  }

  var location = new Location({
    coordinates	: [s.longitude,s.latitude],
    accuracy	: s.accuracy,
    altitude	: s.altitude,
    city		: s.city,
    county		: s.county,
    country		: s.country,
    speed		: s.speed,
    state		: s.state,
    street		: s.street,
    town		: s.town,
    type		: s.type,
    timezone	: s.timezone,
    user		: s.user,
    zip		: s.zip,
    created		: s.created,
  });

  location.save(function(err, location) {
    done(err, location);
  });
};

var detectMovement = function(c /*current_location*/, p /*previous_location*/, m /* moved */) {
  var move = config.LocationRange.none;
  if (c.country !== p.country) {
    logger.info('move: %s', 'country');
    move = config.LocationRange.country;
  } else if (c.state !== p.state) {
    logger.info('move: %s', 'state');
    move = config.LocationRange.state;
  } else if (c.county !== p.county) {
    logger.info('move: %s', 'county');
    move = config.LocationRange.county;
  } else if (c.city !== p.city) {
    logger.info('move: %s', 'city');
    move = config.LocationRange.city;
  } else if (c.town !== p.town) {
    logger.info('move: %s', 'town');
    move = config.LocationRange.town;
  } else /* if (c.street !== p.street) */ {
    logger.info('move: %s', 'street');
    move = config.LocationRange.street;
  }
  m(move);
};

var notifyMovement = function(user, location, move, done) {
  var arg = {owner: user.id, approved: true};

  // list the all friends and compare their location range with
  // the current movement
  Friend.find(arg, function(err, friends) {
    if (err || !friends) {
      if (err) logger.info(errorHandler.getErrorMessage(err));
      // no friend is fine
      done(null);
      return;
    }

    friends.forEach(function(friend) {

      // those who have the range less than the move
      // or the eual as the move are interested in
      // the move
      if (friend.range > move) {
	return;
      }

      getLastLocation(friend.user, function(err, friendLocation) {
	if (err || !friendLocation) {
	  if (err) logger.info(errorHandler.getErrorMessage(err));
	  return;
	}

	var place = null;

	if (move === config.LocationRange.country) {
	  if (location.country === friendLocation.country) {
	    place = location.country;
	  }
	} else if (move === config.LocationRange.state) {
	  if (location.state === friendLocation.state) {
	    place = location.state;
	  }
	} else if (move === config.LocationRange.county) {
	  logger.info('notifyMovement: county: me:%s, friend:%s', location.county, friendLocation.county);
	  if (location.county === friendLocation.county) {
	    place = location.county;
	  }
	} else if (move === config.LocationRange.city) {
	  logger.info('notifyMovement: city: me:%s, friend:%s', location.city, friendLocation.city);
	  if (location.state === friendLocation.state && location.city === friendLocation.city) {
	    place = location.city;
	  }
	} else if (move === config.LocationRange.town) {
	  logger.info('notifyMovement: town: me:%s, friend:%s', location.town, friendLocation.town);
	  if (location.city === friendLocation.city && location.town === friendLocation.town) {
	    place = location.town;
	  }
	} else if (move === config.LocationRange.street) {
	  logger.info('notifyMovement: street');
	}

	User.findById(friend.user).populate('device').exec(function(err, friend) {
	  if (err || !friend) {
	    if (err) logger.info(errorHandler.getErrorMessage(err));
	  } else {
	    logger.info('APN: user %s moved to %s %s', user.name, location.town, location.street);
	    if (typeof friend.device === 'undefined') {
	      logger.info('device not defined');
	    } else if (friend.device.deviceToken) {
	      if (place) {
		apn.sendMovedIn(friend.device.deviceToken, user.id, user.name, place);
	      } else {
		apn.sendMove(friend.device.deviceToken, user.id);
	      }
	    } else {
	      logger.info('user move not notified to the friend due to unavailable deviceToken');
	    }
	  }
	});
      });
    });

    done(null);
  });
};

/**
 * Create a Location
 */
exports.create = function(req, res) {
  var user = req.user;
  var locations = req.body.locations;

  if (locations === undefined || locations.length === 0) {
    res.status(400).send({message: 'location not found'});
    return;
  }

  getLastLocation(user._id, function(err, lastLocation) {
    if (err) {
      logger.inf('can\'t get the last location: %s', user.name);
      res.status(500).send({message: errorHandler.getErrorMessage(err)});
      return;
    }

    var completed = 0;
    var errors = [];

    function saved(err, location) {
      if (err) {
	errors.push(err);
	return;
      }

      completed++;

      if (completed < locations.length) {
	return;
      }

      if (errors.length === completed) {
	logger.inf('failed to save the locations: %s', user.name);
	res.status(500).send({message: errorHandler.getErrorMessage(errors[0])});
	return;
      }

      res.json({});
      //res.status(200).send();

      if (lastLocation && location) {
	detectMovement(location, lastLocation, function(move) {
	  if (move !== config.LocationRange.none) {
	    notifyMovement(user, location, move, function(err) {
	      if (err) {
		logger.info('error during notify movement: %s',
			    errorHandler.getErrorMessage(err));
	      }
	    });
	  } else {
	    logger.info('user not moved');
	  }
	});
      } else {
	logger.info('first time, location is saved');
      }
    }

    var prev = lastLocation;
    locations.forEach(function(location) {
      saveLocation(location, prev, saved);
      prev = location;
    });
  });
};

// get the latest location - last known location
exports.latest = function(req, res) {
  var userid;

  if (req.query.user) {
    userid = new ObjectId(req.query.user);
  } else {
    userid = req.user._id;
  }

  function done() {
    getLastLocation(userid, function(err, lastLocation) {
      if (err) {
	res.status(404).send({message: errorHandler.getErrorMessage(err)});
      } else {
	if (lastLocation) {
	  res.json(lastLocation);
	} else {
	  logger.info('location not found');
	  res.status(404).send({message: 'location not found'});
	}
      }
    });
  }

  if (userid === req.user._id) {
    done();
  } else {
    // return an error when the user is not login
    User.findById(userid).populate('device').exec(function(err, user) {
      if (err) {
	res.status(404).send({message: errorHandler.getErrorMessage(err)});
      } else {
	if (typeof user.device !== 'undefined') {
	  if (user.device.deviceToken || user.device.isSimulator) {
	    done();
	  }
	} else {
	  logger.info('User not available: %s', user.name);
	  res.status(404).send({message: 'User not available'});
	}
      }
    });
  }
};

var listLocation = function(user, timestamp, timezone, callback) {
  if (!timestamp) {
    timestamp = moment.utc();
  }
  if (!timezone) {
    timezone = config.defaultTimezone;
  }

  var localtime = timestamp.clone().tz(timezone);
  var start = localtime.clone().startOf('day');
  var end = localtime.clone().endOf('day');

  Location.find({user: user}).where('created').gt(start.toDate()).lt(end.toDate()).exec(function(err, locations) {
    if (err) {
      callback(err);
    } else {
      callback(null, locations);
    }
  });
};

exports.list = function(req, res) {
  var timestamp;
  if (typeof req.query.timestamp !== 'undefined') {
    timestamp = moment.unix(req.query.timestamp);
  } else {
    timestamp = moment.utc();
  }
  listLocation(req.user, timestamp, req.query.timezone, function(err, locations) {
    if (err) {
      res.status(400).send({message: errorHandler.getErrorMessage(err)});
    } else {
      res.json(locations);
    }
  });
};

exports.read = function(req, res) {
  listLocation(new ObjectId(req.friendId), null, null, function(err, locations) {
    if (err) {
      res.status(400).send({message: errorHandler.getErrorMessage(err)});
    } else {
      res.json(locations);
    }
  });
};

exports.byFriendID = function(req, res, next, id) {
  req.friendId = id;
  next();
};

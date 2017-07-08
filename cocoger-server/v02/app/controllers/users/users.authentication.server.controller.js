'use strict';

/**
 * Module dependencies.
 */
var _ = require('lodash'),
    config = require('../../../config/config'),
    jwt = require('jsonwebtoken'),
    errorHandler = require('../errors.server.controller'),
    friends = require('../friends.server.controller'),
    mongoose = require('mongoose'),
    passport = require('passport'),
    Device = mongoose.model('Device'),
    User = mongoose.model('User'),
    agenda = require('../../../config/agenda'),
    Friend = mongoose.model('Friend'),
    apn = require('../../utils/apn.wrapper'),
    ObjectId = require('mongoose').Types.ObjectId,
    logger = require('winston'),
    shortid = require('shortid'),
    async = require('async');

/**
 * For testing only
 */
exports.test = function(req, res) {
  res.json({"message":"Hello World!"});
};

/**
 * Signup - creates the user only, no session, no token
 *
 * {
 *  "email":"a@b.com",
 *  "password":"this is a passowrd",
 *  "name":"tako ika",
 *  "bod":1990,
 *  "gender":0
 * }
 *
 * Response:  returns a whole User data
 */
exports.signup = function(req, res) {
  // Init Variables
  var user = new User(req.body);

  // Add missing user fields
  user.provider = 'local';

  console.log(req.body);

  // Then save the user
  user.save(function(err, user) {
    if (err) {
      res.status(400).send({message: errorHandler.getErrorMessage(err)});
    } else {
      res.json(user.toJSON());
    }
  });
};

/**
 * Login - password authentication, token(jwt) generation
 *
 * {
 *  "email":"a@b.com",
 *  "password":"this is a passowrd",
 *  "device": {
 *   "id": "xxx", // device unque id
 *   "type": mobile/desktop/settop
 *   "brand":
 *   "model":
 *   "version":
 *   "platform": ios/android/windows/linux
 *   "platformVersion": 10/6.0/10
 *   "country":"",
 *   "lang":"",
 *   "simulator":false
 *  }
 * }
 *
 * Authenticated with email and password with the local passport
 * strategy.  After that, the device information needs to be created
 * or updated if it already exists.  One user can have multiple devices.
 *
 * Since there are multiple devices, the User object can have an array
 * of devices.
 *
 * A Json Web Token will be returned with:
 * {
 *  user: user db id
 *  id: device.id - unique id
 *  login: login timestamp
 * }
 */
exports.login = function(req, res, next) {
  // autheticated with email and password
  passport.authenticate('local', {session: false}, function(err, user, info) {
    if (err || !user || !req.body.device || !req.body.device.id) {
      res.status(404).send({message: info});
    } else {
      // search a device by id
      var index = _.findIndex(user.devices, {id: req.body.device.id});
      // if not found, add a first one
      if (index < 0) {
        index = 0;
        user.devices.push(req.body.device);
      } else {
        // merge the new value
        _.extend(user.devices[index], req.body.device);
      }
      /* allow multiple devices
      for (var i = 0; i < user.devices.length; i++) {
        user.devices[index].login = 0;
      }
      */
      user.devices[index].login = Date.now();
      // save to the db
      user.save(function(err, user) {
	if (err) {
	  res.status(400).send({message: errorHandler.getErrorMessage(err)});
	  return;
	}
        var json = user.toJSON();
        var device = user.devices[index];
        var jwt_payload = {user:user._id, id:device.id, login:device.login};
        json.authToken = jwt.sign(jwt_payload, config.jwtSecret);
        res.json(json);
      });
    }
  })(req, res, next);
};

exports.facebook_login = function(req, res) {
  console.log(req);
  console.log(req.body);
  console.log(req.user);
  res.json(req.user || null);
};


/**
 * Logout from the login device
 */
exports.logout = function(req, res) {
  User.findById(req.user._id, function(err, user) {
    if (err || !user) {
      res.status(400).send({message: errorHandler.getErrorMessage(err)});
    } else {
      var index = _.findIndex(user.devices, {id: req.user.device.id});
      if (index < 0) {
        res.status(400).send({message: 'device not found'});
        return;
      } else {
        user.devices[index].login = 0;
      }
      user.save(function(err, user) {
	if (err) {
	  res.status(400).send({message: errorHandler.getErrorMessage(err)});
          return;
        }
        res.json();
      });
    }
  });
};

/**
 * OAuth callback used for Web login
 */
exports.oauthCallback = function(strategy) {
  return function(req, res, next) {
    passport.authenticate(strategy, function(err, user, redirectURL) {
      if (err || !user) {
	return res.redirect('/#!/signin');
      }
      req.login(user, function(err) {
	if (err) {
	  return res.redirect('/#!/signin');
	}
	return res.redirect(redirectURL || '/');
      });
    })(req, res, next);
  };
};

/**
 * Mobile OAuth with the provider's token
 */
exports.oauthToken = function(strategy) {
  return function(req, res, next) {
    passport.authenticate(strategy, {session:false}, function(err, user) {
      if (err || !user) {
	res.status(400).send({message: errorHandler.getErrorMessage(err)});
      } else {
	Device.findOne({'owner': user._id}, function(err, device) {
	  if (err) {
	    res.status(400).send({message: errorHandler.getErrorMessage(err)});
	    return;
	  }
	  if (!device) {
	    device = new Device(req.body.device_content);
	    device.owner = user._id;
	  } else {
	    device = _.extend(device, req.body.device_content);
	  }

	  device.save(function(err, device) {
	    user.device = device._id;
	    user.updated = Date.now();
	    user.save(function(err) {
	      if (err) {
		res.status(400).send({message: errorHandler.getErrorMessage(err)});
	      } else {
		req.login(user, function(err) {
		  if (err) {
		    res.status(400).send({message: errorHandler.getErrorMessage(err)});
		  } else {
		    var json = user.toJSON();
		    json.authToken = jwt.sign(user, config.jwtSecret);
		    json.locationRange = config.getLocationRange(device.country);
		    //res.status(200).send(json);
		    res.json(json);
		    // reschedule the agenda jobs
		    friends.rescheduleJobs(user);

		    // notify user not available
		    var arg = {owner: user._id, approved: true};
		    Friend.find(arg, function(err, friends) {
		      if (err) {
			res.status(400).send({message: errorHandler.getErrorMessage(err)});
		      } else {
			friends.forEach(function(friend) {
			  User.findById(friend.user).populate('device').exec(function(err, fuser) {
			    if (fuser.device.deviceToken) {
			      apn.sendFriendIn(fuser.device.deviceToken, user.id);
			    }
			  });
			});
		      }
		    });

		  }
		});
	      }
	    });
	  });
	});
      }
    })(req, res, next);
  };
};

/**
 * Helper function to save or update a OAuth user profile
 */
exports.saveOAuthUserProfile = function(req, providerUserProfile, done) {
  if (!req || !req.user) {
    // Define a search query fields
    var searchMainProviderIdentifierField = 'providerData.' + providerUserProfile.providerIdentifierField;
    var searchAdditionalProviderIdentifierField = 'additionalProvidersData.' + providerUserProfile.provider + '.' + providerUserProfile.providerIdentifierField;

    // Define main provider search query
    var mainProviderSearchQuery = {};
    mainProviderSearchQuery.provider = providerUserProfile.provider;
    mainProviderSearchQuery[searchMainProviderIdentifierField] = providerUserProfile.providerData[providerUserProfile.providerIdentifierField];

    // Define additional provider search query
    var additionalProviderSearchQuery = {};
    additionalProviderSearchQuery[searchAdditionalProviderIdentifierField] = providerUserProfile.providerData[providerUserProfile.providerIdentifierField];

    // Define a search query to find existing user with current provider profile
    var searchQuery = {
      $or: [mainProviderSearchQuery, additionalProviderSearchQuery]
    };

    User.findOne(searchQuery, function(err, user) {
      if (err) {
	console.log('user search error: %s', err);
	done(err);
      } else {
	if (!user) {
	  var userEmail = providerUserProfile.email;

	  User.findOne({email: userEmail, active: true}, function(err, localuser) {
	    // user's previous provider is different from the current one
	    if (localuser) {
	      // update the provider
	      localuser.provider = providerUserProfile.provider;
	      localuser.providerData = providerUserProfile.providerData;
	      localuser.providerIdentifierField = providerUserProfile.providerIdentifierField;
	      // And save the user
	      localuser.save(function(err) {
		done(err, localuser);
	      });
	    } else {
	      var gender = (providerUserProfile.gender === 'female') ? 1 : 0;
	      user = new User({
		name: providerUserProfile.name,
		email: providerUserProfile.email,
		gender: gender,
		provider: providerUserProfile.provider,
		providerData: providerUserProfile.providerData,
		providerIdentifierField: providerUserProfile.providerIdentifierField,
	      });

	      // And save the user
	      user.save(function(err) {
		done(err, user);
	      });
	    }
	  });
	} else {
	  // user found in the database
	  done(err, user);
	}
      }
    });
  } else {
    // User is already logged in, join the provider data to the existing user
    var user = req.user;

    // Check if user exists, is not signed in using this provider, and doesn't have that provider data already configured
    if (user.provider !== providerUserProfile.provider && (!user.additionalProvidersData || !user.additionalProvidersData[providerUserProfile.provider])) {
      // Add the provider data to the additional provider data field
      if (!user.additionalProvidersData) user.additionalProvidersData = {};
      user.additionalProvidersData[providerUserProfile.provider] = providerUserProfile.providerData;

      // Then tell mongoose that we've updated the additionalProvidersData field
      user.markModified('additionalProvidersData');

      // And save the user
      user.save(function(err) {
	return done(err, user, '/#!/settings/accounts');
      });
    } else {
      return done(new Error('User is already connected using this provider'), user);
    }
  }
};

/**
 * Remove OAuth provider
 */
exports.removeOAuthProvider = function(req, res, next) {
  var user = req.user;
  var provider = req.param('provider');

  if (user && provider) {
    // Delete the additional provider
    if (user.additionalProvidersData[provider]) {
      delete user.additionalProvidersData[provider];

      // Then tell mongoose that we've updated the additionalProvidersData field
      user.markModified('additionalProvidersData');
    }

    user.save(function(err) {
      if (err) {
	return res.status(400).send(errorHandler.getErrorMessage(err));
      } else {
	req.login(user, function(err) {
	  if (err) {
	    res.status(400).send(err);
	  } else {
	    res.json(user);
	  }
	});
      }
    });
  }
};


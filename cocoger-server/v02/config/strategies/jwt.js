'use strict';

/**
 * Module dependencies.
 */
var passport = require('passport'),
    config = require('../config'),
    JwtStrategy = require('passport-jwt').Strategy,
    ExtractJwt = require('passport-jwt').ExtractJwt,
    Device = require('mongoose').model('Device'),
    User = require('mongoose').model('User');

module.exports = function() {
  var opts = {};
  opts.jwtFromRequest = ExtractJwt.fromAuthHeader();
  opts.secretOrKey = config.jwtSecret;

  // jwt token has:
  //  - device: device database id
  //  - id: id string

  passport.use(new JwtStrategy(opts, function(jwt_payload, done) {
    Device.findById(jwt_payload.device, function(err, device) {
      if (err) {
	return done(err);
      }
      if (!device) {
	return done(null, false, {message: 'Unknown user'});
      }
      if (device.id != jwt_payload.id) {
	return done(null, false, {message: 'Need to sign in again'});
      }
      User.findById(device.owner, function(err, user) {
        if (err) {
	  return done(err);
        }
        if (!user) {
	  return done(null, false, {message: 'Unknown user'});
        }
        user.device = device;
        return done(null, user);
      });
    });

    /*
    User.findOne({email: jwt_payload.email, active: true}, function(err, user) {
      if (err) {
	return done(err);
      }
      if (!user) {
	return done(null, false, {
	  message: 'Unknown user'
	});
      }
      return done(null, user);
    });
    */
    /*
      User.findOne({
      email: jwt_payload.email
      }, function(err, user) {
      if (err) {
      return done(err);
      }
      if (!user) {
      return done(null, false, {
      message: 'Unknown user'
      });
      }
      return done(null, user);
      });
    */
  }));
};

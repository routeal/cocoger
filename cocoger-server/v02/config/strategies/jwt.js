'use strict';

/**
 * Module dependencies.
 */
var _ = require('lodash'),
    passport = require('passport'),
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
  //  - user: user schema id
  //  - id: device unique id
  //  - login: device login time

  passport.use(new JwtStrategy(opts, function(jwt_payload, done) {
    User.findById(jwt_payload.user, function(err, user) {
      console.log(jwt_payload);

      if (err) {
	return done(err);
      }
      if (!user) {
	return done(null, false, {message: 'Unknown user'});
      }
      var index = _.findIndex(user.devices, {id: jwt_payload.id});
      if (index < 0) {
	return done(null, false, {message: 'Unknown user'});
      }
      var t1 = new Date(user.devices[index].login).getTime();
      var t2 = new Date(jwt_payload.login).getTime();
      if (t1 != t2) {
	return done(null, false, {message: 'Need to login'});
      }
      user.device = user.devices[index];
      return done(null, user);
    });
  }));
};

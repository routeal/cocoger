'use strict';

/**
 * Module dependencies.
 */
var passport = require('passport'),
	config = require('../config'),
	JwtStrategy = require('passport-jwt').Strategy,
	User = require('mongoose').model('User');

module.exports = function() {
	var opts = {};
	opts.secretOrKey = config.jwtSecret;
	// Use local strategy
	passport.use(new JwtStrategy(opts, function(jwt_payload, done) {
		User.findOne({
			email: jwt_payload.email,
			active: true
		}).populate('device').exec(function(err, user) {
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

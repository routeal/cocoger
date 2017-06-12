'use strict';

/**
 * Module dependencies.
 */
var passport = require('passport'),
    LocalStrategy = require('passport-local').Strategy,
    User = require('mongoose').model('User');

module.exports = function() {
  // Use local strategy
  passport.use(new LocalStrategy(
    {
      usernameField: 'email',
      passwordField: 'password'
    },
    function(email, password, done) {
      User.findOne({
	email: email,
	active: true
      }, function(err, user) {
	if (err) {
	  return done(err);
	}
	if (!user) {
	  return done(null, false, 'Unknown user');
	}
	if (!user.authenticate(password)) {
	  return done(null, false, 'Invalid password');
	}

	return done(null, user);
      });
    }
  ));
};

'use strict';

/**
 * Module dependencies.
 */
var passport = require('passport'),
    FacebookTokenStrategy = require('passport-facebook-token'),
    User = require('mongoose').model('User'),
    config = require('../config');
    //url = require('url'),
//users = require('../../app/controllers/users.server.controller');

module.exports = function() {
  // Use facebook strategy

  passport.use(new FacebookTokenStrategy(
    {
      clientID: config.facebook.clientID,
      clientSecret: config.facebook.clientSecret,
      /*
      profileFields: [
	'id', 'cover', 'name', 'age_range', 'link', 'gender', 'locale', 'picture', 'timezone', // public_profile
        'updated_time', 'verified',
	'emails', // email
	//'friends' // user_friends
      ]
      */
    },
    function(accessToken, refreshToken, profile, done) {
      User.findOne({providerId: profile.id}, function (err, user) {
        console.log(profile);
        if (err) {
          return done(err);
        }
        if (!user) {
          var user = new User({
            provider: profile.provider,
            providerId: profile.id
          });
          user.save(function(err, user) {
            if (err) {
              console.log(err);
            }
            return done(err, user);
          });
        } else {
          console.log("user found");
          return done(err, user);
        }
      });
    }
  ));

};

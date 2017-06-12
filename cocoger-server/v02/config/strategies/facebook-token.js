'use strict';

/**
 * Module dependencies.
 */
var passport = require('passport'),
    url = require('url'),
    FacebookTokenStrategy = require('passport-facebook-token'),
    config = require('../config'),
    users = require('../../app/controllers/users.server.controller');

module.exports = function() {
  // Use facebook strategy

  passport.use(new FacebookTokenStrategy(
    {
      clientID: config.facebook.clientID,
      clientSecret: config.facebook.clientSecret,
      profileFields: [
	'id', 'name', 'link', 'gender', 'locale', 'timezone', // public_profile
	'emails', // email
	'friends' // user_friends
      ]
    },
    function(accessToken, refreshToken, profile, done) {
      console.log(profile);

      // Set the provider data and include tokens
      var providerData = profile._json;
      providerData.accessToken = accessToken;
      providerData.refreshToken = refreshToken;

      // Create the user OAuth profile
      var providerUserProfile = {
	name: profile.name.givenName,
	gender: profile.gender,
	//lastName: profile.name.familyName,
	//displayName: profile.displayName,
	email: profile.emails[0].value,
	photo: profile.photos[0].value,
	//username: profile.username,
	provider: 'facebook',
	providerIdentifierField: 'id',
	providerData: providerData
      };

      // Save the user OAuth profile
      users.saveOAuthUserProfile(null, providerUserProfile, done);
    }
  ));

};

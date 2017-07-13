'use strict';

/**
 * Module dependencies.
 */
var passport = require('passport');

module.exports = function(app) {
  // User Routes
  var users = require('../../app/controllers/users.server.controller');

  var fb_auth = passport.authenticate('facebook-token', {session: false});

  app.route('/auth/login').post(fb_auth, users.login);

  app.route('/auth/logout').post(fb_auth, users.logout);
};

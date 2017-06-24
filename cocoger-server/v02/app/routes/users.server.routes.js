'use strict';

/**
 * Module dependencies.
 */
var passport = require('passport');

module.exports = function(app) {
  // User Routes
  var users = require('../../app/controllers/users.server.controller');

  var auth_callback = passport.authenticate('jwt', {session: false});

  // for testing
  app.route('/test').get(users.test);

  // sign up with email, password, ...
  app.route('/auth/signup').post(users.signup);

  // sign in with email and password and return a jwt token
  app.route('/auth/login').post(users.login);

  // disable the current jwt token
  app.route('/auth/logout').get(auth_callback, users.logout);

  // get a user info which is indentified by the jwt token
  app.route('/users/me').get(auth_callback, users.me);


  /** web interface **/

  // Setting up the users profile api
  app.route('/users').put(users.update);
  app.route('/users/accounts').delete(users.removeOAuthProvider);

  // Setting up the users password api
  app.route('/users/password').post(users.changePassword);
  app.route('/auth/forgot').post(users.forgot);
  app.route('/auth/reset/:token').get(users.validateResetToken);
  app.route('/auth/reset/:token').post(users.reset);

  // Setting the facebook oauth routes from the web
  app.route('/auth/facebook').get(passport.authenticate('facebook', {
    scope: ['public_profile', 'email', 'user_friends']
  }));
  app.route('/auth/facebook/callback').get(users.oauthCallback('facebook'));

  //app.route('/users/feedback').post();

  /** mobile interface **/

  app.route('/m/users').put(passport.authenticate('jwt', {session: false}), users.update);
  app.route('/m/users').delete(passport.authenticate('jwt', {session: false}), users.delete);
  app.route('/m/users/search').get(passport.authenticate('jwt', {session: false}), users.search);
  app.route('/m/users/provider').get(passport.authenticate('jwt', {session: false}), users.provider);

  // Setting the facebook auth from the mobile clients with the token (session-less)
  app.route('/m/auth/facebook').post(users.oauthToken('facebook-token'));

  // Finish by binding the user middleware
  // app.param('userId', users.userByID);
};

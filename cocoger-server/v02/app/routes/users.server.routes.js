'use strict';

/**
 * Module dependencies.
 */
var passport = require('passport');

module.exports = function(app) {
  // User Routes
  var users = require('../../app/controllers/users.server.controller');

  var auth_callback = passport.authenticate('jwt', {session: false});

  // sign up with email, password, ...
  app.route('/m/auth/signup').post(users.signupToken);

  // sign in with email and password and return a jwt token
  app.route('/m/auth/login').post(users.signinToken);

  // disable the current jwt token
  app.route('/m/auth/logout').get(auth_callback, users.signoutToken);

  // get a user info which is indentified by the jwt token
  app.route('/m/users/me').get(auth_callback, users.me);


  /** web interface **/

  // Setting up the users profile api
  app.route('/users/me').get(users.me);
  app.route('/users').put(users.update);
  app.route('/users/accounts').delete(users.removeOAuthProvider);

  // Setting up the users password api
  app.route('/users/password').post(users.changePassword);
  app.route('/auth/forgot').post(users.forgot);
  app.route('/auth/reset/:token').get(users.validateResetToken);
  app.route('/auth/reset/:token').post(users.reset);

  // Setting up the users authentication api
  app.route('/auth/signup').post(users.signup);
  app.route('/auth/signin').post(users.signin);
  app.route('/auth/signout').get(users.signout);

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

  /*
    app.route('/devices/token').post(
    passport.authenticate('jwt', {session: false}), users.createDevice);
    app.route('/devices/token').put(
    passport.authenticate('jwt', {session: false}), users.updateDevice);
    app.route('/devices/token').delete(
    passport.authenticate('jwt', {session: false}), users.deleteDevice);
  */

  /*

  // Setting the twitter oauth routes
  app.route('/auth/twitter').get(passport.authenticate('twitter'));
  app.route('/auth/twitter/callback').get(users.oauthCallback('twitter'));

  // Setting the google oauth routes
  app.route('/auth/google').get(passport.authenticate('google', {
  scope: [
  'https://www.googleapis.com/auth/userinfo.profile',
  'https://www.googleapis.com/auth/userinfo.email'
  ]
  }));
  app.route('/auth/google/callback').get(users.oauthCallback('google'));

  // Setting the linkedin oauth routes
  app.route('/auth/linkedin').get(passport.authenticate('linkedin'));
  app.route('/auth/linkedin/callback').get(users.oauthCallback('linkedin'));

  // Setting the github oauth routes
  app.route('/auth/github').get(passport.authenticate('github'));
  app.route('/auth/github/callback').get(users.oauthCallback('github'));

  */

  // Finish by binding the user middleware
  app.param('userId', users.userByID);
};

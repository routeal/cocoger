'use strict';

/**
 * Module dependencies.
 */
var passport = require('passport');

module.exports = function(app) {

	var friends = require('../../app/controllers/friends.server.controller');
	var users = require('../../app/controllers/users.server.controller');

	/** web interface */

	app.route('/friends')
		.get(friends.listWithUser)
		.post(users.requiresLogin, friends.invite);

	app.route('/friends/:friendId')
		.get(friends.read)
		.put(users.requiresLogin, friends.update)
		.delete(users.requiresLogin, friends.delete);

	/** mobile interface **/

	app.route('/m/friends/invite').post(
		passport.authenticate('jwt', {session: false}), friends.invite);
	app.route('/m/friends/invite').put(
		passport.authenticate('jwt', {session: false}), friends.accept);
	app.route('/m/friends/invite').delete(
		passport.authenticate('jwt', {session: false}), friends.decline);

	app.route('/m/friends').get(
		passport.authenticate('jwt', {session: false}), friends.list);
	app.route('/m/friends').put(
		passport.authenticate('jwt', {session: false}), friends.update);
	app.route('/m/friends').delete(
		passport.authenticate('jwt', {session: false}), friends.delete);

	app.route('/m/friends/range').put(
		passport.authenticate('jwt', {session: false}), friends.acceptRangeChange);
	app.route('/m/friends/range').delete(
		passport.authenticate('jwt', {session: false}), friends.declineRangeChange);

	app.route('//mfriends/ping').post(
		passport.authenticate('jwt', {session: false}), friends.ping);

	// Finish by binding the friend middleware
	app.param('friendId', friends.friendByID);
};

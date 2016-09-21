'use strict';

var passport = require('passport');

module.exports = function(app) {
	var locations = require('../../app/controllers/locations.server.controller');

	/** mobile interface **/

	// list of locations
	app.route('/m/locations')
		.get(passport.authenticate('jwt', {session: false}), locations.list)
		.post(passport.authenticate('jwt', {session: false}), locations.create);

	// last known location
	app.route('/m/locations/latest')
		.get(passport.authenticate('jwt', {session: false}), locations.latest);

	/** web interface **/

	app.route('/locations')
		.get(locations.list)
		.post(locations.create);

	app.route('/locations/:locationFriendId')
		.get(locations.read);

	app.param('locationFriendId', locations.byFriendID);
};

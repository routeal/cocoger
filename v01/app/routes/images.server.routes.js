'use strict';

var passport = require('passport');

module.exports = function(app) {
	// User Routes
	var images = require('../../app/controllers/images.server.controller');

	app.route('/m/images')
		.get(passport.authenticate('jwt', {session: false}), images.get)
		.post(passport.authenticate('jwt', {session: false}), images.upload)
		.delete(passport.authenticate('jwt', {session: false}), images.delete);
};

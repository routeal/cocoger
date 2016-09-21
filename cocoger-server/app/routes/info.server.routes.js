'use strict';

/**
 * Module dependencies.
 */

module.exports = function(app) {
	// User Routes
	var info = require('../../app/controllers/info.server.controller');

	/** won't be used **/

	/*
	app.route('/info/about/:localeId').get(info.about);
	app.route('/info/faq/:localeId').get(info.faq);
	app.route('/info/term/:localeId').get(info.term);
	app.route('/info/privacy/:localeId').get(info.privacy);
	app.route('/info/forgot/:localeId').get(info.forgot);

	app.param('localeId', info.byLocaleID);
	*/

};

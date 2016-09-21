'use strict';
/**
 * Module dependencies.
 */
var init = require('./config/init')(),
	config = require('./config/config'),
	mongoose = require('mongoose'),
	chalk = require('chalk'),
	cluster = require('cluster'),
	async = require('async'),
	os = require('os');


/*
if (cluster.isMaster) {
	var cpus = os.cpus().length;

	for (var i = 0; i < cpus; i++) {
		console.log('%d forked', i);
		cluster.fork();
	}

	cluster.on('disconnect', function(worker) {
		console.error('cluster disconnect');
		cluster.fork();
	});
} else
*/

{

/**
 * Main application entry file.
 * Please note that the order of loading is important.
 */

// Bootstrap db connection
var db = mongoose.connect(config.db.address, function(err) {
	if (err) {
		console.error(chalk.red('Could not connect to MongoDB!'));
		console.log(chalk.red(err));
	}
});

// Init the express application
var app = require('./config/express')(db);

// Bootstrap passport config
require('./config/passport')();

// Bootstrap agenda
require('./config/agenda');

// Start the app by listening on <port>
app.listen(config.port);

// Expose app
exports = module.exports = app;

// Logging initialization
console.log('MEAN.JS application started on port ' + config.port);

}

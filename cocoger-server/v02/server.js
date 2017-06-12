'use strict';
/**
 * Module dependencies.
 */
var init = require('./config/init')(),
    config = require('./config/config'),
    mongoose = require('mongoose');

console.log();
console.log("/////////////////// START CURRENT CONFIGURATION ///////////////////");
console.log(config);
console.log("/////////////////// END CURRENT CONFIGURATION ///////////////////");
console.log();

/**
 * Main application entry file.
 * Please note that the order of loading is important.
 */

mongoose.Promise = require('bluebird');

// Bootstrap db connection
var db = mongoose.connect(config.db.address, function(err) {
  if (err) {
    console.error('Could not connect to MongoDB!');
    console.log(err);
  }
});

// Init the express application
var app = require('./config/express')(db);

// Bootstrap passport config
require('./config/passport')();

// Bootstrap agenda
// require('./config/agenda');

console.log("/////////////////// START LOCAL VARIABLES ///////////////////");
console.log(app.locals);
console.log("/////////////////// END LOCAL VARIABLES ///////////////////");

// Start the app by listening on <port>
app.listen(config.port);

// Expose app
exports = module.exports = app;

// Logging initialization
console.log('started on port ' + config.port);

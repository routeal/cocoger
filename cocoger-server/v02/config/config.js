'use strict';

/**
 * Module dependencies.
 */
var _ = require('lodash'),
    glob = require('glob');

/**
 * Load app configurations
 */
module.exports = _.extend(
  require('./env/all'),
  require('./env/' + process.env.NODE_ENV) || {}
);

/**
 * Get files by glob patterns
 */
module.exports.getGlobbedFiles = function(globPatterns, removeRoot) {
  // For context switching
  var _this = this;

  // URL paths regex
  var urlRegex = new RegExp('^(?:[a-z]+:)?\/\/', 'i');

  // The output array
  var output = [];

  // If glob pattern is array so we use each pattern in a recursive way, otherwise we use glob 
  if (_.isArray(globPatterns)) {
    globPatterns.forEach(function(globPattern) {
      output = _.union(output, _this.getGlobbedFiles(globPattern, removeRoot));
    });
  } else if (_.isString(globPatterns)) {
    if (urlRegex.test(globPatterns)) {
      output.push(globPatterns);
    } else {
      var files = glob.sync(globPatterns);
      if (removeRoot) {
	files = files.map(function(file) {
	  return file.replace(removeRoot, '');
	});
      }
      output = _.union(output, files);
    }
  }

  return output;
};

/**
 * Get the modules JavaScript files
 */
/*
  module.exports.getJavaScriptAssets = function(includeTests) {
  var output = this.getGlobbedFiles(this.assets.lib.js.concat(this.assets.js), 'public/');

  // To include tests
  if (includeTests) {
  output = _.union(output, this.getGlobbedFiles(this.assets.tests));
  }

  return output;
  };
*/

/**
 * Get the modules CSS files
 */
/*
  module.exports.getCSSAssets = function() {
  var output = this.getGlobbedFiles(this.assets.lib.css.concat(this.assets.css), 'public/');
  return output;
  };
*/


/**
 * Location Ranges
 */

var FullLocationRange = {
  'street'  : 3,
  'town'    : 6,
  'city'    : 9,
  'county'  : 12,
  'state'   : 15,
  'country' : 18,
  'none'    : 21,
};

var JapanLocationRange = {
  'street'  : 3,
  'town'    : 6,
  'city'    : 9,
  'state'   : 15,
  'country' : 18,
};

var UsLocationRange = {
  'street'  : 3,
  'town'    : 6,
  'city'    : 9,
  'county'  : 12,
  'state'   : 15,
  'country' : 18,
};

var locationRange = JapanLocationRange;

module.exports.LocationRange = FullLocationRange;

module.exports.getLocationRange = function(location) {

  if (!location) {
    return FullLocationRange;
  }
  else if (location.toLowerCase() === 'ja') {
    return JapanLocationRange;
  }
  else if (location.toLowerCase() === 'us') {
    return UsLocationRange;
  }
  return JapanLocationRange;
};

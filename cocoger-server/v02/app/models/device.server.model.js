'use strict';

/**
 * Module dependencies.
 */
var mongoose = require('mongoose'),
    Schema = mongoose.Schema;

/**
 * Device Schema
 */
var DeviceSchema = new Schema({
  // timestamp
  created: {
    type: Date,
    default: Date.now
  },
  // device unique id
  deviceId: {
    type: String,
    default: ''
  },
  // phone/tablet/desktop/settop
  type: {
    type: String,
    default: 'mobile'
  },
  // ios, android, windows, etc
  platform: {
    type: String,
    default: ''
  },
  // 10.12(ios), 7(android)
  platformVersion: {
    type: String,
    default: ''
  },
  brand: {
    type: String,
    default: ''
  },
  model: {
    type: String,
    default: ''
  },
  simulator: {
    type: Boolean,
    default: true,
  },
  token: {
    type: String,
    default: ''
  },
  // UNAVAILABLE(being logout) = 0, BACKGROUND = 1, FOREGROUND = 2
  status: {
    type: Number,
    default: 2,
  },
  // app version
  appVersion: {
    type: String,
    default: '0.1'
  }
});

DeviceSchema.set('toJSON', {
  transform: function(doc, ret, options) {
    var retJson = {
      deviceId: ret.deviceId,
      type: ret.type,
      platform: ret.platform,
      platformVersion: ret.platformVersion,
      brand: ret.brand,
      model: ret.model,
      simulator: ret.simulator,
      token: ret.token,
      status: ret.status,
      appVersion: ret.appVersion
    };
    return retJson;
  }
});

/* not needed for now
DeviceSchema.pre('save', function(next) {
  next();
});
*/

mongoose.model('Device', DeviceSchema);

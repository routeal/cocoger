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
  owner: {
    type: Schema.ObjectId,
    ref: 'User'
  },
  // TODO: device token for apn
  deviceToken: {
    type: String,
    default: ''
  },
  // app version
  appVersion: {
    type: String,
    default: '0.1'
  },
  // iphone, nexus, ...
  device: {
    type: String,
    default: 'iphone4s'
  },
  // ios, android, windows, etc
  platform: {
    type: String,
    default: 'ios'
  },
  platformVersion: {
    type: String,
    default: ''
  },
  lang: {
    type: String,
    default: 'en'
  },
  country: {
    type: String,
    default: 'US'
  },
  created: {
    type: Date,
    default: Date.now
  },
  id: {
    type: String,
    default: '',
  },
  isSimulator: {
    type: Boolean,
    default: true,
  },
  activated: {
    type: Boolean,
    default: true,
  },
  // not used for now
  notification: {
    type: Boolean,
    default: true,
  },
});

DeviceSchema.set('toJSON', {
  transform: function(doc, ret, options) {
    var retJson = {
      deviceToken:     ret.deviceToken,
      appVersion:      ret.appVersion,
      platform:        ret.platform,
      platformVersion: ret.platformVersion,
      lang:		 ret.lang,
      country:	 ret.country,
      activated:       ret.activated,
      isSimulator:     ret.isSimulator,
      created:         ret.created
    };
    return retJson;
  }
});

DeviceSchema.pre('save', function(next) {
  if (this.platform === 'iPhone OS') {
    console.log('device object saved');
  }
  next();
});

mongoose.model('Device', DeviceSchema);

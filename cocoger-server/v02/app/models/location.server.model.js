'use strict';

/**
 * Module dependencies.
 */
var mongoose = require('mongoose'),
    Schema = mongoose.Schema;

/**
 * Location Schema
 */
var LocationSchema = new Schema({
  user		: {
    type	: Schema.ObjectId,
    ref	        : 'User'
  },
  coordinates   : {
    type        : [Number],
    index       : '2dsphere'
  },
  altitude      : {
    type        : Number,
    default     : 0
  },
  speed         : {
    type        : Number,
    default     : 0
  },
  time          : {
    type        : Date,
    default     : undefined
  },
  postalCode    : {
    type        : String,
    default     : ''
  },
  countryName   : {
    type        : String,
    default     : ''
  },
  adminArea     : {
    type        : String,
    default     : ''
  },
  subAdminArea  : {
    type        : String,
    default     : ''
  },
  locality      : {
    type        : String,
    default     : ''
  },
  subLocality   : {
    type        : String,
    default     : ''
  },
  thoroughfare  : {
    type        : String,
    default     : ''
  },
  subThoroughfare: {
    type        : String,
    default     : ''
  }
});

LocationSchema.set('toJSON', {
  transform: function(doc, ret, options) {
    var retJson = {
      latitude        : ret.coordinates[1],
      longitude       : ret.coordinates[0],
      altitude        : ret.altitude,
      speed           : ret.speed,
      time            : ret.time, // FIXME: integer
      postalCode      : ret.postalCode,
      countryName     : ret.countryName,
      adminArea       : ret.adminArea,
      subAdminArea    : ret.subAdminArea,
      locality        : ret.locality,
      subLocality     : ret.subLocality,
      thoroughfare    : ret.thoroughfare,
      subThoroughfare : ret.subThoroughfare
    };
    return retJson;
  }
});

LocationSchema.pre('save', function (next, req, callback) {
  console.log(req);
  //this.user = req.user._id;
  next(callback);
});


mongoose.model('Location', LocationSchema);

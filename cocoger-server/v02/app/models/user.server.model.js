'use strict';

/**
 * Module dependencies.
 */
var mongoose = require('mongoose'),
    Schema = mongoose.Schema,
    Location = mongoose.model('Location'),
    Device = mongoose.model('Device'),
    crypto = require('crypto'),
    validator = require('validator');

/**
 * A Validation function for local strategy properties
 */
var validateLocalStrategyProperty = function(property) {
  return ((this.provider !== 'local' && !this.updated) || property.length);
};

/**
 * A Validation function for local strategy email
 */
var validateLocalStrategyEmail = function (email) {
  return ((this.provider !== 'local' && !this.updated) || validator.isEmail(email, { require_tld: false }));
};


/**
 * A Validation function for local strategy password
 */
var validateLocalStrategyPassword = function(password) {
  return (this.provider !== 'local' || (password && password.length >= 6));
};

/**
 * User Schema
 */
var UserSchema = new Schema({

  /*********************************************************************/
  /* user attributes where are exposed to the user */
  /*********************************************************************/

  email: {
    type: String,
    index: {
      unique: true,
      sparse: true
    },
    trim: true,
    default: '',
    //validate: [validateLocalStrategyEmail, 'Please fill a valid email address']
  },
  firstName: {
    type: String,
    trim: true,
    default: '',
    //validate: [validateLocalStrategyProperty, 'Please fill in your first name']
  },
  lastName: {
    type: String,
    trim: true,
    default: '',
    //validate: [validateLocalStrategyProperty, 'Please fill in your last name']
  },
  name: {
    type: String,
    trim: true,
    default: ''
  },
  gender: {
    type: String,
    default: '',
  },
  picture: {
    type: String,
    default: ''
  },
  locale: {
    type: String,
    default: '',
  },
  timezone: {
    type: String,
    default: '',
  },

  /*********************************************************************/
  /* colors  */
  /*********************************************************************/

  myColor: {
    type: String,
    default: '66ffff',
  },
  boyColor: {
    type: String,
    default: '9bb7a7',
  },
  girlColor: {
    type: String,
    default: 'ff6633',
  },
  frameColor: {
    type: String,
    default: '007aff',
  },

  /*********************************************************************/
  /* dates */
  /*********************************************************************/

  created: {
    type: Date,
    default: Date.now
  },
  updated: {
    type: Date,
    default: Date.now
  },

  /*********************************************************************/
  /* passport provider */
  /*********************************************************************/

  // default passport provider is facebook
  provider: {
    type: String,
    required: 'Provider is required'
  },
  providerData: {},
  additionalProvidersData: {},

  /*********************************************************************/
  /* password management for the local provider */
  /*********************************************************************/

  password: {
    type: String,
    default: '',
  },
  salt: {
    type: String
  },
  /* For reset password */
  resetPasswordToken: {
    type: String
  },
  resetPasswordExpires: {
    type: Date
  },

  /*********************************************************************/
  /* roles */
  /*********************************************************************/

  roles: {
    type: [{
      type: String,
      enum: ['user', 'admin']
    }],
    default: ['user'],
    required: 'Please provide at least one role'
  },

  /*********************************************************************/
  /* Friends */
  /*********************************************************************/

  friends: [
    {
      user: {
	type: Schema.ObjectId,
	ref: 'User'
      },
      range: {
	type: Number,
	default: 0
      },
      status: {
	type: Number, // 0=requested, 1=approved, 2=reject, 3=rejected
	default: 0
      },
      approved: {
	type: Date,
	default: undefined
      }
    }
  ],

  /*********************************************************************/
  /* Location */
  /*********************************************************************/

  lastLocationId: {
    type: Schema.ObjectId,
    ref: 'Location',
    default: undefined
  },

  // Note: location data are saved independently in the database since
  // they could become a huge array of the location data

  /*********************************************************************/
  /* Group list */
  /*********************************************************************/

  groups: [
    {
      type: Schema.ObjectId,
      ref: 'Group'
    }
  ],

  /*********************************************************************/
  /* Device list */
  /*********************************************************************/

  devices: [ Device.schema ],

});

// FIXME: send the last location, a list of the friends, and a list of
// the groups
UserSchema.set('toJSON', {
  transform: function(doc, ret, options) {
    var retJson = {
      email: ret.email,
      firstName: ret.firstName,
      lastName: ret.lastName,
      name: ret.name,
      gender: ret.gender,
      picture: ret.picture,
      locale: ret.locale,
      timezone: ret.timezone,
      updated: ret.updated,
      myColor: ret.myColor,
      boyColor: ret.boyColor,
      girlColor: ret.girlColor,
      frameColor: ret.frameColor,
    };
    //console.log(retJson);
    return retJson;
  }
});

/**
 * Hook a pre save method to hash the password
 */
UserSchema.pre('save', function(next) {
  if (this.password && this.isModified('password')) {
    this.salt = crypto.randomBytes(16).toString('base64');
    this.password = this.hashPassword(this.password);
  }
  /*
  if (!this.photoName) {
    if (this.gender === 0) {
      this.photoName = 'boy';
    } else {
      this.photoName = 'girl';
    }
  }
  */
  next();
});

/**
 * Hook a pre validate method to test the local password
 */
UserSchema.pre('validate', function (next) {
  if (this.provider === 'local' && this.password && this.isModified('password')) {
    var result = owasp.test(this.password);
    if (result.errors.length) {
      var error = result.errors.join(' ');
      this.invalidate('password', error);
    }
  }

  next();
});

/**
 * Create instance method for hashing a password
 */
UserSchema.methods.hashPassword = function(password) {
  if (this.salt && password) {
    return crypto.pbkdf2Sync(password, this.salt, 10000, 64, 'sha1').toString('base64');
  } else {
    return password;
  }
};

/**
 * Create instance method for authenticating user
 */
UserSchema.methods.authenticate = function(password) {
  return this.password === this.hashPassword(password);
};

/**
 * Find possible not used username
 */
UserSchema.statics.findUniqueUsername = function(username, suffix, callback) {
  var _this = this;
  var possibleUsername = username + (suffix || '');

  _this.findOne({
    username: possibleUsername
  }, function(err, user) {
    if (!err) {
      if (!user) {
	callback(possibleUsername);
      } else {
	return _this.findUniqueUsername(username, (suffix || 0) + 1, callback);
      }
    } else {
      callback(null);
    }
  });
};

mongoose.model('User', UserSchema);

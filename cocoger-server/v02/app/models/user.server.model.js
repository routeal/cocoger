'use strict';

/**
 * Module dependencies.
 */
var mongoose = require('mongoose'),
    Schema = mongoose.Schema,
    Friend = mongoose.model('Friend'),
    Location = mongoose.model('Location'),
    crypto = require('crypto');

/**
 * A Validation function for local strategy properties
 */
var validateLocalStrategyProperty = function(property) {
  return ((this.provider !== 'local' && !this.updated) || property.length);
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
    trim: true,
    unique: 'testing error message',
    required: 'Please fill in a email',
    validate: [validateLocalStrategyProperty, 'Please fill in your email'],
    match: [/.+\@.+\..+/, 'Please fill a valid email address']
  },
  name: {
    type: String,
    default: '',
    trim: true
  },
  bod: {
    type: Number,
    default: 1980,
  },
  gender: {
    type: Number,
    default: 0,
  },
  photoName: {
    type: String,
    default: 'alien'
  },
  photoType: {
    type: Number,
    default: 0
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
  /* password management */
  /*********************************************************************/

  password: {
    type: String,
    default: '',
    validate: [validateLocalStrategyPassword, 'Password should be longer']
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
  /* passport provider */
  /*********************************************************************/

  // passport provider
  provider: {
    type: String,
    required: 'Provider is required'
  },
  providerData: {},
  providerIdentifierField: {
    type: String,
    default: 'id',
  },
  additionalProvidersData: {},

  /*********************************************************************/
  /* Friends */
  /*********************************************************************/

  friends: [ Friend.schema ],

  /*********************************************************************/
  /* Locations */
  /*********************************************************************/

  locations: [ Location.schema ],

  /*********************************************************************/
  /* Group list */
  /*********************************************************************/

  groups: [ Schema.ObjectId ],

  /*********************************************************************/
  /* Device list */
  /*********************************************************************/

  devices: [
    {
      // device unique id
      id: {
        type: String,
        default: ''
      },
      // mobile/desktop
      type: {
        type: String,
        default: 'mobile'
      },
      brand: {
        type: String,
        default: ''
      },
      model: {
        type: String,
        default: ''
      },
      // ios, android, windows, etc
      platform: {
        type: String,
        default: 'ios'
      },
      // 10.12(ios), 7(android)
      platformVersion: {
        type: String,
        default: ''
      },
      // iso
      lang: {
        type: String,
        default: 'en'
      },
      // iso
      country: {
        type: String,
        default: 'US'
      },
      created: {
        type: Date,
        default: Date.now
      },
      simulator: {
        type: Boolean,
        default: true,
      },
      login: {
        type: Date,
        default: '',
      }
    }
  ]

});

UserSchema.set('toJSON', {
  transform: function(doc, ret, options) {
    var retJson = {
      /* authToken */
      bod : ret.bod,
      created: ret.created,
      /* deviceToken */
      email: ret.email,
      gender: ret.gender,
      id: ret._id,
      name: ret.name,
      photoName: ret.photoName,
      photoType: ret.photoType,
      /* providerID */
      /* providerToken */
      updated: ret.updated, /* translate to revised */
      /* location: ret.location, */
      /* device: ret.device, */
      myColor: ret.myColor,
      boyColor: ret.boyColor,
      girlColor: ret.girlColor,
      frameColor: ret.frameColor,
      provider: ret.provider,
    };
    return retJson;
  }
});

/**
 * Hook a pre save method to hash the password
 */
UserSchema.pre('save', function(next) {
  if (this.password && this.password.length >= 6 && this.password.length < 24) {
    this.salt = new Buffer(crypto.randomBytes(16).toString('base64'), 'base64');
    this.password = this.hashPassword(this.password);
  }
  if (!this.photoName) {
    if (this.gender === 0) {
      this.photoName = 'boy';
    } else {
      this.photoName = 'girl';
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

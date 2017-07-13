'use strict';

/**
 * Module dependencies.
 */
var mongoose = require('mongoose'),
    Schema = mongoose.Schema,
    Friend = mongoose.model('Friend'),
    Location = mongoose.model('Location'),
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
    validate: [validateLocalStrategyEmail, 'Please fill a valid email address']
  },
  firstName: {
    type: String,
    trim: true,
    default: '',
    validate: [validateLocalStrategyProperty, 'Please fill in your first name']
  },
  lastName: {
    type: String,
    trim: true,
    default: '',
    validate: [validateLocalStrategyProperty, 'Please fill in your last name']

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
    type: Date
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

  //providerId: {
  //  type: String,
  //  unique: 'provider id should be unique',
  //  required: 'provider id must be filled in'
  //},

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
      // ios, android, windows, etc
      platform: {
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
      // 10.12(ios), 7(android)
      version: {
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
      // UNAVAILABLE = 0, BACKGROUND = 1, FOREGROUND = 2
      status: {
        type: Number,
        default: 2,
      }
    }
  ]

});

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

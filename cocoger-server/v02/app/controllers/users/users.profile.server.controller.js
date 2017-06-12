'use strict';

/**
 * Module dependencies.
 */
var _ = require('lodash'),
    errorHandler = require('../errors.server.controller'),
    mongoose = require('mongoose'),
    ObjectId = require('mongoose').Types.ObjectId,
    passport = require('passport'),
    User = mongoose.model('User'),
    Friend = mongoose.model('Friend'),
    Device = mongoose.model('Device'),
    apn = require('../../utils/apn.wrapper'),
    moment = require('moment-timezone'),
    agenda = require('../../../config/agenda');

/**
 * updates the user
 */
exports.update = function(req, res) {
  // Init Variables
  var user = req.user;

  // For security measurement we remove the roles from the req.body object
  delete req.body.roles;

  if (req.body.deviceToken) {
    console.log('new devicetoken: %s', req.body.deviceToken);
    Device.findOne({owner: user._id}, function(err, device) {
      if (err || !device) {
	res.status(400).send({message: 'no device found'});
      } else {
	device.deviceToken = req.body.deviceToken;
	device.save(function(err) {
	  if (err) {
	    res.status(400).send({message: errorHandler.getErrorMessage(err)});
	  } else {
	    res.json({});
	  }
	});
      }
    });
  } else {
    // Merge existing user
    user = _.extend(user, req.body);
    user.updated = Date.now();

    user.save(function(err) {
      if (err) {
	res.status(400).send({message: errorHandler.getErrorMessage(err)});
      } else {
	res.json({});
      }
    });
  }
};

exports.delete = function(req, res) {
  var user = req.user;
  var userid = req.user.id; // keep it as a string

  user.email = '-deleted-' + moment().format('x') + '-' + user.email;

  user.active = false;

  user.save(function(err) {
    if (err) {
      res.status(400).send({message: errorHandler.getErrorMessage(err)});
    } else {
      req.logout();

      // device is intact for now - can be deleted

      // delete self from the friends with keeping the self's friend list

      var arg = {owner: new ObjectId(userid), approved: true};

      Friend.find(arg, function(err, friends) {
	if (err) {
	  res.status(400).send({message: errorHandler.getErrorMessage(err)});
	} else {
	  var completed = 0;

	  var done = function (err) {
	    if (++completed === friends.length) {
	      res.json({});
	      //res.status(200).send();
	    }
	  };

	  friends.forEach(function(friend) {
	    Friend.findOne({owner: friend.user, user: friend.owner}).remove(function(err) {
	      User.findById(friend.user).populate('device').exec(function(err, fuser) {
		if (fuser.device.deviceToken) {
		  apn.sendFriendRemoval(fuser.device.deviceToken, userid);
		}
		friend.remove(done);
	      });
	    });
	  });
	}
      });
    }
  });
};

// returns the user
exports.me = function(req, res) {
  res.json(req.user || null);
};

// searches by email
exports.search = function(req, res) {
  var data = req.query.search;
  User.find().or([{ 'email': data}, { 'name': new RegExp(data, 'i')}]).exec(function(err, users) {
    if (err) {
      res.status(400).send({message: errorHandler.getErrorMessage(err)});
    } else if (!users) {
      // nothing found
      res.json({});
    } else {
      // remove self
      for (var i = 0; i < users.length; i++) {
	if (users[i].email === req.user.email) {
	  users.splice(i, 1);
	  break;
	}
      }
      // remove friends
      Friend.find({owner: req.user.id, approved: true}).populate('user').exec(function(err, friends) {
	if (err) {
	  res.status(400).send({message: errorHandler.getErrorMessage(err)});
	} else {
	  // remove the exisiting friends
	  for (var i = 0; i < friends.length; i++) {
	    var j = users.length;
	    while (j--) {
	      if (users[j].email === friends[i].user.email) {
		users.splice(j, 1);
	      }
	    }
	  }
	  res.json(users);
	}
      });
    }
  });
};

// find a user by the provider id - currently facebook only
exports.provider = function(req, res) {
  var id = req.query.id;
  /*
    console.log('id field: %s', req.user.providerIdentifierField);
    console.log('id: %s', id);
  */
  User.findOne({'provider':'facebook', 'providerData.id': id}, function(err, user) {
    if (err) {
      res.status(400).send({message: errorHandler.getErrorMessage(err)});
    } else if (!user) {
      //console.log('not found');
      res.json({});
    } else {
      console.log('found: %j', user);
      res.json(user);
    }
  });
};

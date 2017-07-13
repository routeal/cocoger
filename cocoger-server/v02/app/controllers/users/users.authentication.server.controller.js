'use strict';

/**
 * Module dependencies.
 */
var _ = require('lodash'),
    errorHandler = require('../errors.server.controller'),
    mongoose = require('mongoose'),
    Device = mongoose.model('Device'),
    User = mongoose.model('User');

exports.login = function(req, res) {
  //console.log(req.body);

  // user in the database
  var user = req.user;

  // the user object should be created by the facebook strategy.
  if (!user) {
    res.status(401).send({message: 'User is not logged in'});
    return;
  }

  // user in the device, which may have newer information
  var dev_user = req.body;

  // by default, will not save the user object again
  var save = false;

  if (user.email) {
    var index = _.findIndex(user.devices, {id: dev_user.device.id});
    if (index < 0) {
      console.log("login: new device found");
      user.devices.push(dev_user.device);
      save = true;
    } else {
      var dev = user.devices[index]; // device in the database
      var new_dev = dev_user.device; // device in the device
      //console.log(dev);
      //console.log(new_dev);
      if (dev.token != new_dev.token || dev.status != new_dev.status) {
        console.log("login: new value found in device");
        // FIXME:
        //_.extend(user.devices[index], dev_user.device);
        _.extend(dev, new_dev);
        save = true;
      }
      var updated = new Date(dev_user.updated);
      if (updated.getTime() > user.updated.getTime()) {
        console.log("login: user updated changed");
        user.email = dev_user.email;
        user.name = dev_user.name;
        user.firstName = dev_user.firstName;
        user.lastName = dev_user.lastName;
        user.gender = dev_user.gender;
        user.picture = dev_user.picture;
        user.timezone = dev_user.timezone;
        user.locale = dev_user.locale;
        user.updated = updated;
        save = true;
      }
    }
  } else {
    // when the user is created by the facebook stragegy, most info
    // other than providers are empty.
    console.log("login: initial login");
    save = true;
    user.devices.push(dev_user.device);
    user.email = dev_user.email;
    user.name = dev_user.name;
    user.firstName = dev_user.firstName;
    user.lastName = dev_user.lastName;
    user.gender = dev_user.gender;
    user.picture = dev_user.picture;
    user.timezone = dev_user.timezone;
    user.locale = dev_user.locale;
    user.updated = new Date(dev_user.updated);
    //console.log(user);
  }

  if (save) {
    user.save(function(err, user) {
      if (err) {
        console.log("error in save: " + errorHandler.getErrorMessage(err));
	res.status(400).send({message: errorHandler.getErrorMessage(err)});
      } else {
        console.log("save");
        res.json(user);
      }
    });
  } else {
    console.log("no save");
    res.json(user);
  }
};


exports.logout = function(req, res) {
  var user = req.user;
  if (!user) {
    res.status(401).send({message: 'User is not logged in'});
    return;
  }

  var device = req.body;
  if (!device) {
    res.status(401).send({message: 'Device is not sent'});
    return;
  }

  var index = _.findIndex(user.devices, {id: device.id});
  if (index < 0) {
    res.status(401).send({message: 'device not found'});
    return;
  }

  // makes the device unavailable
  user.devices[index].status = 0;
  user.save(function(err, user) {
    if (err) {
      res.status(400).send({message: errorHandler.getErrorMessage(err)});
    } else {
      res.json();
    }
  });
};

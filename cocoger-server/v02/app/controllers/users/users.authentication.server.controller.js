'use strict';

/**
 * Module dependencies.
 */
var _ = require('lodash'),
    errorHandler = require('../errors.server.controller'),
    mongoose = require('mongoose'),
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

  // convenient function to assign new values to the user
  function saveUser(to, from) {
    to.email = from.email;
    to.firstName = from.firstName;
    to.lastName = from.lastName;
    to.name = from.name;
    to.gender = from.gender;
    to.picture = from.picture;
    to.locale = from.locale;
    to.timezone = from.timezone;
    to.updated = new Date(from.updated);
  }

  console.log("new user device:" + dev_user.device.deviceId);

  // in the first time, user.email is empty
  if (user.email) {
    console.log("existing user device:" + user.devices);
    var index = _.findIndex(user.devices, {deviceId: dev_user.device.deviceId});
    if (index < 0) {
      console.log("login: new device found");
      user.devices.push(dev_user.device);
      save = true;
    } else {
      //console.log(dev);
      //console.log(new_dev);
      if (user.devices[index].status != dev_user.device.status) {
        console.log("login: new value found in device");
        _.extend(user.devices[index], dev_user.device);
        save = true;
      }
      if (dev_user.updated > user.updated.getTime()) {
        console.log("login: user updated changed");
        saveUser(user, dev_user);
        save = true;
      }
    }
  } else {
    // set both user and device
    console.log("login: initial login");
    console.log(dev_user);
    user.devices.push(dev_user.device);
    saveUser(user, dev_user);
    save = true;
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

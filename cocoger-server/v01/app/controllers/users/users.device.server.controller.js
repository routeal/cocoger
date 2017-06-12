'use strict';

/**
 * Module dependencies.
 */
var _ = require('lodash'),
    errorHandler = require('../errors.server.controller'),
    mongoose = require('mongoose'),
    User = mongoose.model('User'),
    Device = mongoose.model('Device');

/**
 * creates a new device
 */
exports.createDevice = function(req, res) {
	var user = req.user;

	var device = new Device(req.body);

	device.save(function(err, device) {
		if (err) {
			res.status(400).send(errorHandler.getErrorMessage(err));
		} else {
			user.device = device.id;

			user.save(function(err) {
				if (err) {
					res.status(400).send(errorHandler.getErrorMessage(err));
				} else {
					res.json(device);
				}
			});
		}
	});
};


/**
 * updates the device
 */
exports.updateDevice = function(req, res) {
	var user = req.user;

	Device.findById(user.device, function(err, device) {
		if (err) {
			res.status(400).send(errorHandler.getErrorMessage(err));
		} else if (!device.activated) {
			res.status(404).send({message: 'No such device available'});
		} else {
			device = _.extend(device, req.body);

			device.save(function(err, device) {
				if (err) {
					res.status(400).send(errorHandler.getErrorMessage(err));
				} else {
					res.json(device);
				}
			});
		}
	});
};

/**
 * deactives the device
 */
exports.deleteDevice = function(req, res) {
	var user = req.user;

	Device.findById(user.device, function(err, device) {
		if (err) {
			res.status(400).send(errorHandler.getErrorMessage(err));
		} else if (!device.activated) {
			res.status(404).send({message: 'No such device available'});
		} else {
			device.activated = false;

			device.save(function(err, device) {
				if (err) {
					res.status(400).send(errorHandler.getErrorMessage(err));
				} else {
					res.status(200).send();
				}
			});
		}
	});
};

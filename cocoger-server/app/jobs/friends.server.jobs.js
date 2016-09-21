'use strict';

/**
 *
 * NOTE:
 * % mongo <db>
 * > db.agendaJobs.find()
 *
 */

var mongoose = require('mongoose'),
    ObjectId = require('mongoose').Types.ObjectId,
    Friend = mongoose.model('Friend'),
    config = require('../../config/config'),
    apn = require('../utils/apn.wrapper'),
    logger = require('winston'),
    Device = mongoose.model('Device'),
    User = mongoose.model('User');


module.exports = function(agenda) {

	agenda.define('invite resender', function(job, done) {
		var data = job.attrs.data;

		Friend.findById(data.friend, function(err, friend) {

			function completed() {
				job.remove(function(err) {
					done();
				});
			}

			// friend is deleted when declined
			if (err || !friend) {
				completed();
				return;
			}

			// invitation is already approved
			if (friend.approved) {
				completed();
				return;
			}

			// always look up the current device info
			User.findById(friend.user).populate('device').exec(function(err, inviteeUserObj) {
				if (err || !inviteeUserObj) {
					done('can\'t get the user object');
					return;
				}

				if (inviteeUserObj.device.deviceToken) {
					apn.sendInvite(inviteeUserObj.device.deviceToken, data.message,
						       data.name, friend.id, friend.range, data.id);
					data.count += 1;
				} else {
					logger.info('invite resender: deviceToken not found');
				}

				if (data.count < config.maxRequestCount) {
					agenda.schedule('1 day', 'invite resender', data);
				}

				completed();
			});
		});
	});

	agenda.define('range request resender', function(job, done) {
		var data = job.attrs.data;

		Friend.findById(data.friend, function(err, friend) {

			function completed() {
				job.remove(function(err) {
					done();
				});
			}

			if (err || !friend) {
				completed();
				return;
			}

			if (friend.range === data.range) {
				completed();
				return;
			}

			// always look up the current device info
			User.findById(friend.user).populate('device').exec(function(err, requesteeUserObj) {
				if (err || !requesteeUserObj) {
					done('can\'t get the user object');
					return;
				}

				if (requesteeUserObj.device.deviceToken) {
					apn.sendRangeChangeRequest(requesteeUserObj.device.deviceToken,
								   data.name, friend.user, data.range, data.id);
					data.count += 1;
				} else {
					logger.info('range request resender: deviceToken not found');
				}

				if (data.count < config.maxRequestCount) {
					agenda.schedule('1 day', 'range request resender', data);
				}

				// remove the current job from the db
				completed();
			});
		});
	});

};

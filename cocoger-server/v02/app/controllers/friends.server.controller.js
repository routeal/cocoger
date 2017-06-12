'use strict';

/**
 * Module dependencies.
 */
var _ = require('lodash'),
    errorHandler = require('./errors.server.controller'),
    mongoose = require('mongoose'),
    ObjectId = require('mongoose').Types.ObjectId,
    passport = require('passport'),
    User = mongoose.model('User'),
    Friend = mongoose.model('Friend'),
    apn = require('../utils/apn.wrapper'),
    async = require('async'),
    logger = require('winston'),
    agenda = require('../../config/agenda'),
    shortid = require('shortid');

/**
 * returns either a list or a particular one of the user's friends
 */
exports.list = function(req, res) {
	var arg = {};
	arg.owner = req.user._id;
	arg.approved = true;

	if (req.query.friend) {
		arg.user = new ObjectId(req.query.friend);
	}

	Friend.find(arg).exec(function(err, friends) {
		if (err) {
			res.status(400).send({message: errorHandler.getErrorMessage(err)});
		} else {
			res.json(friends);
		}
	});
};

exports.listWithUser = function(req, res) {
	var arg = {};
	arg.owner = req.user._id;
	arg.approved = true;

	if (req.query.friend) {
		arg.user = new ObjectId(req.query.friend);
	}

	Friend.find(arg).populate('user').exec(function(err, friends) {
		if (err) {
			res.status(400).send({message: errorHandler.getErrorMessage(err)});
		} else {
			res.json(friends);
		}
	});
};


/**
 * removes the friend from self and self from the friend
 */
exports.delete = function(req, res) {
	var owner = new ObjectId(req.body.owner); // remover
	var user = new ObjectId(req.body.user);

	Friend.findOne({owner: owner, user: user}).remove(function(err) {
		if (err) {
			res.status(400).send({message: errorHandler.getErrorMessage(err)});
		} else {

			Friend.findOne({owner: user, user: owner}).remove(function(err) {
				if (err) {
					res.status(400).send({message: errorHandler.getErrorMessage(err)});
				} else {
					User.findById(user).populate('device').exec(function(err, friendinfo) {
						if (err || !friendinfo) {
							logger.info(errorHandler.getErrorMessage(err));
						} else {
							logger.info('friend removed: %s from %s', friendinfo.name, req.user.name);
							if (friendinfo.device.deviceToken) {
								// sends the notification to the friend
								apn.sendFriendRemoval(friendinfo.device.deviceToken, owner);
							}
						}
						res.json({});
						//res.status(200).send();
					});
				}
			});
		}
	});
};


/**
 * invites a user
 */
exports.invite = function(req, res) {
	// invitor
	var invitorUserObj = req.user;
	var invitorUserId = req.user._id;
	var invitorDeviceToken = req.user.device.deviceToken;

	// invitee
	var inviteeUserId = new ObjectId(req.body.user);

	// data
	var range = req.body.range;
	var message = req.body.message;

	User.findById(inviteeUserId).populate('device').exec(function(err, inviteeUserObj) {

		if (err || !inviteeUserObj) {
			console.error('can\'t find the user object: %s', invitorUserObj.name);
			res.status(400).send({message: 'can\'t find the user object'});
			return;
		}

		var inviteeIsSimulator = inviteeUserObj.device.isSimulator;
		var inviteeDeviceToken = inviteeUserObj.device.deviceToken; // could be empty
		var invitorIsNode = (req.user.device.platform === 'node');

		// make sure that the user is not a friend of mine yet
		Friend.findOne({owner: invitorUserId, user: inviteeUserId}, function(err, invitorFriendObj) {
			if (err) {
				res.status(400).send({message: errorHandler.getErrorMessage(err)});
				return;
			}

			if (invitorFriendObj) {
				if (invitorFriendObj.approved) {
					res.status(400).send({message: 'Already being friend'});
					return;
				} else {
					// NOTE: invite resender will
					// be confused when the invite
					// is issued many times
					invitorFriendObj.remove();
				}
			}

			// create a Friend without approval with the owner as the invitor
			invitorFriendObj = new Friend();
			invitorFriendObj.owner = invitorUserId;
			invitorFriendObj.user = inviteeUserId;
			invitorFriendObj.range = range;
			invitorFriendObj.name = inviteeUserObj.name; // friend's name
			invitorFriendObj.gender = inviteeUserObj.gender; // friend's name
			invitorFriendObj.provider = inviteeUserObj.provider;
			if (typeof inviteeUserObj.providerData !== 'undefined' &&
			    typeof inviteeUserObj.providerData.id !== 'undefined') {
				console.log('invitee is a facebook user');
				invitorFriendObj.providerID = inviteeUserObj.providerData.id;
			} else {
				console.log('invitee is not a facebook user');
			}
			if (inviteeIsSimulator || invitorIsNode) {
				invitorFriendObj.approved = true;
			} else {
				invitorFriendObj.approved = false;
			}
			invitorFriendObj.save(function(err, invitorFriendObj) {
				if (err) {
					res.status(400).send({message: errorHandler.getErrorMessage(err)});
					return;
				}

				// immediately approves for the testing friends
				if (inviteeIsSimulator || invitorIsNode) {
					console.log('immediate approved');
					var inviteeFriendObj = new Friend();
					inviteeFriendObj.approved = true;
					inviteeFriendObj.owner = inviteeUserId;
					inviteeFriendObj.user = invitorUserId;
					inviteeFriendObj.range = range;
					inviteeFriendObj.name = invitorUserObj.name;
					inviteeFriendObj.gender = invitorUserObj.gender;
					inviteeFriendObj.provider = invitorUserObj.provider;
					if (typeof invitorUserObj.providerData !== 'undefined' &&
					    typeof invitorUserObj.providerData.id !== 'undefined') {
						inviteeFriendObj.providerID = invitorUserObj.providerData.id;
					}
					inviteeFriendObj.save(function(err, inviteeFriendObj) {
						if (err) {
							res.status(400).send({message: errorHandler.getErrorMessage(err)});
							return;
						}
						if (invitorDeviceToken) {
							apn.sendAckInvite(invitorDeviceToken, invitorUserId, inviteeUserId);
						}
						res.json({});
						//res.status(200).send();
					});
				} else {
					console.log('sending an invite');
					var count = 0;
					var uid = shortid.generate();

					// sends the notification to the device
					if (inviteeDeviceToken) {
						console.log('sending an invite to the invitee device');
						apn.sendInvite(inviteeDeviceToken, message, invitorUserObj.name, invitorFriendObj.id, range, uid);
						count++;
					} else {
						console.error('failed to send an invite to the invitee device due to no device token');
					}

					// schedule the reminder
					var data = {
						id: uid,
						friend: invitorFriendObj._id,
						user: inviteeUserId,
						name: invitorUserObj.name,
						message: message,
						count: count
					};
					agenda.schedule('1 day', 'invite resender', data);

					res.json({});
					//res.status(200).send();
				}
			});
		});
	});
};

/**
 * accepts the invite
 */
exports.accept = function(req, res) {
	var inviteeUserObj = req.user;
	var agendaDataId = req.body.id;
	var invitorFriendId = new ObjectId(req.body.friend);
	var range = req.body.range;

	function makeFriend(done) {

		// friend's owner is the invitor
		Friend.findById(invitorFriendId).populate('owner').exec(function(err, invitorFriendObj) {
			if (err || !invitorFriendObj) {
				done('can\'t find an invitor object');
				return;
			}

			Friend.populate(invitorFriendObj, {
				path: 'owner.device',
				model: 'Device'
			}, function(err, invitorFriendObj) {
				if (err || !invitorFriendObj) {
					done('can\'t find a device object');
					return;
				}

				var inviteeUserId = invitorFriendObj.user;
				var inviteeDeviceToken = inviteeUserObj.device.deviceToken;
				var inviteeName = inviteeUserObj.name;
				var invitorUserId = invitorFriendObj.owner._id;
				var invitorName = invitorFriendObj.owner.name;
				var invitorGender = invitorFriendObj.owner.gender;
				var invitorIsSimulator = invitorFriendObj.owner.device.isSimulator;
				var invitorDeviceToken = invitorFriendObj.owner.device.deviceToken;
				var invitorProvider = invitorFriendObj.owner.provider;
				var invitorProviderID = '';
				if (typeof invitorFriendObj.owner.providerData !== 'undefined' &&
				    typeof invitorFriendObj.owner.providerData.id !== 'undefined') {
					invitorProviderID = invitorFriendObj.owner.providerData.id;
				}

				invitorFriendObj.range = range;
				invitorFriendObj.approved = true;
				invitorFriendObj.save(function(err, invitorFriendObj) {
					if (err) {
						done(errorHandler.getErrorMessage(err));
						return;
					}

					// create a Friend for myself
					var inviteeFriendObj = new Friend();
					inviteeFriendObj.approved = true;
					inviteeFriendObj.owner = inviteeUserId;
					inviteeFriendObj.user = invitorUserId;
					inviteeFriendObj.range = range;
					inviteeFriendObj.name = invitorName;
					inviteeFriendObj.gender = invitorGender;
					inviteeFriendObj.provider = invitorProvider;
					inviteeFriendObj.providerID = invitorProviderID;
					inviteeFriendObj.save(function(err, inviteeFriendObj) {
						if (err) {
							done(errorHandler.getErrorMessage(err));
							return;
						}

						if (invitorDeviceToken) {
							apn.sendAckInvite(invitorDeviceToken, invitorUserId, inviteeUserId);
						}

						if (inviteeDeviceToken) {
							apn.sendAckInvite(inviteeDeviceToken, invitorUserId, inviteeUserId);
						}

						done();
					});
				});
			});
		});
	}

	// remove the resend job
	agenda.jobs({name: 'invite resender', 'data.id': agendaDataId}, function(err, jobs) {
		if (err || !jobs) {
			res.status(400).send({message: 'can\'t find the invite request'});
			return;
		}
		var doOnce = true;
		jobs.forEach(function(job) {
			job.remove(function(err) {
				if (err) {
					res.status(400).send({message: 'can\'t remove the range request job'});
					return;
				}
				if (doOnce) {
					doOnce = false;
					makeFriend(function(err) {
						if (err) {
							res.status(400).send({message: err});
						} else {
							res.json({});
						}
					});
				}
			});
		});
	});
};

/**
 * declines the invite
 */
exports.decline = function(req, res) {
	var agendaDataId = req.body.id;
	// remove the resend job
	agenda.jobs({name: 'invite resender', 'data.id': agendaDataId}, function(err, jobs) {
		if (err || !jobs) {
			res.status(400).send({message: 'can\'t find the invite request'});
			return;
		}
		jobs.forEach(function(job) {
			job.remove(function(err) {
				res.json({});
			});
		});
	});
/*
	var invitorFriendId = new ObjectId(req.body.friend);

	Friend.findById(invitorFriendId).remove(function(err) {
		if (err) {
			res.status(400).send({message: errorHandler.getErrorMessage(err)});
		} else {
			res.json({});
			//res.status(200).send();
		}
	});
*/
};

/**
 * update the friend object, if the range is changed, send a confirmation to the friend
 */
exports.update = function(req, res) {
	var requestorUserId = req.user._id;
	var requestorUserObj = req.user;
	var requesteeUserId = new ObjectId(req.body.user);

	Friend.findOne({owner: requestorUserId, user: requesteeUserId}, function(err, requestorFriendObj) {
		if (err || !requestorFriendObj) {
			res.status(400).send({message: 'can\'t find the friend object'});
			return;
		}

		User.findById(requesteeUserId).populate('device').exec(function(err, requesteeUserObj) {
			if (err || !requesteeUserObj) {
				res.status(400).send({message: 'can\'t find the user object'});
				return;
			}

			var notifyRangeChangeRequest = -1;
			var notifyRangeHasChanged = false;

			// Note that the location range change is
			// special.  If the range is less than the
			// current one, send a request for the friend
			// to agreee the change.
			if (typeof req.body.range !== undefined) {
				if (req.body.range < requestorFriendObj.range) {
					if (requesteeUserObj.device.isSimulator) {
						notifyRangeHasChanged = true;
					} else {
						notifyRangeChangeRequest = req.body.range;
						req.body.range = requestorFriendObj.range;
					}
				} else if (req.body.range !== requestorFriendObj.range) {
					notifyRangeHasChanged = true;
				}
			}

			requestorFriendObj = _.extend(requestorFriendObj, req.body);
			requestorFriendObj.save(function(err) {
				if (err) {
					res.status(400).send({message: errorHandler.getErrorMessage(err)});
					return;
				}

				// send a request for the range change to the user
				if (notifyRangeChangeRequest >= 0) {
					var count = 0;

					var uid = shortid.generate();

					if (requesteeUserObj.device.deviceToken) {
						apn.sendRangeChangeRequest(requesteeUserObj.device.deviceToken,
									   requestorUserObj.name,
									   requestorUserId, notifyRangeChangeRequest, uid);
						count++;
					}

					// schedule the reminder
					var data = {
						id: uid,
						name: requestorUserObj.name,
						user: requesteeUserId,
						friend: requestorFriendObj._id,
						range: notifyRangeChangeRequest,
						count: count,
					};


					agenda.schedule('1 day', 'range request resender', data);

					res.json({});
					//res.status(200).send();

				} else if (notifyRangeHasChanged) {

					Friend.findOne({owner: requesteeUserId, user: requestorUserId}, function(err, requesteeFriendObj) {
						if (err || !requesteeFriendObj) {
							res.status(400).send({message: 'can\'t find the friend object'});
							return;
						}

						requesteeFriendObj.range = req.body.range;
						requesteeFriendObj.save(function(err) {
							if (err) {
								res.status(400).send({message: errorHandler.getErrorMessage(err)});
								return;
							}
							if (requesteeUserObj.device.deviceToken) {
								logger.info('sending apn to the requestee of the range change');
								apn.sendRangeHasChanged(requesteeUserObj.device.deviceToken,
											requestorUserId,
											req.body.range);
							}
							if (requestorUserObj.device.deviceToken) {
								logger.info('sending apn to the requestor of the range change');
								apn.sendRangeHasChanged(requestorUserObj.device.deviceToken,
											requesteeUserId,
											req.body.range);
							}
							res.json({});
							//res.status(200).send();
						});
					});
				} else {
					res.json({});
					//res.status(200).send();
				}
			});
		});
	});
};

/**
 * accepts the range change
 */
exports.acceptRangeChange = function(req, res) {
	var requestorUserId = new ObjectId(req.body.user);
	var agendaDataId = req.body.id;
	var requesteeUserId = req.user._id;
	var range = req.body.range;

	function updateLocationRange(done) {
		Friend.findOne({owner: requesteeUserId, user: requestorUserId}, function(err, requesteeFriendObj) {
			if (err || !requesteeFriendObj) {
				done('can\'t find the friend object');
				return;
			}

			requesteeFriendObj.range = range;
			requesteeFriendObj.save(function(err, requesteeFriendObj) {
				if (err) {
					done(errorHandler.getErrorMessage(err));
					return;
				}

				Friend.findOne({owner: requestorUserId, user: requesteeUserId}, function(err, requestorFriendObj) {
					if (err) {
						done(errorHandler.getErrorMessage(err));
						return;
					}
					requestorFriendObj.range = range;
					requestorFriendObj.save(function(err, requestorFriendObj) {
						if (err) {
							done(errorHandler.getErrorMessage(err));
							return;
						}

						User.findById(requestorUserId).populate('device').exec(function(err, requestorUserObj) {
							if (err || !requestorUserObj) {
								done('can\'t find the user object');
								return;
							}
							if (requestorUserObj.device.deviceToken) {
								apn.sendRangeHasChanged(requestorUserObj.device.deviceToken, requesteeUserId, range);
							}
							done(null);
						});
					});
				});
			});
		});
	}

	// remove the resend job
	agenda.jobs({name: 'range request resender', 'data.id': agendaDataId}, function(err, jobs) {
		if (err || !jobs) {
			res.status(400).send({message: 'can\'t find the range request job'});
			return;
		}
		var doOnce = true;
		jobs.forEach(function(job) {
			job.remove(function(err) {
				if (err) {
					res.status(400).send({message: 'can\'t remove the range request job'});
					return;
				}
				if (doOnce) {
					doOnce = false;
					updateLocationRange(function(err) {
						if (err) {
							res.status(400).send({message: err});
						} else {
							res.json({});
						}
					});
				}
			});
		});
	});
};

/**
 * Declines the invite
 */
exports.declineRangeChange = function(req, res) {
	var requestorUserId = new ObjectId(req.body.user);
	var agendaDataId = req.body.id;
	var requesteeUserId = req.user._id;

	// remove the resend job
	agenda.jobs({name: 'range request resender', 'data.id': agendaDataId}, function(err, jobs) {
		if (err || !jobs) {
			res.status(400).send({message: 'can\'t find the range request'});
			return;
		}
		jobs.forEach(function(job) {
			job.remove(function(err) {
				if(!err) console.log('Successfully removed job from collection');
				res.json({});
			});
		});
	});

	/*
	Friend.findOne({owner: requestorUserId, user: requesteeUserId}, function(err, requestorFriendObj) {
		if (err || !requestorFriendObj) {
			res.status(400).send({message: 'can\'t find the friend object'});
			return;
		}

		requestorFriendObj.cancelRangeRequest = true;
		requestorFriendObj.save(function(err) {
			res.json({});
			//res.status(200).send();
		});
	});
	*/
};

exports.read = function(req, res) {
	res.json(req.friend);
};

exports.friendByID = function(req, res, next, id) {
	Friend.findById(id).populate('user').exec(function(err, friend) {
		if (err) return next(err);
		if (!friend) return next(new Error('Failed to load friend ' + id));
		req.friend = friend;
		next();
	});
};


/**
 * reschedule all the agenda jobs upon login
 */
exports.rescheduleJobs = function (user) {
	var runat = 0;

	async.series([
		function(callback){
			agenda.jobs({name: 'range request resender', 'data.user': user._id}, function(err, jobs) {
				if (err || !jobs) {
					callback(null, 'range');
					return;
				}
				var count = 0;
				jobs.forEach(function(job) {
					runat += 30;
					console.log('reschedule range request: %s', job.attrs.data.name);
					job.schedule(runat + ' seconds');
					job.save();
					if (++count === jobs.length) {
						callback(null, 'range');
					}
				});
			});
		},
		function(callback){
			agenda.jobs({name: 'invite resender', 'data.user': user._id}, function(err, jobs) {
				if (err || !jobs) {
					callback(null, 'invite');
					return;
				}
				var count = 0;
				jobs.forEach(function(job) {
					runat += 30;
					console.log('reschedule invite request: %s', job.attrs.data.name);
					job.schedule(runat + ' seconds');
					job.save();
					if (++count === jobs.length) {
						callback(null, 'invite');
					}
				});
			});
		}
	], function(err, results){
		// results is now equal to ['one', 'two']
	});
};

exports.ping = function(req, res) {
	var arg = {};
	arg.owner = req.user._id;
	arg.user = new ObjectId(req.body.user);
	arg.approved = true;

	Friend.findOne(arg, function(err, friendObj) {
		if (err || !friendObj) {
			res.status(400).send({message: 'can\'t find the friend object'});
			return;
		}

		User.findById(friendObj.user).populate('device').exec(function(err, friendUserObj) {
			if (err || !friendUserObj) {
				res.status(400).send({message: 'can\'t find the user object'});
				return;
			}

			if (friendUserObj.device && friendUserObj.device.deviceToken) {
				apn.sendPing(friendUserObj.device.deviceToken, friendObj.owner);
			}

			res.json({});
		});
	});
};

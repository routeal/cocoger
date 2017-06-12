'use strict';

/**
 * Module dependencies.
 */
var _ = require('lodash'),
    config = require('../../../config/config'),
    jwt = require('jsonwebtoken'),
    errorHandler = require('../errors.server.controller'),
    friends = require('../friends.server.controller'),
    mongoose = require('mongoose'),
    passport = require('passport'),
    Device = mongoose.model('Device'),
    User = mongoose.model('User'),
    agenda = require('../../../config/agenda'),
    Friend = mongoose.model('Friend'),
    apn = require('../../utils/apn.wrapper'),
    ObjectId = require('mongoose').Types.ObjectId,
    logger = require('winston');

/**
 * Web Signup - creates the user only
 */
exports.signup = function(req, res) {
	// For security measurement we remove the roles from the req.body object
	delete req.body.roles;

	// Init Variables
	var user = new User(req.body);

	// Add missing user fields
	user.provider = 'local';

	// Then save the user
	user.save(function(err) {
		if (err) {
			res.status(400).send({message: errorHandler.getErrorMessage(err)});
		} else {
			// Remove sensitive data before login
			user.password = undefined;
			user.salt = undefined;

			req.login(user, function(err) {
				if (err) {
					res.status(400).send({message: errorHandler.getErrorMessage(err)});
				} else {
					res.json(user);
				}
			});
		}
	});
};

/**
 * Mobile Signup - creates the user only
 */
exports.signupToken = function(req, res) {
	// For security measurement we remove the roles from the req.body object
	delete req.body.roles;

	// Init Variables
	var user = new User(req.body);

	// Add missing user fields
	user.provider = 'local';

	// Then save the user
	user.save(function(err, user) {
		if (err) {
			res.status(400).send({message: errorHandler.getErrorMessage(err)});
			return;
		}

		var device = new Device(req.body.device_content);

		device.owner = user._id;

		device.save(function(err, device) {
			if (err) {
				res.status(400).send({message: errorHandler.getErrorMessage(err)});
				return;
			}

			user.device = device._id;
			user.save(function(err) {
				if (err) {
					res.status(400).send({message: errorHandler.getErrorMessage(err)});
					return;
				}

				// Remove sensitive data before login
				user.password = undefined;
				user.salt = undefined;

				req.login(user, function(err) {
					if (err) {
						res.status(400).send({message: errorHandler.getErrorMessage(err)});
						return;
					}

					var json = user.toJSON();
					json.authToken = jwt.sign(user, config.jwtSecret);
					json.locationRange = config.getLocationRange(device.country);
					//res.status(200).send(json);
					res.json(json);
				});
			});
		});
	});
};

/**
 * Web Signin after passport authentication
 */
exports.signin = function(req, res, next) {
	passport.authenticate('local', {session: false}, function(err, user, info) {
		if (err || !user) {
			res.status(400).send({message: info});
		} else {
			// Remove sensitive data before login
			user.password = undefined;
			user.salt = undefined;

			req.login(user, function(err) {
				if (err) {
					res.status(400).send({message: errorHandler.getErrorMessage(err)});
				} else {
					res.json(user);
				}
			});
		}
	})(req, res, next);
};

/**
 * Mobile Signin after passport authentication
 */
exports.signinToken = function(req, res, next) {
	passport.authenticate('local', {session: false}, function(err, user, info) {
		if (err || !user) {
			res.status(404).send({message: info});
		} else {
			Device.findOne({'owner': user._id}, function(err, device) {
				if (err) {
					res.status(400).send({message: errorHandler.getErrorMessage(err)});
					return;
				}

				if (!device) {
					device = new Device(req.body.device_content);
					device.owner = user._id;
				} else {
					device = _.extend(device, req.body.device_content);
				}

				device.save(function(err, device) {

					user.device = device._id;
					user.updated = Date.now();

					user.save(function(err) {
						if (err) {
							res.status(400).send({message: errorHandler.getErrorMessage(err)});
						} else {
							// Remove sensitive data before login
							user.password = undefined;
							user.salt = undefined;

							req.login(user, function(err) {
								if (err) {
									res.status(400).send({message: errorHandler.getErrorMessage(err)});
								} else {
									var json = user.toJSON();
									json.authToken = jwt.sign(user, config.jwtSecret);
									json.locationRange = config.getLocationRange(device.country);
									//res.status(200).send(json);
									res.json(json);
									// reschedule the agenda jobs
									friends.rescheduleJobs(user);

									// notify user not available
									var arg = {owner: user._id, approved: true};
									Friend.find(arg, function(err, friends) {
										if (err) {
											res.status(400).send({message: errorHandler.getErrorMessage(err)});
										} else {
											friends.forEach(function(friend) {
												User.findById(friend.user).populate('device').exec(function(err, fuser) {
													if (fuser.device.deviceToken) {
														apn.sendFriendIn(fuser.device.deviceToken, user.id);
													}
												});
											});
										}
									});

								}
							});
						}
					});
				});
			});
		}
	})(req, res, next);
};

/**
 * Web Signout
 */
exports.signout = function(req, res) {
	req.logout();
	res.redirect('/');
};

/**
 * Mobile Signout
 */
exports.signoutToken = function(req, res) {
	var userid = req.user.id; // keep it as a string
	Device.findById(req.user.device._id, function(err, device) {
		if (err || !device) {
			res.status(400).send({message: 'device not found'});
		} else {
			logger.info('sign out: %s', req.user.name);
			device.deviceToken = '';
			device.save(function(err) {
				if (err) {
					// continue
					logger.info(errorHandler.getErrorMessage(err));
				}

				req.logout();
				res.json({});

				// notify user not available
				var arg = {owner: new ObjectId(userid), approved: true};
				Friend.find(arg, function(err, friends) {
					if (err) {
						res.status(400).send({message: errorHandler.getErrorMessage(err)});
					} else {
						friends.forEach(function(friend) {
							User.findById(friend.user).populate('device').exec(function(err, fuser) {
								if (fuser.device.deviceToken) {
									apn.sendFriendOut(fuser.device.deviceToken, userid);
								}
							});
						});
					}
				});

			});
		}
	});
};

/**
 * OAuth callback used for Web login
 */
exports.oauthCallback = function(strategy) {
	return function(req, res, next) {
		passport.authenticate(strategy, function(err, user, redirectURL) {
			if (err || !user) {
				return res.redirect('/#!/signin');
			}
			req.login(user, function(err) {
				if (err) {
					return res.redirect('/#!/signin');
				}
				return res.redirect(redirectURL || '/');
			});
		})(req, res, next);
	};
};

/**
 * Mobile OAuth with the provider's token
 */
exports.oauthToken = function(strategy) {
	return function(req, res, next) {
		passport.authenticate(strategy, {session:false}, function(err, user) {
			if (err || !user) {
				res.status(400).send({message: errorHandler.getErrorMessage(err)});
			} else {
				Device.findOne({'owner': user._id}, function(err, device) {
					if (err) {
						res.status(400).send({message: errorHandler.getErrorMessage(err)});
						return;
					}
					if (!device) {
						device = new Device(req.body.device_content);
						device.owner = user._id;
					} else {
						device = _.extend(device, req.body.device_content);
					}

					device.save(function(err, device) {
						user.device = device._id;
						user.updated = Date.now();
						user.save(function(err) {
							if (err) {
								res.status(400).send({message: errorHandler.getErrorMessage(err)});
							} else {
								req.login(user, function(err) {
									if (err) {
										res.status(400).send({message: errorHandler.getErrorMessage(err)});
									} else {
										var json = user.toJSON();
										json.authToken = jwt.sign(user, config.jwtSecret);
										json.locationRange = config.getLocationRange(device.country);
										//res.status(200).send(json);
										res.json(json);
										// reschedule the agenda jobs
										friends.rescheduleJobs(user);

										// notify user not available
										var arg = {owner: user._id, approved: true};
										Friend.find(arg, function(err, friends) {
											if (err) {
												res.status(400).send({message: errorHandler.getErrorMessage(err)});
											} else {
												friends.forEach(function(friend) {
													User.findById(friend.user).populate('device').exec(function(err, fuser) {
														if (fuser.device.deviceToken) {
															apn.sendFriendIn(fuser.device.deviceToken, user.id);
														}
													});
												});
											}
										});

									}
								});
							}
						});
					});
				});
			}
		})(req, res, next);
	};
};

/**
 * Helper function to save or update a OAuth user profile
 */
exports.saveOAuthUserProfile = function(req, providerUserProfile, done) {
	if (!req || !req.user) {
		// Define a search query fields
		var searchMainProviderIdentifierField = 'providerData.' + providerUserProfile.providerIdentifierField;
		var searchAdditionalProviderIdentifierField = 'additionalProvidersData.' + providerUserProfile.provider + '.' + providerUserProfile.providerIdentifierField;

		// Define main provider search query
		var mainProviderSearchQuery = {};
		mainProviderSearchQuery.provider = providerUserProfile.provider;
		mainProviderSearchQuery[searchMainProviderIdentifierField] = providerUserProfile.providerData[providerUserProfile.providerIdentifierField];

		// Define additional provider search query
		var additionalProviderSearchQuery = {};
		additionalProviderSearchQuery[searchAdditionalProviderIdentifierField] = providerUserProfile.providerData[providerUserProfile.providerIdentifierField];

		// Define a search query to find existing user with current provider profile
		var searchQuery = {
			$or: [mainProviderSearchQuery, additionalProviderSearchQuery]
		};

		User.findOne(searchQuery, function(err, user) {
			if (err) {
				console.log('user search error: %s', err);
				done(err);
			} else {
				if (!user) {
					var userEmail = providerUserProfile.email;

					User.findOne({email: userEmail, active: true}, function(err, localuser) {
						// user's previous provider is different from the current one
						if (localuser) {
							// update the provider
							localuser.provider = providerUserProfile.provider;
							localuser.providerData = providerUserProfile.providerData;
							localuser.providerIdentifierField = providerUserProfile.providerIdentifierField;
							// And save the user
							localuser.save(function(err) {
								done(err, localuser);
							});
						} else {
							var gender = (providerUserProfile.gender === 'female') ? 1 : 0;
							user = new User({
								name: providerUserProfile.name,
								email: providerUserProfile.email,
								gender: gender,
								provider: providerUserProfile.provider,
								providerData: providerUserProfile.providerData,
								providerIdentifierField: providerUserProfile.providerIdentifierField,
							});

							// And save the user
							user.save(function(err) {
								done(err, user);
							});
						}
					});
				} else {
					// user found in the database
					done(err, user);
				}
			}
		});
	} else {
		// User is already logged in, join the provider data to the existing user
		var user = req.user;

		// Check if user exists, is not signed in using this provider, and doesn't have that provider data already configured
		if (user.provider !== providerUserProfile.provider && (!user.additionalProvidersData || !user.additionalProvidersData[providerUserProfile.provider])) {
			// Add the provider data to the additional provider data field
			if (!user.additionalProvidersData) user.additionalProvidersData = {};
			user.additionalProvidersData[providerUserProfile.provider] = providerUserProfile.providerData;

			// Then tell mongoose that we've updated the additionalProvidersData field
			user.markModified('additionalProvidersData');

			// And save the user
			user.save(function(err) {
				return done(err, user, '/#!/settings/accounts');
			});
		} else {
			return done(new Error('User is already connected using this provider'), user);
		}
	}
};

/**
 * Remove OAuth provider
 */
exports.removeOAuthProvider = function(req, res, next) {
	var user = req.user;
	var provider = req.param('provider');

	if (user && provider) {
		// Delete the additional provider
		if (user.additionalProvidersData[provider]) {
			delete user.additionalProvidersData[provider];

			// Then tell mongoose that we've updated the additionalProvidersData field
			user.markModified('additionalProvidersData');
		}

		user.save(function(err) {
			if (err) {
				return res.status(400).send(errorHandler.getErrorMessage(err));
			} else {
				req.login(user, function(err) {
					if (err) {
						res.status(400).send(err);
					} else {
						res.json(user);
					}
				});
			}
		});
	}
};

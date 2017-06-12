'use strict';

var apn = require('apn');
var logger = require('winston');


var pushNotification = function (token, content, payload, sound, key, args) {
	var options = {
		cert		: './config/apncerts/dev/cert.pem',
		key		: './config/apncerts/dev/key.pem',
		production	: false
	};
	var service = new apn.Connection(options);
	var note = new apn.Notification();
	if (content) {
		note.setContentAvailable(true);
	}
 	if (key) {
		note.setLocKey(key);
	}
 	if (args) {
		note.setLocArgs(args);
	}
	if (sound) {
		note.sound = 'default';
	}
	if (payload) {
		note.payload = payload;
	}
	console.log(note);
	service.pushNotification(note, apn.Device(token));
	return service;
};


exports.sendInvite = function (token, message, name, friend, range, id) {
	var payload = {
		category: 'Invite',
		aps: {
			id: id,
			message: message,
			from: name,
			friend: friend,
			range: range,
		}
	};
	pushNotification(token, false, payload, true, 'Friend Request from %@', [name]);
};

exports.sendAckInvite = function (token, owner, user) {
	var payload = {
		category: 'AckInvite',
		aps: {
			owner: owner,
			user: user,
		}
	};
	pushNotification(token, true, payload);
};

exports.sendFriendRemoval = function (token, user) {
	var payload = {
		category: 'FriendRemoval',
		aps: {
			user: user,
		}
	};
	pushNotification(token, true, payload);
};

exports.sendFriendOut = function (token, user) {
	var payload = {
		category: 'FriendOut',
		aps: {
			user: user,
		}
	};
	pushNotification(token, true, payload);
};

exports.sendFriendIn = function (token, user) {
	var payload = {
		category: 'FriendIn',
		aps: {
			user: user,
		}
	};
	pushNotification(token, true, payload);
};

exports.sendRangeChangeRequest = function (token, name, user, range, id) {
	var payload = {
		category: 'RangeChangeRequest',
		aps: {
			from: name,
			user: user,
			range: range,
			id: id,
		}
	};
	pushNotification(token, false, payload, true, 'Location range request from %@', [name]);
};

exports.sendRangeHasChanged = function (token, user, range) {
	var payload = {
		category: 'RangeChanged',
		aps: {
			user: user,
			range: range,
		}
	};
	pushNotification(token, true, payload);
};

exports.sendMovedIn = function (token, user, name, place) {
	var payload = {
		category: 'MovedIn',
		aps: {
			user: user,
		}
	};
	pushNotification(token, false, payload, true, '%@ entered in %@', [name, place]);
};

exports.sendMove = function (token, user) {
	var payload = {
		category: 'Move',
		aps: {
			user: user
		}
	};
	pushNotification(token, true, payload);
};

exports.sendPing = function (token, user) {
	var payload = {
		category: 'Ping',
		aps: {
			user: user
		}
	};
	pushNotification(token, true, payload);
};

exports.sendMessage = function (token, message) {
	var payload = {
		category: 'Message',
		aps: {
			message: message,
		}
	};
	pushNotification(token, false, payload, true, 'Important message to you');
};

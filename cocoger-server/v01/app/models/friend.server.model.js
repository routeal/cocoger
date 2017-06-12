'use strict';

/**
 * Module dependencies.
 */
var mongoose = require('mongoose'),
    Schema = mongoose.Schema;

/**
 * Friend Schema
 */
var FriendSchema = new Schema({
	approved: {
		type: Boolean,
		default: false
	},
	owner: {
		type: Schema.ObjectId,
		ref: 'User'
	},
	user: {
		type: Schema.ObjectId,
		ref: 'User'
	},
	range: {
		type: Number,
		default: 0
	},
	name: {
		type: String,
		default: '',
		trim: true
	},
	gender: {
		type: Number,
		default: 0,
	},
	photoName: {
		type: String,
		default: 'baby'
	},
	photoType: {
		type: Number,
		default: 0
	},
	created: {
		type: Date,
		default: Date.now
	},
	provider: {
		type: String,
		default: ''
	},
	providerID: {
		type: String,
		default: ''
	},
});

FriendSchema.set('toJSON', {
	transform: function(doc, ret, options) {
		var retJson = {
			user: ret.user,
			range: ret.range,
			name: ret.name,
			gender: ret.gender,
			photoName: ret.photoName,
			photoType: ret.photoType,
			created: ret.created,
			provider: ret.provider,
			providerID: ret.providerID
		};
		return retJson;
	}
});

mongoose.model('Friend', FriendSchema);

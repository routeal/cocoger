'use strict';

/**
 * Module dependencies.
 */
var mongoose = require('mongoose'),
	Schema = mongoose.Schema;

/**
 * Location Schema  -  should use GeoJSON????
 */
var LocationSchema = new Schema({
	coordinates	: {
		type	: [Number],
		index	: '2dsphere'
	},
	accuracy	: Number,
	altitude	: Number,
	speed		: Number,
	type		: Number,
	timezone	: {
		type	: String,
		default : 'Asia/Tokyo',
	},
	// Integer value representing the number of milliseconds since
	// 1 January 1970 00:00:00 UTC (Unix Epoch).
	created		: {
		type	: Date,
		default : Date.now
	},
	user		: {
		type	: Schema.ObjectId,
		ref	: 'User'
	},
	device		: {
		type	: Schema.ObjectId,
		ref	: 'Device'
	},
	zip		: {
		type	: String,
		default : '',
	},
	country		: {
		type	: String,
		default : '',
	},
	state		: {
		type	: String,
		default : '',
	},
	county		: {
		type	: String,
		default : '',
	},
	city		: {
		type	: String,
		default : '',
	},
	town		: {
		type	: String,
		default : '',
	},
	street		: {
		type	: String,
		default : '',
	}
});

LocationSchema.set('toJSON', {
	transform: function(doc, ret, options) {
		var retJson = {
			latitude	: ret.coordinates[1],
			longitude	: ret.coordinates[0],
			altitude	: ret.altitude,
			speed		: ret.speed,
			accuracy	: ret.accuracy,
			type		: ret.type,
			timezone	: ret.timezone,
			created		: ret.created,
			user		: ret.user,
			device		: ret.device,
			zip		: ret.zip,
			country		: ret.country,
			state		: ret.state,
			county		: ret.county,
			city		: ret.city,
			town		: ret.town,
			street		: ret.street,
		};
		return retJson;
	}
});

mongoose.model('Location', LocationSchema);

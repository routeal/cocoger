'use strict';

/**
 * Module dependencies.
 */
var mongoose = require('mongoose'),
    Schema = mongoose.Schema;

/**
 * Image Schema
 */
var ImageSchema = new Schema({
	user: {
		type: Schema.ObjectId,
		ref: 'User'
	},
	type: {
		type: Number,
		default: 1
	},
	name: {
		type: String,
		default: '',
	},
	url: {
		type: String,
		default: ''
	},
	data: {
		// in base64
		type: String,
	},
	created: {
		type: Date,
		default: Date.now
	},
});

/*
ImageSchema.pre('save', function(next) {
	if (this.data) {
		this.data = new Buffer(this.data, 'base64')
	}
	next();
});
*/

mongoose.model('Image', ImageSchema);

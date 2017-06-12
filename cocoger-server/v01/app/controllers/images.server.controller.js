'use strict';

/**
 * Module dependencies.
 */
var mongoose = require('mongoose'),
    errorHandler = require('./errors.server.controller'),
    User = mongoose.model('User'),
    IconImage = mongoose.model('Image'),
    ObjectId = require('mongoose').Types.ObjectId,
    config = require('../../config/config'),
    logger = require('winston'),
    _ = require('lodash');

var list = function(req, res) {

        var assetImages =
		[
			{'type':1, 'name':'alien'},
			{'type':1, 'name':'baby'},
			{'type':1, 'name':'bone'},
			{'type':1, 'name':'boy1'},
			{'type':1, 'name':'boy2'},
			{'type':1, 'name':'cat'},
			{'type':1, 'name':'cattle'},
			{'type':1, 'name':'dog'},
			{'type':1, 'name':'dog2'},
			{'type':1, 'name':'footpad'},
			{'type':1, 'name':'fox'},
			{'type':1, 'name':'frog'},
			{'type':1, 'name':'girl1'},
			{'type':1, 'name':'girl2'},
			{'type':1, 'name':'hippo'},
			{'type':1, 'name':'horse'},
			{'type':1, 'name':'jack'},
			{'type':1, 'name':'jelly'},
			{'type':1, 'name':'kyoto'},
			{'type':1, 'name':'lacoon'},
			{'type':1, 'name':'liberty'},
			{'type':1, 'name':'love'},
			{'type':1, 'name':'mouse'},
			{'type':1, 'name':'mummy'},
			{'type':1, 'name':'owl'},
			{'type':1, 'name':'panty'},
			{'type':1, 'name':'penguin'},
			{'type':1, 'name':'pyramid'},
			{'type':8, 'name':'tgirl1'},
			{'type':8, 'name':'tgirl2'},
			{'type':8, 'name':'tgirl3'},
			{'type':8, 'name':'tgirl4'},
			{'type':8, 'name':'tgirl5'},
			{'type':8, 'name':'tgirl6'},
			{'type':8, 'name':'tgirl7'},
			{'type':8, 'name':'tgirl8'},
			{'type':8, 'name':'tguy1'},
			{'type':8, 'name':'tguy2'},
			{'type':8, 'name':'tguy3'},
			{'type':8, 'name':'tguy4'},
			{'type':8, 'name':'tguy5'},
			{'type':8, 'name':'tguy6'},
			{'type':8, 'name':'tguy7'},
			{'type':8, 'name':'tguy8'},
		];

	IconImage.find({user: req.user.id}, function(err, images) {
		if (err) {
			// not important
			logger.info(errorHandler.getErrorMessage(err).message);
		}
		if (images) {
			assetImages.forEach(function(image) {
				image.url = 'http://' + config.domain + '/images/' + image.name + '.png';
			});

			images.forEach(function(image) {
				// will not send the data
				image.data = null;
				assetImages.push(image);
			});
		}
		res.json(assetImages);
	});
};

exports.get = function(req, res) {
	if (req.query.user && req.query.name) {
		var user = new ObjectId(req.query.user);
		IconImage.findOne({user: user, name: req.query.name}, function(err, image) {
			if (err) {
				res.status(400).send({message: errorHandler.getErrorMessage(err)});
			} else {
				res.json(image);
			}
		});
	} else {
		list(req, res);
	}
};


exports.upload = function(req, res) {
	// NOTE: image is saved as a bas64 string
	var image = new IconImage(req.body);
	image.save(function(err) {
		if (err) {
			res.status(400).send({message: errorHandler.getErrorMessage(err)});
		} else {
			res.json({});
			//res.status(200).send();
		}
	});
};


exports.delete = function(req, res) {
	var user = new ObjectId(req.body.user);
	IconImage.findOne({user: user, name: req.body.name}, function(err, image) {
		if (err || !image) {
			if (err) {
				res.status(400).send({message: errorHandler.getErrorMessage(err)});
			} else {
				res.status(400).send({message: 'message not found'});
			}
		} else {
			image.remove(function(err) {
				if (err) {
					res.status(400).send({message: errorHandler.getErrorMessage(err)});
				} else {
					logger.info('image removed: %s %s', req.user.name, req.body.name);
					res.json({});
					//res.status(200).send();
				}
			});
		}
	});
};

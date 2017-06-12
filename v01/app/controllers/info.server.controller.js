'use strict';

/**
 * Module dependencies.
 */
var mongoose = require('mongoose'),
    errorHandler = require('./errors.server.controller'),
    config = require('../../config/config'),
    logger = require('winston'),
    _ = require('lodash');


exports.about = function(req, res) {
	res.redirect('/#!/about/' + req.localeId);
};

exports.faq = function(req, res) {
	res.redirect('/#!/faq/' + req.localeId);
};

exports.term = function(req, res) {
	res.redirect('/#!/term/' + req.localeId);
};

exports.privacy = function(req, res) {
	res.redirect('/#!/privacy/' + req.localeId);
};

exports.forgot = function(req, res) {
	res.redirect('/#!/password/forgot');
};

exports.byLocaleID = function(req, res, next, id) {
	req.localeId = id;
	next();
};

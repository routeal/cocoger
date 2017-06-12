'use strict';

var Agenda = require('agenda'),
    config = require('./config'),
    path = require('path');

var connectionOpts = {
	db: {
		address: config.db.address,
		collection: config.db.collection.agenda
	}
};

var agenda = new Agenda(connectionOpts);

config.getGlobbedFiles('./app/jobs/*.js').forEach(function(jobPath) {
	require(path.resolve(jobPath))(agenda);
});

agenda.on('ready', function() {
  agenda.start();
});

module.exports = agenda;

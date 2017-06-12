var http	= require('http');
var server  	= http.createServer();
var apn    	= require('apn');

// SILENT PUSH is not delivered when the app is in background

var pushNotification = function (token, payload) {
	var options = {
		production	: false
	}
	var service = new apn.Connection(options);
	var note = new apn.Notification();
	note.payload = payload;
	note.contentAvailable = 1;
	note.priority = 10;
	note.sound = '';
	service.pushNotification(note, apn.Device(token));
	return service;
};

server.on('request', function(req, res) {

	//service.pushNotification(note, myDevice);

	var payload = {
		aps: {
			category: "Message",
			alert: "test",
			contentAvailable: 1
		}
	};
	pushNotification("6cc5c447368db0371552e5bde50de124f2930a5716133b218a3b7569794b2097", payload);

    res.writeHead(200, {'Content-Type': 'text/html'});
    res.write('apn sent');
    res.end();
});

server.listen(9182, '127.0.0.1');
console.log('start server');

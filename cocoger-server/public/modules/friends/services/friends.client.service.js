'use strict';

//Friends service used for communicating with the friends REST endpoints
angular.module('friends').factory('Friends', ['$resource',
	function($resource) {
		return $resource('friends/:friendId', {
			friendId: '@_id'
		}, {
			update: {
				method: 'PUT'
			}
		});
	}
]);

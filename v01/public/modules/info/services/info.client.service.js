'use strict';

//Info service used for communicating with the info REST endpoints
angular.module('info').factory('Info', ['$resource',
	function($resource) {
		return $resource('info/:infoId', {
			infoId: '@_id'
		}, {
			update: {
				method: 'PUT'
			}
		});
	}
]);

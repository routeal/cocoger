'use strict';


angular.module('core').controller('HomeController', ['$scope', 'Authentication',
	function($scope, Authentication) {
		// This provides Authentication context.
		$scope.authentication = Authentication;
	}
]);


angular.module('core').controller('FooterController', ['$scope', 'Authentication',
	function($scope, Authentication) {
		// This provides Authentication context.
		$scope.authentication = Authentication;
	}
]);


angular.module('core').controller('LayoutController', ['$scope', '$window',
	function($scope, $window) {
		$scope.isMobile = function() {
			return /iPhone|iPad|iPod|Android/i.test(window.navigator.userAgent);
		};
	}
]);

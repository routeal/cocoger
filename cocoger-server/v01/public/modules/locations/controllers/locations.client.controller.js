'use strict';

// Locations controller
angular.module('locations').controller('LocationsController', ['$scope', '$stateParams', '$location', 'Authentication', 'Locations', '$window',
        function($scope, $stateParams, $location, Authentication, Locations, $window) {

        $scope.authentication = Authentication;

        $scope.locations = [];

        $scope.map = {
            // http://angular-google-maps.org/use
            center: {
                latitude: 35.691006,
                longitude: 139.700181
            },
            zoom: 12,
            lineStyle: {
                color: '#333',
                weight: 5,
                opacity: 0.7
            },
        };


	$scope.friendId = null;

        // Find a list of Locations
        $scope.find = function() {
		if ($stateParams.friendId) {
			console.log($stateParams.friendId);
			$scope.friendId = $stateParams.friendId;
		}
		$scope.showToday();
        };

        $scope.$on('mapInitialized', function(event, map) {
            $scope.map = map;
            $scope.$watchCollection('locations', function(newVal, oldVal) {
                if ($scope.locations.length === 0) {
                    return;
                }
                var center = Math.floor($scope.locations.length / 2);
                var loc = $scope.locations[center];
                map.setCenter({lat:loc.latitude, lng:loc.longitude});
            });
            $scope.$digest();
        });


        $scope.showToday = function() {
		var arg = {};
		arg.timestamp = $window.moment().unix();
		if ($scope.friendId) {
			arg.friendId = $scope.friendId;
		}
		$scope.locations = Locations.query(arg);
        };

        $scope.showPrev = function() {
		var arg = {};
		arg.timestamp = $window.moment().unix();
		if ($scope.friendId) {
			arg.friendId = $scope.friendId;
		}
		$scope.locations = Locations.query(arg);
        };

	$scope.minDate = new Date(2015, 9, 1);
	$scope.maxDate = new Date();

	$scope.today = function() {
		$scope.dt = new Date();
	};
	$scope.today();

	$scope.status = {
		opened: false
	};

	$scope.open = function($event) {
		$scope.status.opened = true;
	};

	$scope.changedate = function() {
		var arg = {};
		arg.timestamp = $window.moment($scope.dt).unix();
		if ($scope.friendId) {
			arg.friendId = $scope.friendId;
		}
		$scope.locations = Locations.query(arg);
	};

    }
]);

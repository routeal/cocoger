'use strict';

angular.module('friends').controller('FriendsController', ['$scope', '$stateParams', '$location', 'Authentication', 'Friends',
	function($scope, $stateParams, $location, Authentication, Friends) {
		$scope.authentication = Authentication;

		$scope.invite = function(id, range, message) {
			var friend = new Friends({
				user: id,
				range: range,
				message: message
			});
			friend.$save(function(response) {
				// successfully sent, go back to the friend list
				$location.path('friends');
			}, function(errorResponse) {
				$scope.error = errorResponse.data.message;
			});
		};

		$scope.remove = function(friend) {
			if (friend) {
				friend.$remove();

				for (var i in $scope.friends) {
					if ($scope.friends[i] === friend) {
						$scope.friends.splice(i, 1);
					}
				}
			} else {
				$scope.friend.$remove(function() {
					$location.path('friends');
				});
			}
		};

		$scope.update = function() {
			var friend = $scope.friend;

			friend.$update(function() {
				$location.path('friends/' + friend._id);
			}, function(errorResponse) {
				$scope.error = errorResponse.data.message;
			});
		};

		$scope.find = function() {
			$scope.friends = Friends.query();
		};

		$scope.findOne = function() {
			$scope.friend = Friends.get({
				friendId: $stateParams.friendId
			});
		};

		$scope.select = function(friend) {
			console.log('%s selected', friend.user.name);
			console.log('%s selected', friend.user.id);

			$location.path('/locations/' + friend.user.id);
		};
	}
]);

angular.module('friends').filter('gender', function() {
	return function (item) {
		if (item === 0) {
			return 'Man';
		} else if (item === 1) {
			return 'Woman';
		}
	};
});

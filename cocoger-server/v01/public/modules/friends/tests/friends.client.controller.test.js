'use strict';

(function() {
	// Friends Controller Spec
	describe('FriendsController', function() {
		// Initialize global variables
		var FriendsController,
			scope,
			$httpBackend,
			$stateParams,
			$location;

		// The $resource service augments the response object with methods for updating and deleting the resource.
		// If we were to use the standard toEqual matcher, our tests would fail because the test values would not match
		// the responses exactly. To solve the problem, we define a new toEqualData Jasmine matcher.
		// When the toEqualData matcher compares two objects, it takes only object properties into
		// account and ignores methods.
		beforeEach(function() {
			jasmine.addMatchers({
				toEqualData: function(util, customEqualityTesters) {
					return {
						compare: function(actual, expected) {
							return {
								pass: angular.equals(actual, expected)
							};
						}
					};
				}
			});
		});

		// Then we can start by loading the main application module
		beforeEach(module(ApplicationConfiguration.applicationModuleName));

		// The injector ignores leading and trailing underscores here (i.e. _$httpBackend_).
		// This allows us to inject a service but then attach it to a variable
		// with the same name as the service.
		beforeEach(inject(function($controller, $rootScope, _$location_, _$stateParams_, _$httpBackend_) {
			// Set a new global scope
			scope = $rootScope.$new();

			// Point global variables to injected services
			$stateParams = _$stateParams_;
			$httpBackend = _$httpBackend_;
			$location = _$location_;

			// Initialize the Friends controller.
			FriendsController = $controller('FriendsController', {
				$scope: scope
			});
		}));

		it('$scope.find() should create an array with at least one friend object fetched from XHR', inject(function(Friends) {
			// Create sample friend using the Friends service
			var sampleFriend = new Friends({
				title: 'An Friend about MEAN',
				content: 'MEAN rocks!'
			});

			// Create a sample friends array that includes the new friend
			var sampleFriends = [sampleFriend];

			// Set GET response
			$httpBackend.expectGET('friends').respond(sampleFriends);

			// Run controller functionality
			scope.find();
			$httpBackend.flush();

			// Test scope value
			expect(scope.friends).toEqualData(sampleFriends);
		}));

		it('$scope.findOne() should create an array with one friend object fetched from XHR using a friendId URL parameter', inject(function(Friends) {
			// Define a sample friend object
			var sampleFriend = new Friends({
				title: 'An Friend about MEAN',
				content: 'MEAN rocks!'
			});

			// Set the URL parameter
			$stateParams.friendId = '525a8422f6d0f87f0e407a33';

			// Set GET response
			$httpBackend.expectGET(/friends\/([0-9a-fA-F]{24})$/).respond(sampleFriend);

			// Run controller functionality
			scope.findOne();
			$httpBackend.flush();

			// Test scope value
			expect(scope.friend).toEqualData(sampleFriend);
		}));

		it('$scope.create() with valid form data should send a POST request with the form input values and then locate to new object URL', inject(function(Friends) {
			// Create a sample friend object
			var sampleFriendPostData = new Friends({
				title: 'An Friend about MEAN',
				content: 'MEAN rocks!'
			});

			// Create a sample friend response
			var sampleFriendResponse = new Friends({
				_id: '525cf20451979dea2c000001',
				title: 'An Friend about MEAN',
				content: 'MEAN rocks!'
			});

			// Fixture mock form input values
			scope.title = 'An Friend about MEAN';
			scope.content = 'MEAN rocks!';

			// Set POST response
			$httpBackend.expectPOST('friends', sampleFriendPostData).respond(sampleFriendResponse);

			// Run controller functionality
			scope.create();
			$httpBackend.flush();

			// Test form inputs are reset
			expect(scope.title).toEqual('');
			expect(scope.content).toEqual('');

			// Test URL redirection after the friend was created
			expect($location.path()).toBe('/friends/' + sampleFriendResponse._id);
		}));

		it('$scope.update() should update a valid friend', inject(function(Friends) {
			// Define a sample friend put data
			var sampleFriendPutData = new Friends({
				_id: '525cf20451979dea2c000001',
				title: 'An Friend about MEAN',
				content: 'MEAN Rocks!'
			});

			// Mock friend in scope
			scope.friend = sampleFriendPutData;

			// Set PUT response
			$httpBackend.expectPUT(/friends\/([0-9a-fA-F]{24})$/).respond();

			// Run controller functionality
			scope.update();
			$httpBackend.flush();

			// Test URL location to new object
			expect($location.path()).toBe('/friends/' + sampleFriendPutData._id);
		}));

		it('$scope.remove() should send a DELETE request with a valid friendId and remove the friend from the scope', inject(function(Friends) {
			// Create new friend object
			var sampleFriend = new Friends({
				_id: '525a8422f6d0f87f0e407a33'
			});

			// Create new friends array and include the friend
			scope.friends = [sampleFriend];

			// Set expected DELETE response
			$httpBackend.expectDELETE(/friends\/([0-9a-fA-F]{24})$/).respond(204);

			// Run controller functionality
			scope.remove(sampleFriend);
			$httpBackend.flush();

			// Test array after successful delete
			expect(scope.friends.length).toBe(0);
		}));
	});
}());

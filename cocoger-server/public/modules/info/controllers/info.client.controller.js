'use strict';

angular.module('info').controller('InfoController', ['$scope', '$stateParams', '$location', 'Authentication', 'Info',
	function($scope, $stateParams, $location, Authentication, Info) {
		$scope.authentication = Authentication;

		$scope.mobile = function () {
			return /iPhone|iPad|iPod|Android/i.test(navigator.userAgent);
		};

		$scope.init = function() {
			var isMobile = /iPhone|iPad|iPod|Android/i.test(navigator.userAgent);
			if (!isMobile) return;

			var content = angular.element(document.getElementsByClassName('content'));
			$scope.margintop = content.css('margin-top');
			$scope.marginbottom = content.css('margin-top');

			var header = angular.element(document.getElementsByTagName('header'));
			header.css('display', 'none');
			header.css('visibility', 'hidden');

			var footer = angular.element(document.getElementsByTagName('footer'));
			footer.css('display', 'none');
			footer.css('visibility', 'hidden');

			content.css('margin-top', 0);
			content.css('margin-bottom', 0);
		};

		$scope.$on('$viewContentLoaded', function readyToTrick() {
			// do again to force to remove the header and
			// footer, there should be a better way to do

			var content = angular.element(document.getElementsByClassName('content'));
			$scope.margintop = content.css('margin-top');
			$scope.marginbottom = content.css('margin-top');

			var header = angular.element(document.getElementsByTagName('header'));
			header.css('display', 'none');
			header.css('visibility', 'hidden');

			var footer = angular.element(document.getElementsByTagName('footer'));
			footer.css('display', 'none');
			footer.css('visibility', 'hidden');

			content.css('margin-top', 0);
			content.css('margin-bottom', 0);
		});

		$scope.$on('$destroy', function() {
			var isMobile = /iPhone|iPad|iPod|Android/i.test(navigator.userAgent);
			if (!isMobile) return;

			var content = angular.element(document.getElementsByClassName('content'));
			content.css('margin-top', $scope.margintop);
			content.css('margin-bottom', $scope.marginbottom);

			var header = angular.element(document.getElementsByTagName('header'));
			header.css('display', 'block');
			header.css('visibility', 'visible');

			var footer = angular.element(document.getElementsByTagName('footer'));
			footer.css('display', 'block');
			footer.css('visibility', 'visible');
		});


		$scope.faqList = [
			{
				title: 'title 1',
				faq: [
					{
						question: 'What is cocoger?',
						answer: 'LINE HERE is an app where you can share your location with family and friends. You can use LINE HERE to find your friend\'s location, so you can use it when you are meeting each other. You can also use it to check your child\'s location, so you can use the app for increased safety and crime prevention.',
					},
					{
						question: 'What are the recommended system specifications?',
						answer: 'The recommended system specifications for LINE HERE are as follows:<ul><li>iOS\niOS 7.0 or above</li><li>Android\nAndroid OS 4.1 or above</li></ul>Please be aware the app may not work correctly outside of the recommended system specifications, therefore in those cases we are unable to offer service support. ',
					},
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
				],
			},
			{
				title: 'title 2',
				faq: [
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
				],
			},
			{
				title: 'title 3',
				faq: [
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
				],
			},
		];

		$scope.faqListJa = [
			{
				title: 'Japanse title 1',
				faq: [
					{
						question: 'What is cocoger?',
						answer: 'LINE HERE is an app where you can share your location with family and friends. You can use LINE HERE to find your friend\'s location, so you can use it when you are meeting each other. You can also use it to check your child\'s location, so you can use the app for increased safety and crime prevention.',
					},
					{
						question: 'What are the recommended system specifications?',
						answer: 'The recommended system specifications for LINE HERE are as follows:<ul><li>iOS\niOS 7.0 or above</li><li>Android\nAndroid OS 4.1 or above</li></ul>Please be aware the app may not work correctly outside of the recommended system specifications, therefore in those cases we are unable to offer service support. ',
					},
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
				],
			},
			{
				title: 'title 2',
				faq: [
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
				],
			},
			{
				title: 'title 3',
				faq: [
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
					{
						question: 'What is cocoger?',
						answer: 'Dynamic Group Body - 1',
					},
				],
			},
		];

		$scope.getFAQ = function() {
			if ($stateParams.lang === 'ja')
				return $scope.faqListJa;
			else
				return $scope.faqList;
		};
	}
]);

angular.module('info').filter('unsafe', function($sce) {
	return function(val) {
	        return $sce.trustAsHtml(val);
	};
});

'use strict';

// Setting up route
angular.module('info').config(['$stateProvider',
	function($stateProvider) {
		// Info state routing
		$stateProvider.
		state('listAbout', {
			url: '/about',
			templateUrl: 'modules/info/views/view-about.client.view.html'
		}).
		state('listAboutEn', {
			url: '/about/en',
			templateUrl: 'modules/info/views/view-about.client.view.html'
		}).
		state('listAboutJa', {
			url: '/about/ja',
			templateUrl: 'modules/info/views/view-about.ja.client.view.html'
		}).
		state('listFAQ', {
			url: '/faq',
			templateUrl: 'modules/info/views/view-faq.client.view.html',
			params: {
				lang: 'en',
			}
		}).
		state('listFAQEn', {
			url: '/faq/en',
			templateUrl: 'modules/info/views/view-faq.client.view.html',
			params: {
				lang: 'en',
			}
		}).
		state('listFAQJa', {
			url: '/faq/ja',
			templateUrl: 'modules/info/views/view-faq.client.view.html',
			params: {
				lang: 'ja',
			}
		}).
		state('listTermJa', {
			url: '/term/ja',
			templateUrl: 'modules/info/views/view-term.ja.client.view.html',
		}).
		state('listTerm', {
			url: '/term/{path:.*}',
			templateUrl: 'modules/info/views/view-term.client.view.html',
		}).
		state('listPrivJa', {
			url: '/privacy/ja',
			templateUrl: 'modules/info/views/view-privacy.ja.client.view.html',
		}).
		state('listPriv', {
			url: '/privacy/{path:.*}',
			templateUrl: 'modules/info/views/view-privacy.client.view.html',
		});
	}
]);

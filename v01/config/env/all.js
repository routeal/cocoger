'use strict';

module.exports = {
	app: {
		title: 'cocoger',
		description: 'cocoger service platform',
		keywords: ''
	},
	port: process.env.PORT || 3000,
	domain: process.env.ELB_DOMAIN || 'cocoger.com',
	templateEngine: 'swig',
	sessionSecret: 'MEAN',
	sessionCollection: 'sessions',
	jwtSecret: 'cocoger-2015',
	jwtIssuer: 'routeal.com',
        defaultTimezone: 'Asia/Tokyo',
	maxRequestCount: 3,
	assets: {
		lib: {
			css: [
				'public/lib/bootstrap/dist/css/bootstrap.min.css',
				'public/lib/bootstrap/dist/css/bootstrap-theme.min.css',
			],
			js: [
				'public/lib/angular/angular.min.js',
				'public/lib/angular-animate/angular-animate.min.js',
				'public/lib/angular-bootstrap/ui-bootstrap-tpls.min.js',

				'public/lib/angular-resource/angular-resource.min.js',
				'public/lib/angular-cookies/angular-cookies.min.js',
				'public/lib/angular-touch/angular-touch.min.js',
				'public/lib/angular-sanitize/angular-sanitize.min.js',
				'public/lib/angular-ui-router/release/angular-ui-router.min.js',
				'public/lib/angular-ui-utils/ui-utils.min.js',
				'public/lib/angular-translate/angular-translate.min.js',

				'public/lib/moment/min/moment.min.js',
				'public/lib/lodash/lodash.min.js',
				'//maps.googleapis.com/maps/api/js?sensor=false',
				'public/lib/angular-google-maps/dist/angular-google-maps.min.js'
			]
		},
		css: [
			'public/modules/**/css/*.css'
		],
		js: [
			'public/config.js',
			'public/application.js',
			'public/translations_en.js',
			'public/translations_ja.js',
			'public/modules/*/*.js',
			'public/modules/*/*[!tests]*/*.js'
		],
		tests: [
			'public/lib/angular-mocks/angular-mocks.js',
			'public/modules/*/tests/*.js'
		]
	}
};

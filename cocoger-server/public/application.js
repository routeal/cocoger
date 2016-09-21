'use strict';

//Start by defining the main module and adding the module dependencies
angular.module(ApplicationConfiguration.applicationModuleName, ApplicationConfiguration.applicationModuleVendorDependencies);

// Setting HTML5 Location Mode
angular.module(ApplicationConfiguration.applicationModuleName)
	.config(['$locationProvider',
		 function($locationProvider) {
			 $locationProvider.hashPrefix('!');
		 }
		])
// Setting localization
	.config(['$translateProvider',
		 function($translateProvider) {

			 $translateProvider.translations('en', translationsEn);
			 $translateProvider.translations('ja', translationsJa);
			 $translateProvider.preferredLanguage(findLanguage());
		 }
		]);

function findLanguage() {
    try {
        return (navigator.browserLanguage || navigator.language || navigator.userLanguage).substr(0, 2)
    } catch (e) {
        return "en";
    }
}


//Then define the init function for starting up the application
angular.element(document).ready(function() {
	//Fixing facebook bug with redirect
	if (window.location.hash === '#_=_') window.location.hash = '#!';

	//Then init the app
	angular.bootstrap(document, [ApplicationConfiguration.applicationModuleName]);
});

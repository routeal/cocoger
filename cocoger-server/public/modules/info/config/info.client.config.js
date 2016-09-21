'use strict';

// Configuring the Info module
angular.module('info').run(['Menus',
	function(Menus) {
		// Set top bar menu items
		Menus.addMenuItem('topbar', 'Info', 'info', 'dropdown', '/info(/about)?');
		Menus.addSubMenuItem('topbar', 'info', 'About', 'about');
		Menus.addSubMenuItem('topbar', 'info', 'FAQ', 'faq');
		Menus.addSubMenuItem('topbar', 'info', 'Term', 'term');
	}
]);

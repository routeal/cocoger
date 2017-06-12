var geocoder = require('geocoder');

geocoder.selectProvider("google");

/*
geocoder.geocode("Kobe", function ( err, data ) {
    console.log("ret="+data);
});
*/

geocoder.reverseGeocode(35.663185, 139.752868, function ( err, data ) {
    if (err || data.status !== 'OK') {
	console.log(err);
    } else {
	// looks like the first one is most in the detail
	var result = data.results[0];
	if (result.types.indexOf('sublocality') < 0) {
	    console.log('only sublocality supported')
	}

	var address_component;
	for (address_component in result.address_components) {
	    //console.log(result.address_components[address_component]);

	    if (result.address_components[address_component].types.indexOf('sublocality_level_1') >= 0) {
		console.log('street:' + result.address_components[address_component].long_name);
	    }
	    else if (result.address_components[address_component].types.indexOf('ward') >= 0) {
		console.log('town:' + result.address_components[address_component].long_name);
	    }
	    else if (result.address_components[address_component].types.indexOf('locality') >= 0) {
		console.log('city:' + result.address_components[address_component].long_name);
	    }
	    else if (result.address_components[address_component].types.indexOf('administrative_area_level_1') >= 0) {
		console.log('state:' + result.address_components[address_component].long_name);
	    }
	    else if (result.address_components[address_component].types.indexOf('country') >= 0) {
		console.log('country:' + result.address_components[address_component].long_name);
	    }
	}
    }
});

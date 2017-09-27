package com.routeal.cocoger.ui.main;

import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.PointOfInterest;
import com.routeal.cocoger.MainApplication;

/**
 * Created by hwatanabe on 9/27/17.
 */

class PoiManager {
    private GoogleMap.OnPoiClickListener mPoiClickListener = new GoogleMap.OnPoiClickListener() {
        @Override
        public void onPoiClick(PointOfInterest pointOfInterest) {
            Toast.makeText(MainApplication.getContext(), "Clicked: " +
                            pointOfInterest.name + "\nPlace ID:" + pointOfInterest.placeId +
                            "\nLatitude:" + pointOfInterest.latLng.latitude +
                            " Longitude:" + pointOfInterest.latLng.longitude,
                    Toast.LENGTH_LONG).show();
        }
    };
    private GoogleMap mMap;


    PoiManager(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnPoiClickListener(mPoiClickListener);
    }

}

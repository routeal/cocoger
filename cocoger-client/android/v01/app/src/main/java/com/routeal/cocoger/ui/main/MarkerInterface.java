package com.routeal.cocoger.ui.main;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by hwatanabe on 10/15/17.
 */

interface MarkerInterface {
    boolean onMarkerClick(Marker marker);

    void onWindowHidden(InfoWindow infoWindow);
}
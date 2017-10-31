package com.routeal.cocoger.ui.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.routeal.cocoger.util.Utils;

/**
 * Created by hwatanabe on 9/27/17.
 */

class PoiMarker {
    private final static String TAG = "PoiMarker";
    private GoogleMap mMap;
    private GeoDataClient mGeoDataClient;
    InfoWindowManager mInfoWindowManager;
    private static Marker mMarker;
    private static InfoWindow mWindow;

    PoiMarker(GoogleMap map, GeoDataClient geoDataClient, InfoWindowManager infoWindowManager) {
        mMap = map;
        mGeoDataClient = geoDataClient;
        mInfoWindowManager = infoWindowManager;
    }

    void onPoiClick(PointOfInterest pointOfInterest) {
        mGeoDataClient.getPlaceById(pointOfInterest.placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    Place place = places.get(0);
                    addPoiInfoWindow(place);
                    places.release();
                }
            }
        });
    }

    // One time exclusive info window for clicking on Poi in the map, which creates
    // a transparent marker and adds an info window on it.
    private void addPoiInfoWindow(Place place) {
        LatLng pos = place.getLatLng();
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
        BitmapDescriptor transparent = BitmapDescriptorFactory.fromBitmap(bitmap);

        MarkerOptions options = new MarkerOptions()
                .position(pos)
                .icon(transparent)
                .anchor((float) 0.5, (float) 0.5);
        mMarker = mMap.addMarker(options);

        final PoiInfoFragment poiInfoFragment = new PoiInfoFragment();
        poiInfoFragment.setTitle(place.getName().toString());
        poiInfoFragment.setLocation(Utils.getLocation(place.getLatLng()));
        poiInfoFragment.setAddress(place.getAddress().toString());
        poiInfoFragment.setPoiMarker(this);
        InfoWindow.MarkerSpecification mMarkerOffset = new InfoWindow.MarkerSpecification(0, 0);
        mWindow = new InfoWindow(mMarker, mMarkerOffset, poiInfoFragment);
        mInfoWindowManager.setHideOnFling(true);
        mInfoWindowManager.toggle(mWindow, true);

        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(place.getId());
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                if (photos == null) return;
                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                if (photoMetadataBuffer == null || photoMetadataBuffer.getCount() == 0) return;
                // Get the first photo in the list.
                PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                if (photoMetadata == null || !photoMetadata.isDataValid()) return;
                // Get the attribution text.
                CharSequence attribution = photoMetadata.getAttributions();
                if (attribution == null || attribution.length() == 0) return;
                // Get a full-size bitmap for the photo.
                Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getScaledPhoto(photoMetadata, 128, 128);
                if (!photoMetadata.isDataValid()) return;
                photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                        PlacePhotoResponse photo = task.getResult();
                        if (photo == null) return;
                        Bitmap bitmap = photo.getBitmap();
                        if (bitmap == null) return;
                        poiInfoFragment.setStreetViewPicture(bitmap);
                    }
                });
            }
        });
    }

    void removePoiInfoWindow() {
        if (mWindow != null) {
            mInfoWindowManager.hide(mWindow);
            Fragment fragment = mWindow.getWindowFragment();
            FragmentManager fragmentManager = fragment.getFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction trans = fragmentManager.beginTransaction();
                trans.remove(fragment);
                trans.commit();
            }
            mWindow = null;
        }
        if (mMarker != null) {
            mMarker.remove();
            mMarker = null;
        }
    }

    void onWindowHidden(InfoWindow infoWindow) {
        Fragment fragment = infoWindow.getWindowFragment();
        if (fragment != null) {
            if (fragment instanceof PoiInfoFragment) {
                removePoiInfoWindow();
            }
        }
    }
}

package com.routeal.cocoger.ui.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.view.animation.BounceInterpolator;

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

class PoiManager implements GoogleMap.OnPoiClickListener {
    private GoogleMap mMap;
    private GeoDataClient mGeoDataClient;
    private InfoWindowManager mInfoWindowManager;
    private Marker mMarker;
    private InfoWindow mWindow;

    PoiManager(GoogleMap googleMap, GeoDataClient geoDataClient, InfoWindowManager infoWindowManager) {
        mMap = googleMap;
        mMap.setOnPoiClickListener(this);
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Your marker title")
                        .snippet("Your marker snippet"));
                dropPinEffect(marker);
            }
        });
        mGeoDataClient = geoDataClient;
        mInfoWindowManager = infoWindowManager;
    }

    @Override
    public void onPoiClick(PointOfInterest pointOfInterest) {
        mGeoDataClient.getPlaceById(pointOfInterest.placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    Place place = places.get(0);
                    addInfoWindow(place);
                    places.release();
                }
            }
        });
    }

    void addInfoWindow(Place place) {
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
        poiInfoFragment.setPoiManager(PoiManager.this);
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
                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                // Get the first photo in the list.
                PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                // Get the attribution text.
                CharSequence attribution = photoMetadata.getAttributions();
                // Get a full-size bitmap for the photo.
                Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getScaledPhoto(photoMetadata, 128, 128);
                photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                        PlacePhotoResponse photo = task.getResult();
                        Bitmap bitmap = photo.getBitmap();
                        poiInfoFragment.setStreetViewPicture(bitmap);
                    }
                });
            }
        });
    }

    void removeInfoWindow() {
        if (mWindow != null) {
            mInfoWindowManager.hide(mWindow);
            mWindow = null;
        }
        if (mMarker != null) {
            mMarker.remove();
            mMarker = null;
        }
    }

    private void dropPinEffect(final Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final android.os.Handler handler = new android.os.Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        // Use the bounce interpolator
        final android.view.animation.Interpolator interpolator =
                new BounceInterpolator();

        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 15);
                } else { // done elapsing, show window
                    marker.showInfoWindow();
                }
            }
        });
    }

}

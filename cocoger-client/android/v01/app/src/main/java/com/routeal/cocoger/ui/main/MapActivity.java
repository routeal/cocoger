package com.routeal.cocoger.ui.main;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.util.Utils;
import com.theartofdev.edmodo.cropper.CropImage;

abstract class MapActivity extends MapBaseActivity
        implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback, View.OnClickListener, InfoWindowManager.WindowShowListener {

    final static int DEFAULT_ZOOM = 16;
    private final static String TAG = "MapActivity";
    private final static String KEY_CAMERA_POSITION = "camera_position";
    private final static String KEY_LOCATION = "location";
    protected GoogleMap mMap;
    protected GeoDataClient mGeoDataClient;
    protected Utils.ProgressBarView mSpinner;
    protected InfoWindowManager mInfoWindowManager;
    protected MarkerManager mMm;
    protected MapDirection mDirection;
    protected MapStyle mMapStyle;
    protected PoiManager mPoi;
    protected PlaceManager mPlace;

    private View mapView;
    private CameraPosition mCameraPosition;
    private Location mInitialLocation;
    private MapBroadcastReceiver mReceiver;

    @Override
    protected void onDestroy() {
        FriendManager.destroy();
        FB.setUser(null);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mInitialLocation = (Location) savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // set up the 'my' location button
        Drawable myLocationDrawable = Utils.getIconDrawable(this, R.drawable.ic_my_location_white_36dp, R.color.gray);
        FloatingActionButton myLocationButton = (FloatingActionButton) findViewById(R.id.my_location);
        myLocationButton.setImageDrawable(myLocationDrawable);
        myLocationButton.setOnClickListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mReceiver.getLocation());
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (mPlace != null) {
                    mPlace.setCropImage(this, result.getUri());
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                Utils.getLatLng(mReceiver.getLocation()), DEFAULT_ZOOM));
    }

    @Override
    void startApp(Location location) {
        mInitialLocation = location;

        // show the mSpinner
        mSpinner = Utils.getProgressBar(this);

        // set up the info window manager
        MapInfoWindowFragment mapInfoWindowFragment =
                (MapInfoWindowFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mInfoWindowManager = mapInfoWindowFragment.infoWindowManager();
        mInfoWindowManager.setHideOnFling(true);
        mInfoWindowManager.setWindowShowListener(this);

        mapView = mapInfoWindowFragment.getView();

        // mSpinner will be dismissed in the MapReady callback
        mapInfoWindowFragment.getMapAsync(this);
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mGeoDataClient = Places.getGeoDataClient(this, null);
        mMm = new MarkerManager(mMap, mInfoWindowManager);
        mDirection = new MapDirection(mMap, mInfoWindowManager);
        mPoi = new PoiManager(mMap, mGeoDataClient, mInfoWindowManager);
        mPlace = new PlaceManager(this, mMap, mInfoWindowManager);

        mReceiver = new MapBroadcastReceiver(this, mMap, mMm, mDirection, mPlace);
        mReceiver.setLocation(mInitialLocation);

        mMap.setPadding(8, 0, 0, 148);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);

        setupApp();

        mMapStyle = new MapStyle(mMap, this);
        mMapStyle.init(this, mMap);

        Log.d(TAG, "onMapReady: location detected from the base object");

        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mInitialLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    Utils.getLatLng(mInitialLocation), DEFAULT_ZOOM));
        }

        // the location may not be available at this point
        if (mInitialLocation != null) {
            Log.d(TAG, "onMapReady: setupMarkers");
            Address address = Utils.getAddress(mInitialLocation);
            mMm.setupMarkers(mInitialLocation, address);

            Intent intent = new Intent(FB.USER_LOCATION_UPDATE);
            intent.putExtra(FB.LOCATION, mInitialLocation);
            intent.putExtra(FB.ADDRESS, address);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

        mCameraPosition = null;
        mInitialLocation = null;

        if (mSpinner != null) {
            mSpinner.hide();
            mSpinner = null;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mMm != null) {
            if (mMm.onMarkerClick(marker)) {
                return true;
            }
        }
        if (mPoi != null) {
            if (mPoi.onMarkerClick(marker)) {
                return true;
            }
        }
        if (mPlace != null) {
            if (mPlace.onMarkerClick(marker)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onWindowShowStarted(@NonNull InfoWindow infoWindow) {

    }

    @Override
    public void onWindowShown(@NonNull InfoWindow infoWindow) {

    }

    @Override
    public void onWindowHideStarted(@NonNull InfoWindow infoWindow) {

    }

    @Override
    public void onWindowHidden(@NonNull InfoWindow infoWindow) {
        if (mMm != null) {
            mMm.onWindowHidden(infoWindow);
        }
        if (mPoi != null) {
            mPoi.onWindowHidden(infoWindow);
        }
        if (mPlace != null) {
            mPlace.onWindowHidden(infoWindow);
        }
    }

    abstract void closeSlidePanel();
}

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PointOfInterest;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.util.Utils;
import com.theartofdev.edmodo.cropper.CropImage;

abstract class MapActivity extends MapBaseActivity
        implements GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnPoiClickListener,
        OnMapReadyCallback,
        View.OnClickListener,
        InfoWindowManager.WindowShowListener {

    // package public
    final static int DEFAULT_ZOOM = 16;

    private final static String TAG = "MapActivity";
    private final static String KEY_CAMERA_POSITION = "camera_position";
    private final static String KEY_LOCATION = "location";

    protected GoogleMap mMap;
    protected InfoWindowManager mInfoWindowManager;
    protected GeoDataClient mGeoDataClient;
    protected Utils.ProgressBarView mSpinner;
    protected MapDirection mDirection;
    protected MapStyle mMapStyle;

    private View mapView;
    private CameraPosition mCameraPosition;
    private LatLng mInitialLocation;
    private MapBroadcastReceiver mReceiver;
    private UserMarkers mUserMarkers;
    private PlaceMarkers mPlaceMarkers;
    private PoiMarker mPoiMarker;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mInitialLocation = (LatLng) savedInstanceState.getParcelable(KEY_LOCATION);
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
                mPlaceMarkers.setCropImage(result.getUri());
            }
        }
    }

    @Override
    public void onClick(View v) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mReceiver.getLocation(), DEFAULT_ZOOM));
    }

    @Override
    void startApp(Location location) {
        mInitialLocation = Utils.getLatLng(location);

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
        mDirection = new MapDirection(mMap, mInfoWindowManager);
        mUserMarkers = new UserMarkers(mMap, mInfoWindowManager);
        mPlaceMarkers = new PlaceMarkers(this, mMap, mInfoWindowManager);
        mPoiMarker = new PoiMarker(mMap, mGeoDataClient, mInfoWindowManager);

        if (mReceiver == null) {
            mReceiver = new MapBroadcastReceiver(this, mMap, mInfoWindowManager, mUserMarkers,
                    mPlaceMarkers, mDirection);
            mReceiver.setLocation(mInitialLocation);
        }

        mMap.setPadding(8, 0, 0, 148);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnPoiClickListener(this);

        mPlaceMarkers.setup();

        setupApp();

        mMapStyle = new MapStyle(mMap, this);
        mMapStyle.init(this, mMap);

        Log.d(TAG, "onMapReady: location detected from the base object");

        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mInitialLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    mInitialLocation, DEFAULT_ZOOM));
        }

        // the location may not be available at this point
        if (mInitialLocation != null) {
            Address address = Utils.getAddress(mInitialLocation);
            if (address != null) {
                Log.d(TAG, "onMapReady: init");
                mUserMarkers.init(mInitialLocation, address);
            } else {
                Log.d(TAG, "onMapReady: no address, no init");
            }

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
        if (mUserMarkers.onMarkerClick(marker)) {
            return true;
        } else if (mPlaceMarkers.onMarkerClick(marker)) {
            return true;
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
        mPoiMarker.onWindowHidden(infoWindow);
    }

    @Override
    public void onCameraMove() {
        mUserMarkers.onCameraMove();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mPlaceMarkers.onMapLongClick(latLng);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mPlaceMarkers.onMarkerDragEnd(marker);
    }

    @Override
    public void onPoiClick(PointOfInterest pointOfInterest) {
        mPoiMarker.onPoiClick(pointOfInterest);
    }

    abstract void closeSlidePanel();

    abstract void updateMessage();
}

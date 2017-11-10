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
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.service.LocationUpdate;
import com.routeal.cocoger.util.Utils;
import com.theartofdev.edmodo.cropper.CropImage;

abstract class MapActivity extends MapBaseActivity
        implements
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnPoiClickListener,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnPolygonClickListener,
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
    private Location mInitialLocation;
    private Address mInitialAddress;
    private MapBroadcastReceiver mReceiver;
    private UserMarkers mUserMarkers;
    private PlaceMarkers mPlaceMarkers;
    private GroupMarkers mGroupMarkers;
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
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // set up the 'my' location button
        Drawable myLocationDrawable = Utils.getIconDrawable(this, R.drawable.ic_my_location_white_36dp, R.color.grey_500);
        FloatingActionButton myLocationButton = (FloatingActionButton) findViewById(R.id.my_location);
        myLocationButton.setImageDrawable(myLocationDrawable);
        myLocationButton.setOnClickListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
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
        Intent intent = new Intent(FB.USER_SHOW);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    void startApp(Location location) {
        mInitialLocation = location;
        if (location != null) {
            mInitialAddress = Utils.getAddress(location);
        }

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
        mGroupMarkers = new GroupMarkers(this, mMap, mInfoWindowManager);
        mUserMarkers = new UserMarkers(mMap, mInfoWindowManager);
        mPlaceMarkers = new PlaceMarkers(this, mMap, mInfoWindowManager);
        mPoiMarker = new PoiMarker(mMap, mGeoDataClient, mInfoWindowManager);
        mMapStyle = new MapStyle(mMap, this);
        mReceiver = new MapBroadcastReceiver(this, mMap, mInfoWindowManager, mUserMarkers,
                mPlaceMarkers, mGroupMarkers, mDirection);

        mGroupMarkers.setUserMarkers(mUserMarkers);
        mUserMarkers.setGroupMarkers(mGroupMarkers);

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
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);

        setupApp();

        Log.d(TAG, "onMapReady: location detected from the base object");

        LatLng latLng = null;

        if (mInitialLocation != null) {
            latLng = Utils.getLatLng(mInitialLocation);

            if (mCameraPosition != null) {
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            } else if (mInitialLocation != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MapActivity.DEFAULT_ZOOM));
            }

            mUserMarkers.setup(latLng, mInitialAddress);

            // if the FB user is not available at this time, the markers are not initialized.  Send
            // the initial location so that the markers can be initialized when the FB user becomes
            // available.
            Intent intent = new Intent(FB.USER_LOCATION);
            intent.putExtra(FB.LOCATION, latLng);
            intent.putExtra(FB.ADDRESS, mInitialAddress);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            // this may not save the initial location since the user may not become available by this time
            saveInitialLocation();
        }

        mCameraPosition = null;

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

    @Override
    public void onPolylineClick(Polyline polyline) {
        mDirection.onPolylineClick(polyline);
    }

    @Override
    public void onPolygonClick(Polygon polygon) {
        mGroupMarkers.onPolygonClick(polygon);
    }

    void saveInitialLocation() {
        if (mInitialLocation != null && mInitialAddress != null && FB.getUser() != null) {
            LocationUpdate.getInstance().saveLocation(mInitialLocation, mInitialAddress);
            mInitialLocation = null;
            mInitialAddress = null;
        }
    }

    abstract void closeSlidePanel();

    abstract void updateMessage();
}

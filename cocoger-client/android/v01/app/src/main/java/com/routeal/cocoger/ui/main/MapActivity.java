package com.routeal.cocoger.ui.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.provider.DBUtil;
import com.routeal.cocoger.service.LocationService;
import com.routeal.cocoger.util.CircleTransform;
import com.routeal.cocoger.util.PicassoMarker;
import com.routeal.cocoger.util.Utils;
import com.squareup.picasso.Picasso;

public class MapActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnInfoWindowClickListener {

    private final static String TAG = "MapActivity";
    private final static String LOCATION_PERMISSION = "locationPermission";
    private final static String KEY_CAMERA_POSITION = "camera_position";
    private final static String KEY_LOCATION = "location";

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final static int DEFAULT_ZOOM = 15;

    private GoogleMap mMap;

    private View mapView;

    private CameraPosition mCameraPosition;

    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    private ProgressDialog busyCursor;

    private Marker myMarker;

    // drawing target
    private PicassoMarker myMarkerTarget;

    private Address mAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.activity_maps);

        // registers the receiver to receive the location updates from the service
        LocalBroadcastManager.getInstance(this).registerReceiver(mLastLocationReceiver,
                new IntentFilter(LocationService.LAST_LOCATION_UPDATE));

        // sets up the 'my' location button
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_my_location_white_36dp);
        drawable.mutate();
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.gray));
        FloatingActionButton myLocationButton = (FloatingActionButton) findViewById(R.id.my_location);
        myLocationButton.setImageDrawable(drawable);
        myLocationButton.setOnClickListener(myLocationButtonListener);

        // when the permission is already granted,
        if (MainApplication.isLocationPermitted()) {
            buildMap();
        } else {

            // First, check that the permission is granted.
            // Second, when granted, connect the google api service
            // Third, get the last known location
            // Fourth, close the connection of the google api service
            // Five, let the background service handle the location service

            checkPermission();
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            connectGoogleApiClient();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    connectGoogleApiClient();
                } else {
                    Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_place_white_36dp);
                    drawable.mutate();
                    DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.teal));

                    new MaterialDialog.Builder(this)
                            .icon(drawable)
                            .limitIconToDefaultSize()
                            .title(R.string.location_denied_title)
                            .content(R.string.location_denied_content)
                            .positiveText(R.string.try_again)
                            .negativeText(R.string.exit)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    checkPermission();
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    finish();
                                }
                            })
                            .show();
                }
            }
            break;
        }
    }

    /**
     * Connects with Google API client
     */
    private void connectGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Builds the map when the Google Play services client is successfully connected.
     */
    @Override
    public void onConnected(Bundle bundle) {
        buildMap();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    /**
     * Handles failure to connect to the Google Play services client.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            // Refer to the reference doc for ConnectionResult to see what error codes might
            // be returned in onConnectionFailed.
            Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                    + connectionResult.getErrorCode());
            // TODO: ERROR dialog
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    connectGoogleApiClient();
                } else {
                    // TODO: ERROR dialog
                }
                break;
        }
    }

    private void buildMap() {
        busyCursor = Utils.spinBusyCursor(this);

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnInfoWindowClickListener(this);

        if (MainApplication.isLocationPermitted()) {
            if (mLastKnownLocation == null) {
                mLastKnownLocation = LocationService.getLastLocation();
            }
            if (mCameraPosition != null) {
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            } else if (mLastKnownLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getLatLng(mLastKnownLocation), DEFAULT_ZOOM));
            }
            if (mLastKnownLocation != null) {
                setupMyLocationMarker(mLastKnownLocation);
                busyCursor.dismiss();
            }
            return;
        }

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        boolean mLocationPermissionGranted = false;

        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if (mLastKnownLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getLatLng(mLastKnownLocation), DEFAULT_ZOOM));
            setupMyLocationMarker(mLastKnownLocation);
        }

        MainApplication.permitLocation(true);

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

        busyCursor.dismiss();
    }

    /**
     * Receives location updates from the location service
     */
    private BroadcastReceiver mLastLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            Location location = intent.getParcelableExtra("location");
            if (location != null) {
                // first time only
                if (mLastKnownLocation == null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getLatLng(location), DEFAULT_ZOOM));
                    setupMyLocationMarker(location);
                    busyCursor.dismiss();
                } else {
                    if (myMarker != null) {
                        myMarker.setPosition(getLatLng(location));
                    }
                }
                mLastKnownLocation = location;
            }

            mAddress = intent.getParcelableExtra("address");
        }
    };

    private LatLng getLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    private void setupMyLocationMarker(Location location) {
        // my location marker
        MarkerOptions options = new MarkerOptions().position(getLatLng(location));
        myMarker = mMap.addMarker(options);
        myMarkerTarget = new PicassoMarker(myMarker);
        Picasso.with(getApplicationContext())
                .load(DBUtil.getUser().getPicture())
                .transform(new CircleTransform())
                .into(myMarkerTarget);
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private View.OnClickListener myLocationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(
                    new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())));
        }
    };

    public Address getAddress() {
        return mAddress;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }
}

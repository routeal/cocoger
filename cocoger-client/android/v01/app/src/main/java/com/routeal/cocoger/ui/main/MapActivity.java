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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.User;
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
        GoogleMap.OnMarkerClickListener,
        InfoWindowManager.WindowShowListener {

    private final static String TAG = "MapActivity";
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

    private InfoWindow.MarkerSpecification markerSpec;

    private MyInfoFragment myInfoFragment;

    private InfoWindowManager infoWindowManager;

    private CallbackManager callbackManager;

    private ShareDialog shareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int offsetX = (int) getResources().getDimension(R.dimen.marker_offset_x);
        int offsetY = (int) getResources().getDimension(R.dimen.marker_offset_y);
        markerSpec = new InfoWindow.MarkerSpecification(offsetX, offsetY);

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
                    return;
                } else {
                    // TODO: ERROR dialog
                }
                break;
        }

        // facebook sharing
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void buildMap() {
        busyCursor = Utils.spinBusyCursor(this);

        MapInfoWindowFragment mapInfoWindowFragment =
                (MapInfoWindowFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapInfoWindowFragment.getMapAsync(this);
        mapView = mapInfoWindowFragment.getView();
        infoWindowManager = mapInfoWindowFragment.infoWindowManager();
        infoWindowManager.setHideOnFling(true);

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, shareCallback);
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

        mMap.setOnMarkerClickListener(this);

        if (MainApplication.isLocationPermitted()) {
            if (mLastKnownLocation == null) {
                mLastKnownLocation = LocationService.getLastLocation();
            }
            if (mCameraPosition != null) {
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            } else if (mLastKnownLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        Utils.getLatLng(mLastKnownLocation), DEFAULT_ZOOM));
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
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    Utils.getLatLng(mLastKnownLocation), DEFAULT_ZOOM));
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
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            Utils.getLatLng(location), DEFAULT_ZOOM));
                    setupMyLocationMarker(location);
                    busyCursor.dismiss();
                } else {
                    if (myMarker != null) {
                        myMarker.setPosition(Utils.getLatLng(location));
                    }
                }
                mLastKnownLocation = location;
            }

            mAddress = intent.getParcelableExtra("address");
        }
    };

    private void setupMyLocationMarker(Location location) {
        // my location marker
        MarkerOptions options = new MarkerOptions().position(Utils.getLatLng(location));
        myMarker = mMap.addMarker(options);
        myMarkerTarget = new PicassoMarker(myMarker);
        Picasso.with(getApplicationContext())
                .load(DBUtil.getUser().getPicture())
                .transform(new CircleTransform())
                .into(myMarkerTarget);

        myInfoFragment = new MyInfoFragment();
        myInfoFragment.setMapActivity(this);
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

    private View.OnClickListener myLocationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude())));
        }
    };

    public Address getAddress() {
        return mAddress;
    }

    public Location getLocation() { return mLastKnownLocation; }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getId().compareTo(myMarker.getId()) == 0) {
            InfoWindow infoWindow = new InfoWindow(myMarker, markerSpec, myInfoFragment);
            infoWindowManager.toggle(infoWindow);
        }
        return true;
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
    }

    public static class MyInfoFragment extends Fragment implements View.OnClickListener {
        private MapActivity mapActivity;
        private AppCompatTextView name;
        private AppCompatImageView street_snapshot;
        private AppCompatTextView current_address;
        private AppCompatButton post_facebook;
        private AppCompatButton more_info;
        private AppCompatButton save_to_map;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            Log.d(TAG, "MyInfoFragment: onCreateView");

            View view = inflater.inflate(R.layout.infowindow_me, container, false);
            name = (AppCompatTextView) view.findViewById(R.id.name);
            street_snapshot = (AppCompatImageView) view.findViewById(R.id.street_snapshot);
            current_address = (AppCompatTextView) view.findViewById(R.id.current_address);
            post_facebook = (AppCompatButton) view.findViewById(R.id.post_facebook);
            more_info = (AppCompatButton) view.findViewById(R.id.more_info);
            save_to_map = (AppCompatButton) view.findViewById(R.id.save_to_map);
            street_snapshot.setOnClickListener(this);
            post_facebook.setOnClickListener(this);
            more_info.setOnClickListener(this);
            save_to_map.setOnClickListener(this);
            return view;
        }

        @SuppressWarnings({"MissingPermission"})
        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            Log.d(TAG, "MyInfoFragment: onViewCreated");

            String url = String.format(getResources().getString(R.string.street_view_image_url),
                    mapActivity.getLocation().getLatitude(),
                    mapActivity.getLocation().getLongitude());

            Picasso.with(getContext())
                    .load(url)
                    .resize(96, 96)
                    .into(street_snapshot);

            User user = DBUtil.getUser();
            if (user != null) {
                name.setText(user.getName());
            }

            if (mapActivity.getAddress() != null) {
                String address = mapActivity.getAddress().getAddressLine(0);
                if (address != null) {
                    current_address.setText(address);
                }
            }

            GoogleApiClient mGoogleApiClient = LocationService.getGoogleApiClient();

            if (mGoogleApiClient != null) {
                PendingResult<PlaceLikelihoodBuffer> result =
                        Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
                result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
                        for (PlaceLikelihood placeLikelihood : placeLikelihoods) {
                            Log.i(TAG, String.format("Place '%s' has likelihood: %g",
                                    placeLikelihood.getPlace().getName(),
                                    placeLikelihood.getLikelihood()));
                            if (placeLikelihood.getLikelihood() >= 0.9 &&
                                    placeLikelihood.getPlace().getWebsiteUri() != null &&
                                    placeLikelihood.getPlace().getPhoneNumber() != null) {
                                setPlace(placeLikelihood.getPlace());
                                break;
                            }
                        }
                        placeLikelihoods.release();
                    }
                });
            }
        }

        // replace this in the info window
        void setPlace(Place place) {
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.street_snapshot:
                    Intent intent = new Intent(getContext(), StreetViewActivity.class);
                    intent.putExtra("location", Utils.getLatLng(mapActivity.getLocation()));
                    startActivity(intent);
                    break;
                case R.id.post_facebook:
                    if (ShareDialog.canShow(ShareLinkContent.class)) {
                        String url = String.format(getResources().getString(R.string.google_map_url),
                                mapActivity.getLocation().getLatitude(),
                                mapActivity.getLocation().getLongitude());
                        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                .setContentUrl(Uri.parse(url))
                                .build();
                        mapActivity.shareDialog.show(linkContent);
                    }
                    break;
                case R.id.more_info:
                    Toast.makeText(getContext(), "moreinfo", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.save_to_map:
                    Toast.makeText(getContext(), "savetomap", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        void setMapActivity(MapActivity mapActivity) { this.mapActivity = mapActivity; }
    }

    private FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onCancel() {
        }

        @Override
        public void onError(FacebookException error) {
            Toast.makeText(getApplicationContext(),
                    String.format("Error: %s", error.toString()), Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onSuccess(Sharer.Result result) {
            Toast.makeText(getApplicationContext(),
                    "Successfully posted to facebook", Toast.LENGTH_SHORT).show();
        }
    };

}

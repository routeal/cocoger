package com.routeal.cocoger.ui.main;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
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
import com.routeal.cocoger.service.MainService;
import com.routeal.cocoger.util.CircleTransform;
import com.routeal.cocoger.util.PicassoMarker;
import com.routeal.cocoger.util.Utils;
import com.squareup.picasso.Picasso;

public class MapActivity extends MapBaseActivity {

    public static final String USER_AVAILABLE = "user_available";
    public static final String LAST_LOCATION_UPDATE = "last_location_update";

    private final static String TAG = "MapActivity";
    private final static String KEY_CAMERA_POSITION = "camera_position";
    private final static String KEY_LOCATION = "location";

    private final static int DEFAULT_ZOOM = 15;

    private GoogleMap mMap;

    private View mapView;

    private CameraPosition mCameraPosition;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    private ProgressDialog spinner;

    private Marker myMarker;

    // drawing target
    private PicassoMarker myMarkerTarget;

    private Address mAddress;

    private InfoWindow.MarkerSpecification markerSpec;

    private MyInfoFragment myInfoFragment;

    private InfoWindowManager infoWindowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init the InfoWindow
        int offsetX = (int) getResources().getDimension(R.dimen.marker_offset_x);
        int offsetY = (int) getResources().getDimension(R.dimen.marker_offset_y);
        markerSpec = new InfoWindow.MarkerSpecification(offsetX, offsetY);

        // retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // set up the 'my' location button
        Drawable drawable = Utils.getIconDrawable(this, R.drawable.ic_my_location_white_36dp, R.color.gray);
        FloatingActionButton myLocationButton = (FloatingActionButton) findViewById(R.id.my_location);
        myLocationButton.setImageDrawable(drawable);
        myLocationButton.setOnClickListener(myLocationButtonListener);

        // registers the receiver to receive the location updates from the service
        IntentFilter filter = new IntentFilter();
        filter.addAction(MapActivity.LAST_LOCATION_UPDATE);
        filter.addAction(MapActivity.USER_AVAILABLE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalLocationReceiver, filter);
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

    void startApp() {
        // show the spinner
        spinner = Utils.getBusySpinner(this);

        MainService.setForegroundMode();
        MainService.start(getApplicationContext());

        MapInfoWindowFragment mapInfoWindowFragment =
                (MapInfoWindowFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        infoWindowManager = mapInfoWindowFragment.infoWindowManager();
        infoWindowManager.setHideOnFling(true);
        mapView = mapInfoWindowFragment.getView();

        // spinner will be dismissed in the MapReady callback
        mapInfoWindowFragment.getMapAsync(mReadyCallback);
    }

    OnMapReadyCallback mReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            mMap.setOnMarkerClickListener(mMarkerClickListener);

            // Get the current location from the base object
            mLastKnownLocation = getDeviceLocation();

            // get the current location from the service
            if (mLastKnownLocation == null) {
                mLastKnownLocation = MainService.getLastLocation();
            }

            if (mCameraPosition != null) {
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            } else if (mLastKnownLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        Utils.getLatLng(mLastKnownLocation), DEFAULT_ZOOM));
            }

            if (mLastKnownLocation != null) {
                setupMyLocationMarker(mLastKnownLocation);
            }

            spinner.dismiss();
            spinner = null;
        }
    };

    /**
     * Receives location updates from the location service
     */
    private BroadcastReceiver mLocalLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MapActivity.LAST_LOCATION_UPDATE)) {
                Location location = intent.getParcelableExtra(MainService.LOCATION_UPDATE);

                if (location != null) {
                    // first time only
                    if (mLastKnownLocation == null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                Utils.getLatLng(location), DEFAULT_ZOOM));
                        setupMyLocationMarker(location);
                    }

                    if (myMarker != null) {
                        myMarker.setPosition(Utils.getLatLng(location));
                    }

                    mLastKnownLocation = location;
                }

                mAddress = intent.getParcelableExtra(MainService.ADDRESS_UPDATE);
            } else if (intent.getAction().equals(MapActivity.USER_AVAILABLE)) {
                if (MainApplication.getUser() != null && myMarkerTarget != null) {
                    Picasso.with(getApplicationContext())
                            .load(MainApplication.getUser().getPicture())
                            .transform(new CircleTransform())
                            .into(myMarkerTarget);
                }
            }
        }
    };

    private void setupMyLocationMarker(Location location) {
        // my location marker
        MarkerOptions options = new MarkerOptions().position(Utils.getLatLng(location));
        myMarker = mMap.addMarker(options);
        myMarkerTarget = new PicassoMarker(myMarker);
        // sometimes the user won't be available when the process comes here faster than
        // etting the user from the firebase database
        if (MainApplication.getUser() != null) {
            Picasso.with(getApplicationContext())
                    .load(MainApplication.getUser().getPicture())
                    .transform(new CircleTransform())
                    .into(myMarkerTarget);
        }
        myInfoFragment = new MyInfoFragment();
        myInfoFragment.setMapActivity(this);
    }

    GoogleMap.OnMarkerClickListener mMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            if (marker.getId().compareTo(myMarker.getId()) == 0) {
                InfoWindow infoWindow = new InfoWindow(myMarker, markerSpec, myInfoFragment);
                infoWindowManager.toggle(infoWindow);
            }
            return true;
        }
    };

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

        @SuppressWarnings("MissingPermission")
        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            Log.d(TAG, "MyInfoFragment: onViewCreated");

            String url = String.format(getResources().getString(R.string.street_view_image_url),
                    mapActivity.mLastKnownLocation.getLatitude(),
                    mapActivity.mLastKnownLocation.getLongitude());

            Picasso.with(getContext())
                    .load(url)
                    .resize(96, 96)
                    .into(street_snapshot);

            User user = MainApplication.getUser();
            if (user != null) {
                name.setText(user.getDisplayName());
            }

            if (mapActivity.mAddress != null) {
                String address = mapActivity.mAddress.getAddressLine(0);
                if (address != null) {
                    current_address.setText(address);
                }
            }

            GoogleApiClient mGoogleApiClient = MainService.getGoogleApiClient();

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
                    intent.putExtra("location", Utils.getLatLng(mapActivity.mLastKnownLocation));
                    startActivity(intent);
                    break;
                case R.id.post_facebook:
                    break;
                case R.id.more_info:
                    Toast.makeText(getContext(), "moreinfo", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.save_to_map:
                    Toast.makeText(getContext(), "savetomap", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        void setMapActivity(MapActivity mapActivity) {
            this.mapActivity = mapActivity;
        }
    }

}

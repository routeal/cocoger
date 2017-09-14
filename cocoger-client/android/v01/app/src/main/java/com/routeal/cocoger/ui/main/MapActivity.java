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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.service.MainService;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.Utils;

import java.util.Iterator;
import java.util.Map;

public class MapActivity extends MapBaseActivity {

    public static final String USER_AVAILABLE = "user_available";
    public static final String USER_LOCATION_UPDATE = "user_location_update";
    public final static String FRIEND_LOCATION_UPDATE = "friend_location_update";
    public final static String FRIEND_LOCATION_REMOVE = "friend_location_remove";
    public final static String FRIEND_RANGE_UPDATE = "friend_range_update";

    public final static String FRIEND_KEY = "friend_key";
    public final static String LOCATION_UPDATE = "location_update";
    public final static String ADDRESS_UPDATE = "address_update";

    private final static String TAG = "MapActivity";
    private final static String KEY_CAMERA_POSITION = "camera_position";
    private final static String KEY_LOCATION = "location";

    private final static int DEFAULT_ZOOM = 15;

    private GoogleMap mMap;

    private View mapView;

    private CameraPosition mCameraPosition;

    private Location mLastKnownLocation;

    private ProgressDialog mSpinner;

    private InfoWindowManager mInfoWindowManager;

    private boolean mHasFriendMarkers = false;

    private MarkerManager mMm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ensure that the service is started
        MainService.start(getApplicationContext());
        // set it in the foreground mode
        MainService.setForegroundMode();

        // retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // set up the 'my' location button
        Drawable drawable = Utils.getIconDrawable(this, R.drawable.ic_my_location_white_36dp,
                R.color.gray);
        FloatingActionButton myLocationButton =
                (FloatingActionButton) findViewById(R.id.my_location);
        myLocationButton.setImageDrawable(drawable);
        myLocationButton.setOnClickListener(myLocationButtonListener);

        // registers the receiver to receive the location updates from the service
        IntentFilter filter = new IntentFilter();
        filter.addAction(MapActivity.USER_AVAILABLE);
        filter.addAction(MapActivity.USER_LOCATION_UPDATE);
        filter.addAction(MapActivity.FRIEND_LOCATION_UPDATE);
        filter.addAction(MapActivity.FRIEND_LOCATION_REMOVE);
        filter.addAction(MapActivity.FRIEND_RANGE_UPDATE);
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
        // show the mSpinner
        mSpinner = Utils.getBusySpinner(this);

        // set up the info window manager
        MapInfoWindowFragment mapInfoWindowFragment =
                (MapInfoWindowFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mInfoWindowManager = mapInfoWindowFragment.infoWindowManager();
        mInfoWindowManager.setHideOnFling(true);
        mapView = mapInfoWindowFragment.getView();

        // mSpinner will be dismissed in the MapReady callback
        mapInfoWindowFragment.getMapAsync(mReadyCallback);
    }

    OnMapReadyCallback mReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            mMm = new MarkerManager(mMap, mInfoWindowManager);

            // Get the current location from the base object
            mLastKnownLocation = getDeviceLocation();
            Log.d(TAG, "onMapReady: location detected from the base object");

            // the location may not be available at this point
            if (mLastKnownLocation != null) {
                Log.d(TAG, "onMapReady: setupMarkers");
                setupMarkers(mLastKnownLocation, Utils.getAddress(mLastKnownLocation));
            }

            if (mCameraPosition != null) {
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            } else if (mLastKnownLocation != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        Utils.getLatLng(mLastKnownLocation), DEFAULT_ZOOM));
            }

            mSpinner.dismiss();
            mSpinner = null;
        }
    };

    /**
     * Receives location updates from the location service
     */
    private BroadcastReceiver mLocalLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MapActivity.USER_LOCATION_UPDATE)) {
                Address address = intent.getParcelableExtra(MapActivity.ADDRESS_UPDATE);
                Location location = intent.getParcelableExtra(MapActivity.LOCATION_UPDATE);
                if (location == null || address == null) {
                    return;
                }
                if (mMap != null) {
                    // first time only
                    if (mLastKnownLocation == null) {
                        Log.d(TAG, "Receive Last_location_update: setupMarkers");
                        setupMarkers(location, address);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                Utils.getLatLng(location), DEFAULT_ZOOM));
                    } else {
                        mMm.reposition(FB.getUid(), location, address, LocationRange.CURRENT.range);
                    }
                }
                mLastKnownLocation = location;
            } else if (intent.getAction().equals(MapActivity.USER_AVAILABLE)) {
                Log.d(TAG, "Receive User_available: setupMarkers");
                setupMarkers(mLastKnownLocation, Utils.getAddress(mLastKnownLocation));
            } else if (intent.getAction().equals(MapActivity.FRIEND_LOCATION_UPDATE)) {
                String fid = intent.getStringExtra(MapActivity.FRIEND_KEY);
                if (fid == null) {
                    return;
                }
                Friend friend = null;
                User user = MainApplication.getUser();
                if (user != null && user.getFriends() != null) {
                    friend = user.getFriends().get(fid);
                    if (friend == null || friend.getLocation() == null) {
                        return;
                    }
                }
                final int range = friend.getRange();
                FB.getLocation(friend.getLocation(), new FB.LocationListener() {
                    @Override
                    public void onFail(String err) {
                    }

                    @Override
                    public void onSuccess(String key, LocationAddress location) {
                        mMm.reposition(key, Utils.getLocation(location), Utils.getAddress(location), range);
                    }
                });
            } else if (intent.getAction().equals(MapActivity.FRIEND_LOCATION_REMOVE)) {
                String fid = intent.getStringExtra(MapActivity.FRIEND_KEY);
                if (fid == null) {
                    return;
                }
                mMm.remove(fid);
            } else if (intent.getAction().equals(MapActivity.FRIEND_RANGE_UPDATE)) {
                String fid = intent.getStringExtra(MapActivity.FRIEND_KEY);
                if (fid == null) {
                    return;
                }
                Friend friend = null;
                User user = MainApplication.getUser();
                if (user != null && user.getFriends() != null) {
                    friend = user.getFriends().get(fid);
                    if (friend == null || friend.getLocation() == null) {
                        return;
                    }
                }
                int range = friend.getRange();
                mMm.reposition(fid, range);
            }
        }
    };

    private void setupMarkers(Location location, Address address) {
        if (location == null) return;

        // run only once
        if (mHasFriendMarkers) return;

        User user = MainApplication.getUser();
        if (user == null) {
            Log.d(TAG, "setupMarkers: user not available");
            return;
        }

        Log.d(TAG, "setupMarkers: start processing");

        mHasFriendMarkers = true;

        mMm.add(FB.getUid(), user.getDisplayName(), user.getPicture(), location, address,
                LocationRange.CURRENT.range);

        Map<String, Friend> friends = user.getFriends();
        if (friends == null || friends.isEmpty()) {
            Log.d(TAG, "setupMarkers: empty friend");
            return;
        }

        Iterator<String> it = friends.keySet().iterator();

        while (it.hasNext()) {
            final Friend friend = friends.get(it.next());
            FB.getLocation(friend.getLocation(), new FB.LocationListener() {
                @Override
                public void onSuccess(String key, LocationAddress location) {
                    Location l = Utils.getLocation(location);
                    Address a = Utils.getAddress(location);
                    mMm.add(key, friend.getDisplayName(), friend.getPicture(), l, a, friend.getRange());
                }

                @Override
                public void onFail(String err) {
                }
            });
        }
    }
}

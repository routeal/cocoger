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
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.service.MainService;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.Utils;

public class MapActivity extends MapBaseActivity {

    public static final String USER_AVAILABLE = "user_available";
    public static final String USER_LOCATION_UPDATE = "user_location_update";
    public final static String FRIEND_LOCATION_UPDATE = "friend_location_update";
    public final static String FRIEND_LOCATION_REMOVE = "friend_location_remove";
    public final static String FRIEND_RANGE_UPDATE = "friend_range_update";
    public final static String FRIEND_MARKER_SHOW = "friend_marker_show";
    public final static String DIRECTION_ROUTE_DRAW = "direction_route_draw";
    public final static String DIRECTION_ROUTE_REMOVE = "direction_route_remove";

    public final static String FRIEND_KEY = "friend_key";
    public final static String LOCATION_DATA = "location_data";
    public final static String ADDRESS_DATA = "address_data";

    private final static String TAG = "MapActivity";
    private final static String KEY_CAMERA_POSITION = "camera_position";
    private final static String KEY_LOCATION = "location";

    private final static int DEFAULT_ZOOM = 16;

    protected GoogleMap mMap;

    private View mapView;

    private CameraPosition mCameraPosition;

    private ProgressDialog mSpinner;

    private InfoWindowManager mInfoWindowManager;

    private MarkerManager mMm;

    private SimpleDirection mDirection;

    private MapStyle mMapStyle;

    private PoiManager mPoi;

    private OnMapReadyCallback mReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            mMapStyle = new MapStyle(mMap, MapActivity.this);
            mMm = new MarkerManager(mMap, mInfoWindowManager);
            mDirection = new SimpleDirection(mMap, mInfoWindowManager);
            mPoi = new PoiManager(mMap);

            MapActivity.this.onMapReady();

            mMap.setPadding(8, 0, 0, 148);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.setMyLocationEnabled(true);

            mMapStyle.init(MapActivity.this, mMap);

            Log.d(TAG, "onMapReady: location detected from the base object");

            // the location may not be available at this point
            if (getLocation() != null) {
                Log.d(TAG, "onMapReady: setupMarkers");
                mMm.setupMarkers(getLocation(), Utils.getAddress(getLocation()));
            }

            if (mCameraPosition != null) {
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            } else if (getLocation() != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        Utils.getLatLng(getLocation()), DEFAULT_ZOOM));
            }

            mSpinner.dismiss();
            mSpinner = null;
        }
    };

    private View.OnClickListener myLocationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    Utils.getLatLng(getLocation()), DEFAULT_ZOOM));
        }
    };

    /**
     * Receives location updates from the location service
     */
    private BroadcastReceiver mLocalLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MapActivity.USER_LOCATION_UPDATE)) {
                Address address = intent.getParcelableExtra(MapActivity.ADDRESS_DATA);
                Location location = intent.getParcelableExtra(MapActivity.LOCATION_DATA);
                if (location == null || address == null) {
                    return;
                }
                if (mMap != null) {
                    // first time only
                    if (getLocation() == null) {
                        Log.d(TAG, "Receive Last_location_update: setupMarkers");
                        if (MainApplication.getUser() != null) {
                            mMm.setupMarkers(location, address);
                        }
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                Utils.getLatLng(location), DEFAULT_ZOOM));
                    } else {
                        if (MainApplication.getUser() != null) {
                            mMm.reposition(FB.getUid(), location, address, LocationRange.CURRENT.range);
                        }
                    }
                }
                setLocation(location);
            } else if (intent.getAction().equals(MapActivity.USER_AVAILABLE)) {
                Log.d(TAG, "Receive User_available: setupMarkers");
                mMm.setupMarkers(getLocation(), Utils.getAddress(getLocation()));
            } else if (intent.getAction().equals(MapActivity.FRIEND_LOCATION_UPDATE)) {
                final String fid = intent.getStringExtra(MapActivity.FRIEND_KEY);
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
                    public void onSuccess(Location location, Address address) {
                        mMm.reposition(fid, location, address, range);
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
                Log.d(TAG, "FRIEND_RANGE_UPDATE:" + fid);
                mMm.reposition(fid, range);
            } else if (intent.getAction().equals(MapActivity.FRIEND_MARKER_SHOW)) {
                String fid = intent.getStringExtra(MapActivity.FRIEND_KEY);
                if (fid == null) {
                    return;
                }
                Log.d(TAG, "FRIEND_MARKER_SHOW:" + fid);
                mMm.show(fid);
                closeSlidePanel();
            } else if (intent.getAction().equals(MapActivity.DIRECTION_ROUTE_DRAW)) {
                Location location = intent.getParcelableExtra(MapActivity.LOCATION_DATA);
                mDirection.addDirection(location, getLocation());
                closeSlidePanel();
            } else if (intent.getAction().equals(MapActivity.DIRECTION_ROUTE_REMOVE)) {
                mDirection.removeDirection();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ensure that the service is started
        //MainService.start(getApplicationContext());
        // set it in the foreground mode
        MainService.setForegroundMode();

        // retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            setLocation((Location) savedInstanceState.getParcelable(KEY_LOCATION));
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // set up the 'my' location button
        Drawable myLocationDrawable = Utils.getIconDrawable(this, R.drawable.ic_my_location_white_36dp, R.color.gray);
        FloatingActionButton myLocationButton = (FloatingActionButton) findViewById(R.id.my_location);
        myLocationButton.setImageDrawable(myLocationDrawable);
        myLocationButton.setOnClickListener(myLocationButtonListener);

        // registers the receiver to receive the location updates from the service
        IntentFilter filter = new IntentFilter();
        filter.addAction(MapActivity.USER_AVAILABLE);
        filter.addAction(MapActivity.USER_LOCATION_UPDATE);
        filter.addAction(MapActivity.FRIEND_LOCATION_UPDATE);
        filter.addAction(MapActivity.FRIEND_LOCATION_REMOVE);
        filter.addAction(MapActivity.FRIEND_RANGE_UPDATE);
        filter.addAction(MapActivity.FRIEND_MARKER_SHOW);
        filter.addAction(MapActivity.DIRECTION_ROUTE_DRAW);
        filter.addAction(MapActivity.DIRECTION_ROUTE_REMOVE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalLocationReceiver, filter);
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, getLocation());
            super.onSaveInstanceState(outState);
        }
    }

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

    void onMapReady() {
    }

    void closeSlidePanel() {
    }
}

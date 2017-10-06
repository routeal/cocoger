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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.Utils;

class MapActivity extends MapBaseActivity {

    private final static String TAG = "MapActivity";
    private final static String KEY_CAMERA_POSITION = "camera_position";
    private final static String KEY_LOCATION = "location";

    private final static int DEFAULT_ZOOM = 16;

    protected GoogleMap mMap;

    protected GeoDataClient mGeoDataClient;

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

            mGeoDataClient = Places.getGeoDataClient(MapActivity.this, null);
            mMapStyle = new MapStyle(mMap, MapActivity.this);
            mMm = new MarkerManager(mMap, mInfoWindowManager);
            mDirection = new SimpleDirection(mMap, mInfoWindowManager);
            mPoi = new PoiManager(mMap, mGeoDataClient, mInfoWindowManager);

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
            if (intent.getAction().equals(FB.USER_LOCATION_UPDATE)) {
                Address address = intent.getParcelableExtra(FB.NEW_ADDRESS);
                Location location = intent.getParcelableExtra(FB.NEW_LOCATION);
                if (location == null || address == null) {
                    return;
                }
                if (mMap != null) {
                    // first time only
                    if (getLocation() == null) {
                        Log.d(TAG, "Receive Last_location_update: setupMarkers");
                        if (FB.getUser() != null) {
                            mMm.setupMarkers(location, address);
                        }
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                Utils.getLatLng(location), DEFAULT_ZOOM));
                    } else {
                        if (FB.getUser() != null) {
                            mMm.reposition(FB.getUid(), location, address, LocationRange.CURRENT.range);
                        }
                    }
                }
                setLocation(location);
                setAddress(address);
            } else if (intent.getAction().equals(FB.USER_AVAILABLE)) {
                Log.d(TAG, "Receive User_available: setupMarkers");
                if (mMm != null) {
                    if (getAddress() == null) {
                        setAddress(Utils.getAddress(getLocation()));
                    }
                    mMm.setupMarkers(getLocation(), getAddress());
                }
            } else if (intent.getAction().equals(FB.FRIEND_LOCATION_UPDATE)) {
                final String fid = intent.getStringExtra(FB.FRIEND_KEY);
                final String newLocationKey = intent.getStringExtra(FB.NEW_LOCATION);
                final String oldLocationKey = intent.getStringExtra(FB.OLD_LOCATION);
                final Friend friend = FB.getFriend(fid);
                if (friend == null) return; // shouldn't happen
                final int range = friend.getRange();
                FB.getLocation(newLocationKey, new FB.LocationListener() {
                    @Override
                    public void onFail(String err) {
                        Log.d(TAG, "Friend new location: " + err);
                    }

                    @Override
                    public void onSuccess(Location newLocation, final Address newAddress) {
                        // move the cursor
                        mMm.reposition(fid, newLocation, newAddress, range);
                        // compare the new location with the old location to issue the range movement
                        FB.getLocation(oldLocationKey, new FB.LocationListener() {
                            @Override
                            public void onFail(String err) {
                                Log.d(TAG, "Friend old location: " + err);
                            }

                            @Override
                            public void onSuccess(Location oldLocation, Address oldAddress) {
                                // detect move in the range or above
                                // TODO
                                /*
                                int movedRange = Utils.detectRangeMove(newAddress, oldAddress, range);
                                if (movedRange > 0) {
                                    // if the address is the same as the user, send a notification
                                    if (Utils.isEqualAddress(newAddress, getAddress(), range)) {
                                        // send notification
                                        int nid = Math.abs((int) friend.getCreated());
                                        String message = Utils.getRangeMoveMessage(friend, newAddress, oldAddress);
                                        Notifi.send(nid, friend.getDisplayName(), message, friend.getPicture());
                                    }
                                }
                                */
                            }
                        });
                    }
                });
            } else if (intent.getAction().equals(FB.FRIEND_LOCATION_REMOVE)) {
                String fid = intent.getStringExtra(FB.FRIEND_KEY);
                if (fid == null) {
                    return;
                }
                mMm.remove(fid);
            } else if (intent.getAction().equals(FB.FRIEND_RANGE_UPDATE)) {
                String fid = intent.getStringExtra(FB.FRIEND_KEY);
                if (fid == null) {
                    return;
                }
                Friend friend = null;
                User user = FB.getUser();
                if (user != null && user.getFriends() != null) {
                    friend = user.getFriends().get(fid);
                    if (friend == null || friend.getLocation() == null) {
                        return;
                    }
                }
                int range = friend.getRange();
                Log.d(TAG, "FRIEND_RANGE_UPDATE:" + fid);
                mMm.reposition(fid, range);
            } else if (intent.getAction().equals(FB.FRIEND_MARKER_SHOW)) {
                String fid = intent.getStringExtra(FB.FRIEND_KEY);
                if (fid == null) {
                    return;
                }
                Log.d(TAG, "FRIEND_MARKER_SHOW:" + fid);
                mMm.show(fid);
                closeSlidePanel();
            } else if (intent.getAction().equals(FB.DIRECTION_ROUTE_ADD)) {
                Location location = intent.getParcelableExtra(FB.NEW_LOCATION);
                mDirection.addDirection(MapActivity.this, location, getLocation());
                closeSlidePanel();
            } else if (intent.getAction().equals(FB.DIRECTION_ROUTE_REMOVE)) {
                mDirection.removeDirection();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        filter.addAction(FB.USER_AVAILABLE);
        filter.addAction(FB.USER_LOCATION_UPDATE);
        filter.addAction(FB.FRIEND_LOCATION_UPDATE);
        filter.addAction(FB.FRIEND_LOCATION_REMOVE);
        filter.addAction(FB.FRIEND_RANGE_UPDATE);
        filter.addAction(FB.FRIEND_MARKER_SHOW);
        filter.addAction(FB.DIRECTION_ROUTE_ADD);
        filter.addAction(FB.DIRECTION_ROUTE_REMOVE);
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
        mInfoWindowManager.setWindowShowListener(new InfoWindowManager.WindowShowListener() {
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
                Fragment fragment = infoWindow.getWindowFragment();
                if (fragment != null) {
                    if (fragment instanceof PoiInfoFragment) {
                        Log.d(TAG, "InfoWindowManager:onWindowHidden=PoiInfoFragment");
                        mPoi.removeInfoWindow();
                    }
                }
            }
        });
        mapView = mapInfoWindowFragment.getView();

        // mSpinner will be dismissed in the MapReady callback
        mapInfoWindowFragment.getMapAsync(mReadyCallback);
    }

    void onMapReady() {
    }

    void closeSlidePanel() {
    }
}

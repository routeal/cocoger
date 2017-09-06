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

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.service.MainService;
import com.routeal.cocoger.util.LoadImage;
import com.routeal.cocoger.util.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MapActivity extends MapBaseActivity {

    public static final String USER_AVAILABLE = "user_available";
    public static final String LAST_LOCATION_UPDATE = "last_location_update";

    private final static String TAG = "MapActivity";
    private final static String KEY_CAMERA_POSITION = "camera_position";
    private final static String KEY_LOCATION = "location";

    private final static int DEFAULT_ZOOM = 15;

    private final static int MARKER_SZIE = 128;

    private GoogleMap mMap;

    private View mapView;

    private CameraPosition mCameraPosition;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    private ProgressDialog spinner;

    private InfoWindow.MarkerSpecification mMarkerOffset;

    private BaseMarker myMarker;

    //private MyInfoFragment myInfoFragment;

    private InfoWindowManager infoWindowManager;

    private Map<String, BaseMarker> mMarkers = new HashMap<>();

    private boolean mHasFriendMarkers = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init the InfoWindow
        int offsetX = (int) getResources().getDimension(R.dimen.marker_offset_x);
        int offsetY = (int) getResources().getDimension(R.dimen.marker_offset_y);
        mMarkerOffset = new InfoWindow.MarkerSpecification(offsetX, offsetY);

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
        filter.addAction(MapActivity.LAST_LOCATION_UPDATE);
        filter.addAction(MapActivity.USER_AVAILABLE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalLocationReceiver, filter);

        // my marker
        myMarker = new BaseMarker();
        mMarkers.put(FB.getUid(), myMarker);

        //myInfoFragment = new MyInfoFragment();
        //myInfoFragment.setMapActivity(MapActivity.this);
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

            // NOTE:
            // Both location and user may not be available at this point.

            // Get the current location from the base object
            mLastKnownLocation = getDeviceLocation();

            // get the current location from the service if the
            // location is not available from the base object
            if (mLastKnownLocation == null) {
                mLastKnownLocation = MainService.getLastLocation();
            }

            if (mCameraPosition != null) {
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            } else if (mLastKnownLocation != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        Utils.getLatLng(mLastKnownLocation), DEFAULT_ZOOM));
            }

            if (mLastKnownLocation != null) {
                myMarker.setPosition(mLastKnownLocation);
            }

            User user = MainApplication.getUser();
            if (user != null) {
                myMarker.addUser(FB.getUid(), user.getDisplayName(), user.getPicture());
            }

            // this needs both the user object and the current user location.
            setupFriendMarkers();

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
                    }
                    myMarker.setPosition(location);
                    mLastKnownLocation = location;
                }
                Address address = intent.getParcelableExtra(MainService.ADDRESS_UPDATE);
                if (address != null) {
                    myMarker.setAddress(address);
                }
            } else if (intent.getAction().equals(MapActivity.USER_AVAILABLE)) {
                User user = MainApplication.getUser();
                if (user != null) {
                    myMarker.addUser(FB.getUid(), user.getDisplayName(), user.getPicture());
                }
            }

            // this is usually done at the initialization
            // but in case it is missed during the initialization
            setupFriendMarkers();
        }
    };

    GoogleMap.OnMarkerClickListener mMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            for (Map.Entry<String, BaseMarker> entry : mMarkers.entrySet()) {
                BaseMarker bm = entry.getValue();
                if (bm.onMarkerClick(marker)) {
                    return true;
                }
            }
            return false;
        }
    };

    class BaseMarker {
        private Set<String> mIds = new HashSet<>();
        private Set<String> mNames = new HashSet<>();
        private Set<String> mPictures = new HashSet<>();
        private Marker mMarker;
        private boolean pictured = false;
        private LoadImage.LoadMarkerImage imageTask;
        private Address mAddress;

        void addUser(String id, String name, String picture) {
            int size = mPictures.size();
            mIds.add(id);
            mNames.add(name);
            mPictures.add(picture);
            if (size != mPictures.size()) {
                Log.d(TAG, "pic should be redraw");
                getPicture();
            }
        }

        void getPicture() {
            if (mMarker == null) return;
            String[] pictures = mPictures.toArray(new String[0]);
            if (pictures.length > 0) {
                pictured = true;
                if (imageTask != null) {
                    imageTask.cancel(true);
                    imageTask = null;
                }
                imageTask = new LoadImage.LoadMarkerImage(mMarker);
                imageTask.execute(pictures);
            }
        }

        boolean onMarkerClick(Marker marker) {
            if (marker.getId().compareTo(mMarker.getId()) == 0) {
                String[] ids = mIds.toArray(new String[0]);
                String[] names = mNames.toArray(new String[0]);
                if (ids.length == 1) {
                    Bundle args = new Bundle();
                    args.putString("id", ids[0]);
                    args.putParcelable("location", getLocation());
                    args.putParcelable("address", mAddress);
                    args.putString("name", names[0]);
                    SingleInfoFragment info = new SingleInfoFragment();
                    info.setArguments(args);
                    InfoWindow infoWindow = new InfoWindow(mMarker, mMarkerOffset, info);
                    infoWindowManager.toggle(infoWindow);
                } else if (mIds.size() > 1) {
                    Log.d(TAG, "many user's info window");
                }
                return true;
            }
            return false;
        }

        void setPosition(Location location) {
            if (mMarker == null) {
                MarkerOptions options = new MarkerOptions().position(Utils.getLatLng(location));
                mMarker = mMap.addMarker(options);
            }
            mMarker.setPosition(Utils.getLatLng(location));
            if (!pictured) getPicture();
        }

        void setAddress(Address address) {
            mAddress = address;
        }

        Location getLocation() {
            if (mMarker == null) {
                return null;
            }
            return Utils.getLocation(mMarker.getPosition());
        }
    }

    // we call this until it is succeeded.
    private void setupFriendMarkers() {
        final User user = MainApplication.getUser();
        if (user == null) return;

        if (myMarker.getLocation() == null) return;

        if (mHasFriendMarkers) return;
        mHasFriendMarkers = true;

        Map<String, Friend> friends = user.getFriends();
        if (friends == null || friends.isEmpty()) return;

        Iterator<String> it = friends.keySet().iterator();

        while (it.hasNext()) {
            final String key = it.next();
            final Friend friend = friends.get(key);

            FB.getLocation(friend.getLocation(), new FB.LocationListener() {
                @Override
                public void onSuccess(LocationAddress location) {
                    Location l = Utils.getLocation(location);

                    // check the buddies nearby
                    for (Map.Entry<String, BaseMarker> entry : mMarkers.entrySet()) {
                        BaseMarker bm = entry.getValue();
                        if (l.distanceTo(bm.getLocation()) < 5.0) {
                            Log.d(TAG, "pic added");
                            bm.addUser(key, friend.getDisplayName(), friend.getPicture());
                            return;
                        }
                    }

                    BaseMarker m = new BaseMarker();
                    m.setPosition(l);
                    m.setAddress(Utils.getAddress(location));
                    m.addUser(key, friend.getDisplayName(), friend.getPicture());
                    mMarkers.put(key, m);
                }

                @Override
                public void onFail(String err) {
                }
            });
        }
    }
}

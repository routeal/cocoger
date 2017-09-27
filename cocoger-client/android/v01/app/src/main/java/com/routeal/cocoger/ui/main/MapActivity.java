package com.routeal.cocoger.ui.main;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.service.MainService;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.Utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    private boolean mHasFriendMarkers = false;

    private MarkerManager mMm;

    class SimpleDirectionRoute {
        Polyline line;
        InfoWindow window;
        Marker marker;
    }

    private SimpleDirectionRoute mDirectionRoute = new SimpleDirectionRoute();

    class CustomMapStyle {
        SwitchCompat view;
        int id;
        String resource;
    }

    CustomMapStyle mMapStyles[];

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

        // set up the 'my' location button
        Drawable layerDrawable = Utils.getIconDrawable(this, R.drawable.ic_layers_white_24dp, R.color.gray);
        FloatingActionButton layerButton = (FloatingActionButton) findViewById(R.id.map_layer);
        layerButton.setImageDrawable(layerDrawable);
        layerButton.setOnClickListener(mapLayerButtonListener);

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

        mMapStyles = new CustomMapStyle[7];
        mMapStyles[0] = new CustomMapStyle();
        mMapStyles[0].id = R.raw.mapstyle_retro;
        mMapStyles[0].resource = "retro";
        mMapStyles[1] = new CustomMapStyle();
        mMapStyles[1].id = R.raw.mapstyle_night;
        mMapStyles[1].resource = "night";
        mMapStyles[2] = new CustomMapStyle();
        mMapStyles[2].id = R.raw.mapstyle_gray;
        mMapStyles[2].resource = "gray";
        mMapStyles[3] = new CustomMapStyle();
        mMapStyles[3].id = R.raw.mapstyle_blue;
        mMapStyles[3].resource = "blue";
        mMapStyles[4] = new CustomMapStyle();
        mMapStyles[4].id = R.raw.mapstyle_slate;
        mMapStyles[4].resource = "slate";
        mMapStyles[5] = new CustomMapStyle();
        mMapStyles[5].id = R.raw.mapstyle_white;
        mMapStyles[5].resource = "white";
        mMapStyles[6] = new CustomMapStyle();
        mMapStyles[6].id = R.raw.mapstyle_pink;
        mMapStyles[6].resource = "pink";
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

    private View.OnClickListener myLocationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    Utils.getLatLng(getLocation()), DEFAULT_ZOOM));
        }
    };

    private View.OnClickListener mapLayerButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LayoutInflater layoutInflaterAndroid = LayoutInflater.from(MapActivity.this);
            View view = layoutInflaterAndroid.inflate(R.layout.dialog_layer, null);

            mMapStyles[0].view = (SwitchCompat) view.findViewById(R.id.switch_retro);
            mMapStyles[1].view = (SwitchCompat) view.findViewById(R.id.switch_night);
            mMapStyles[2].view = (SwitchCompat) view.findViewById(R.id.switch_grayscale);
            mMapStyles[3].view = (SwitchCompat) view.findViewById(R.id.switch_muted_blue);
            mMapStyles[4].view = (SwitchCompat) view.findViewById(R.id.switch_pale_down);
            mMapStyles[5].view = (SwitchCompat) view.findViewById(R.id.switch_paper);
            mMapStyles[6].view = (SwitchCompat) view.findViewById(R.id.switch_pinky);

            for (int i = 0; i < mMapStyles.length; i++) {
                String current_style = MainApplication.getString("style");
                if (current_style != null && !current_style.isEmpty()) {
                    if (mMapStyles[i].resource.equals(current_style)) {
                        mMapStyles[i].view.setChecked(true);
                    } else {
                        mMapStyles[i].view.setChecked(false);
                    }
                }
            }

            for (int i = 0; i < mMapStyles.length; i++) {
                final int n = i;
                mMapStyles[n].view.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            CustomMapStyle cstyle = mMapStyles[n];
                            for (int j = 0; j < mMapStyles.length; j++) {
                                if (cstyle.view != mMapStyles[j].view) {
                                    mMapStyles[j].view.setChecked(false);
                                }
                            }
                            MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(MapActivity.this, cstyle.id);
                            mMap.setMapStyle(style);
                            MainApplication.putString("style", cstyle.resource);
                        } else {
                            mMap.setMapStyle(null);
                            MainApplication.putString("style", "normal");
                        }
                    }
                });
            }

            final SwitchCompat traffic = (SwitchCompat) view.findViewById(R.id.switch_traffic);
            traffic.setChecked(MainApplication.getBool("traffic"));

            traffic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mMap.setTrafficEnabled(isChecked);
                    MainApplication.putBool("traffic", isChecked);
                }
            });

            AlertDialog dialog = new AlertDialog.Builder(MapActivity.this)
                    .setView(view)
                    .setCancelable(true)
                    .setNegativeButton(R.string.close, null)
                    .show();
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
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

    void onMapReady() {
    }

    void closeSlidePanel() {
    }

    OnMapReadyCallback mReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            mMm = new MarkerManager(mMap, mInfoWindowManager);

            boolean traffic_value = MainApplication.getBool("traffic");
            mMap.setTrafficEnabled(traffic_value);

            String current_style = MainApplication.getString("style");
            if (current_style == null || current_style.equals("normal")) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            } else {
                for (int i = 0; i < mMapStyles.length; i++) {
                    if (mMapStyles[i].resource.equals(current_style)) {
                        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(MapActivity.this, mMapStyles[i].id);
                        mMap.setMapStyle(style);
                        break;
                    }
                }
            }

            MapActivity.this.onMapReady();

            mMap.setPadding(8, 0, 0, 148);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.setMyLocationEnabled(true);

            mMap.setOnPoiClickListener(mPoiClickListener);
            mMap.setOnPolylineClickListener(mPolylineClickListener);

            Log.d(TAG, "onMapReady: location detected from the base object");

            // the location may not be available at this point
            if (getLocation() != null) {
                Log.d(TAG, "onMapReady: setupMarkers");
                setupMarkers(getLocation(), Utils.getAddress(getLocation()));
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
                            setupMarkers(location, address);
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
                setupMarkers(getLocation(), Utils.getAddress(getLocation()));
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
                drawDirection(location);
                closeSlidePanel();
            } else if (intent.getAction().equals(MapActivity.DIRECTION_ROUTE_REMOVE)) {
                cleanupDirection();
            }
        }
    };

    private void setupMarkers(Location location, Address address) {
        if (location == null) return;

        // run only once
        if (mHasFriendMarkers) return;

        // map is not ready
        if (mMm == null) return;

        User user = MainApplication.getUser();
        if (user == null) {
            Log.d(TAG, "setupMarkers: user not available");
            return;
        }

        // in the very first beginning, the location might not be set.
        if (user.getLocation() == null) {
            FB.saveLocation(location, address);
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
            final String key = it.next();
            final Friend friend = friends.get(key);
            if (friend.getLocation() != null) {
                FB.getLocation(friend.getLocation(), new FB.LocationListener() {
                    @Override
                    public void onSuccess(Location location, Address address) {
                        mMm.add(key, friend.getDisplayName(), friend.getPicture(),
                                location, address, friend.getRange());
                    }

                    @Override
                    public void onFail(String err) {
                    }
                });
            }
        }
    }

    GoogleMap.OnPoiClickListener mPoiClickListener = new GoogleMap.OnPoiClickListener() {
        @Override
        public void onPoiClick(PointOfInterest pointOfInterest) {
            Toast.makeText(getApplicationContext(), "Clicked: " +
                            pointOfInterest.name + "\nPlace ID:" + pointOfInterest.placeId +
                            "\nLatitude:" + pointOfInterest.latLng.latitude +
                            " Longitude:" + pointOfInterest.latLng.longitude,
                    Toast.LENGTH_LONG).show();
        }
    };

    void cleanupDirection() {
        if (mDirectionRoute.line != null) {
            mDirectionRoute.line.remove();
            mDirectionRoute.line = null;
        }
        if (mDirectionRoute.window != null) {
            mInfoWindowManager.hide(mDirectionRoute.window);
            mDirectionRoute.window = null;
        }
        if (mDirectionRoute.marker != null) {
            mDirectionRoute.marker.remove();
            mDirectionRoute.marker = null;
        }
    }

    void drawDirection(final Location locationTo) {
        cleanupDirection();
        SimpleDirection.getDirection(locationTo, getLocation(), new SimpleDirection.SimpleDirectionListener() {
            @Override
            public void onSuccess(List<SimpleDirection.Route> routes) {
                SimpleDirection.Route route = routes.get(0);
                PolylineOptions lineOptions = new PolylineOptions();
                lineOptions.addAll(route.points);
                lineOptions.width(10);
                lineOptions.color(R.color.red);
                mDirectionRoute.line = mMap.addPolyline(lineOptions);
                mDirectionRoute.line.setClickable(true);

                //
                int n = route.points.size() / 2;
                LatLng pos = route.points.get(n);
                Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.TRANSPARENT);
                BitmapDescriptor transparent = BitmapDescriptorFactory.fromBitmap(bitmap);

                MarkerOptions options = new MarkerOptions()
                        .position(pos)
                        .icon(transparent)
                        .anchor((float) 0.5, (float) 0.5);
                mDirectionRoute.marker = mMap.addMarker(options);

                // FIXME:
                Context context = MainApplication.getContext();
                int offsetX = (int) context.getResources().getDimension(R.dimen.marker_offset_x);
                int offsetY = (int) context.getResources().getDimension(R.dimen.marker_offset_y);
                InfoWindow.MarkerSpecification mMarkerOffset = new InfoWindow.MarkerSpecification(offsetX, offsetY);

                DirectionInfoFragment dif = new DirectionInfoFragment();
                dif.setDistance(route.distance);
                dif.setDuration(route.duration);
                dif.setDestination(locationTo);
                mDirectionRoute.window = new InfoWindow(mDirectionRoute.marker, mMarkerOffset, dif);
                mInfoWindowManager.setHideOnFling(true);
                mInfoWindowManager.show(mDirectionRoute.window, true);

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(Utils.getLatLng(locationTo));
                builder.include(Utils.getLatLng(getLocation()));
                LatLngBounds bounds = builder.build();
                int padding = 200; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.moveCamera(cu);
            }

            @Override
            public void onFail(String err) {
                String message = MapActivity.this.getResources().getString(R.string.no_direction_available);
                if (!err.isEmpty()) {
                    message += " (" + err + ")";
                }
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    GoogleMap.OnPolylineClickListener mPolylineClickListener = new GoogleMap.OnPolylineClickListener() {
        @Override
        public void onPolylineClick(Polyline polyline) {
            if (mDirectionRoute.line != null && mDirectionRoute.window != null) {
                if (polyline.getId().equals(mDirectionRoute.line.getId())) {
                    mInfoWindowManager.show(mDirectionRoute.window, true);
                }
            }
        }
    };
}

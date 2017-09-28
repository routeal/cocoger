package com.routeal.cocoger.service;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.provider.DBUtil;
import com.routeal.cocoger.ui.main.MapActivity;
import com.routeal.cocoger.util.Utils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;

/**
 * Created by hwatanabe on 4/11/17.
 */

public class MainService extends BasePeriodicService {

    enum LocationMode {
        NONE,
        BACKGROUND,
        FOREGROUND
    }

    private static final String TAG = "MainService";

    private final static long BACKGROUND_INTERVAL = 20000;

    private final static long FOREGROUND_INTERVAL = 2000;

    private final static int PAST_LOCATION_QUEUE_MAX = 10;

    private final static int PAST_LOCATION_QUEUE_SIZE = 6;

    private final static float FOREGROUND_MIN_MOVEMENT = 10.0f;

    private final static float BACKGROUND_MIN_MOVEMENT = 100.0f;

    private static long mServiceInterval = BACKGROUND_INTERVAL;

    private static LocationMode mRequestedLocationMode = LocationMode.BACKGROUND;

    private static MainService mActiveService;

    private LocationRequest mLocationRequest;

    private LocationMode mLocationMode = LocationMode.NONE;

    private Location mLastKnownLocation;

    private Address mLastKnownAddress;

    private GoogleApiClient mGoogleApi;

    private Geocoder mGeocoder;

    class PastLocation {
        float distance;
        Location location;

        PastLocation(float distance, Location location) {
            this.distance = distance;
            this.location = location;
        }
    }

    static class LocationAscendingOrder implements Comparator<PastLocation> {
        @Override
        public int compare(PastLocation o1, PastLocation o2) {
            return (int) (o1.distance - o2.distance);
        }
    }

    private static PriorityQueue<PastLocation> queue =
            new PriorityQueue<>(PAST_LOCATION_QUEUE_MAX, new LocationAscendingOrder());

    public static void setForegroundMode() {
        Log.d(TAG, "setForegroundMode");
        mRequestedLocationMode = LocationMode.FOREGROUND;
        mServiceInterval = FOREGROUND_INTERVAL;
        if (mActiveService != null) {
            mActiveService.execTask();
        }
    }

    public static void setBackgroundMode() {
//        Log.d(TAG, Log.getStackTraceString(new Exception()));
        Log.d(TAG, "setBackgroundMode");
        mRequestedLocationMode = LocationMode.BACKGROUND;
        mServiceInterval = BACKGROUND_INTERVAL;
        if (mActiveService != null) {
            mActiveService.execTask();
        }
    }

    public static void start(Context context) {
        // start the service only when the user is authenticated
        //if (FB.isAuthenticated()) {
        Intent intent = new Intent(context, MainService.class);
        context.startService(intent);
        //}
    }

    public static void stop() {
        if (mActiveService != null) {
            mActiveService.stopLocationUpdate();
            mActiveService.stopResident();
        }
        //mActiveService.mLocationMode = LocationMode.NONE;
        //mActiveService.mRequestedLocationMode = LocationMode.NONE;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // set up FB monitoring, in other words, this is an entry
        // point to FB
        FB.monitorAuthentication();
    }

    @Override
    public void onDestroy() {
        mGeocoder = null;
        mGoogleApi.disconnect();
        super.onDestroy();
    }

    private void connectGoogleApi() {
        if (mGeocoder == null) {
            mGeocoder = new Geocoder(this, Locale.getDefault());
        }

        if (mGoogleApi == null) {
            mGoogleApi = new GoogleApiClient.Builder(mActiveService)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }

        if (!(mGoogleApi.isConnected() || mGoogleApi.isConnecting())) {
            Log.d(TAG, "connecting google api client");
            mGoogleApi.connect();
        }
    }

    private void startLocationUpdate() {
        if (mGoogleApi == null || !mGoogleApi.isConnected()) {
            return;
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // do nothing
        } else {
            return;
        }

        if (!FB.isAuthenticated()) return;

        if (mLastKnownLocation == null) {
            mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApi);
        }

        if (mLocationMode == mRequestedLocationMode) return;

        Log.d(TAG, "startLocationUpdate");

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApi, mLocationListener);

        if (mRequestedLocationMode.equals(LocationMode.BACKGROUND)) {
            mLocationMode = LocationMode.BACKGROUND;

            mLocationRequest = LocationRequest.create()
                    .setInterval(15 * 60 * 1000) // 15 mins
                    .setFastestInterval(5 * 60000) // 5 min
                    //.setSmallestDisplacement(BACKGROUND_MIN_MOVEMENT)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApi,
                    mLocationRequest, mLocationListener);

            Log.d(TAG, "start background LocationUpdate");
        } else if (mRequestedLocationMode.equals(LocationMode.FOREGROUND)) {
            mLocationMode = LocationMode.FOREGROUND;

            mLocationRequest = LocationRequest.create()
                    .setInterval(60 * 1000) // 60 sec
                    .setFastestInterval(10000) // 10 sec
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApi,
                    mLocationRequest, mLocationListener);

            Log.d(TAG, "start foreground LocationUpdate");
        }
    }

    private void stopLocationUpdate() {
        if (mGoogleApi != null && mGoogleApi.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApi, mLocationListener);
        }
    }

    @Override
    protected long getIntervalTime() {
        return mServiceInterval;
    }

    @Override
    protected void execTask() {
        Log.d(TAG, "execTask");

        mActiveService = this;

        // Try to connect Google Api until it is succeded.  The main
        // UI (MapActivity) should resolve any issue like old version
        // and not installed.
        connectGoogleApi();

        // Start a location update.  The location permission should be
        // taken care of by the main UI (MapActivity).
        startLocationUpdate();

        // Schedule to run this again.
        makeNextPlan();
    }

    @Override
    protected void makeNextPlan() {
        this.scheduleNextTime();
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location == null) return;

            Log.d(TAG, "onLocationChanged");

            if (mLastKnownLocation == null) {
                mLastKnownLocation = location;
                return;
            }

//            saveLocation(location);

            // distance in meter
            float distance = location.distanceTo(mLastKnownLocation);

            // saveLocation location when
            // 1. in foreground, keep the past 10 locations, then if
            // the minimum move is more than 5 meters, save the
            // location.
            // 2. in background, when moved more than 10 meters

            if (mLocationMode == LocationMode.FOREGROUND) {
                queue.add(new PastLocation(distance, location));
                if (queue.size() == PAST_LOCATION_QUEUE_SIZE) {
                    Log.d(TAG, "foreground distance: " + queue.poll().distance);
                    PastLocation pl = queue.poll();
                    if (pl.distance > FOREGROUND_MIN_MOVEMENT) {
                        saveLocation(pl.location);
                    } else {
                        broadcastLocation(pl.location, mLastKnownAddress);
                    }
                    queue.clear();
                } else {
                    broadcastLocation(location, mLastKnownAddress);
                }
            } else if (mLocationMode == LocationMode.BACKGROUND) {
                Log.d(TAG, "background distance: " + distance);
                if (distance >= BACKGROUND_MIN_MOVEMENT) {
                    saveLocation(location);
                }
            }
        }
    };

    private int detectRangeChange(Address n, Address o) {
        if (n == null || o == null) {
            return 0;
        }
        if (n.getCountryName() != null && o.getCountryName() != null) {
            if (!n.getCountryName().equals(o.getCountryName())) {
                return 1;
            }
        }
        if (n.getAdminArea() != null && o.getAdminArea() != null) {
            if (!n.getAdminArea().equals(o.getAdminArea())) {
                return 2;
            }
        }
        if (n.getSubAdminArea() != null && o.getSubAdminArea() != null) {
            if (!n.getSubAdminArea().equals(o.getSubAdminArea())) {
                return 4;
            }
        }
        if (n.getLocality() != null && o.getLocality() != null) {
            if (!n.getLocality().equals(o.getLocality())) {
                return 8;
            }
        }
        if (n.getSubLocality() != null && o.getSubLocality() != null) {
            if (!n.getSubLocality().equals(o.getSubLocality())) {
                return 16;
            }
        }
        if (n.getThoroughfare() != null && o.getThoroughfare() != null) {
            if (!n.getThoroughfare().equals(o.getThoroughfare())) {
                return 32;
            }
        }
        if (n.getSubThoroughfare() != null && o.getSubThoroughfare() != null) {
            if (!n.getSubThoroughfare().equals(o.getSubThoroughfare())) {
                return 64;
            }
        }
        return 128;
    }

    private void saveLocation(final Location location) {
        location.setSpeed(Utils.getSpeed(location, mLastKnownLocation));

        Address address = Utils.getFromLocation(location);

        if (address == null) {
            Log.d(TAG, "address not available from Geocoder");
            return;
        }

        int rangeChange = detectRangeChange(address, mLastKnownAddress);

        if (rangeChange == 0) {
            Log.d(TAG, "range not available");
            return;
        }

        mLastKnownLocation = location;

        mLastKnownAddress = address;

        Log.d(TAG, "saveLocation");
        broadcastLocation(mLastKnownLocation, mLastKnownAddress);

        FB.saveLocation(mLastKnownLocation, mLastKnownAddress, new FB.SaveLocationListener() {
            @Override
            public void onSuccess(String key) {
                DBUtil.saveSentLocation(mLastKnownLocation, mLastKnownAddress, key);
            }

            @Override
            public void onFail(String err) {
                // save the database
                Log.d(TAG, "saveLocation: failed to save location");
                DBUtil.saveUnsentLocation(mLastKnownLocation, mLastKnownAddress);
            }
        });

        // locations not sent to the server
        List<LocationAddress> dbLocations = DBUtil.getUnsentLocations();
        if (dbLocations != null) {
            for (int i = 0; i < dbLocations.size() && i < 100; i++) {
                Log.d(TAG, "saveLocation: save location to database");
                final LocationAddress la = dbLocations.get(i);
                FB.saveLocation(Utils.getLocation(la), Utils.getAddress(la), false,
                        new FB.SaveLocationListener() {
                            @Override
                            public void onSuccess(String key) {
                                DBUtil.deleteLocation(la.getId());
                            }

                            @Override
                            public void onFail(String err) {
                            }
                        });
            }
        }
    }

    private void broadcastLocation(Location location, Address address) {
        // notify both location and address to the activity
        Intent intent = new Intent(MapActivity.USER_LOCATION_UPDATE);
        intent.putExtra(MapActivity.LOCATION_DATA, location);
        intent.putExtra(MapActivity.ADDRESS_DATA, address);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}

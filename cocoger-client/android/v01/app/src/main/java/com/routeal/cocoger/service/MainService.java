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

    private final static int PAST_LOCATION_QUEUE_SIZE = 5;

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
                    .setFastestInterval(3000) // 5 sec
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

    private float getSpeed(Location to, Location from) {
        float distance = to.distanceTo(from);
        float elapsed = Math.abs((float) ((to.getTime() - from.getTime()) / 1000.0));
        if (elapsed > 0) {
            float speed = distance / elapsed; // meter / seconds
            { // testing
                float speed2 = speed * 18 / 5;
                Log.d(TAG, "getSpeed: speed=" + speed2 + " (km/h)");
            }
            return speed;
        }
        return 0;
    }

    private float getSpeed2(Location to, Location from) {
        double distance = Utils.distanceTo(to, from);
        double elapsed = Math.abs((float) ((to.getTime() - from.getTime()) / 1000.0));
        if (elapsed > 0) {
            double speed = distance / elapsed; // meter / seconds
            { // testing
                double speed2 = speed * 18 / 5;
                Log.d(TAG, "getSpeed: speed=" + speed2 + " (km/h)");
            }
            return (float) speed;
        }
        return 0;
    }

    private float getSpeed3(Location to, Location from) {
        float speed = 0;
        if (to.hasSpeed()) {
            speed = to.getSpeed();
        } else {
            long elapsed = to.getElapsedRealtimeNanos() - from.getElapsedRealtimeNanos();
            elapsed *= 1e-9; // seconds

            float[] result = new float[3];
            Location.distanceBetween(to.getLatitude(), to.getLongitude(), from.getLatitude(), from.getLongitude(), result);
            float distance = result[0]; // meter
            speed = distance / elapsed;
            to.setSpeed(speed);
        }
        float speed2 = speed * 18 / 5;
        Log.d(TAG, "getSpeed: speed=" + speed2 + " (km/h)");
        return speed;
    }

    private void saveLocation(final Location location) {
        location.setSpeed(getSpeed3(location, mLastKnownLocation));

        mLastKnownLocation = location;

        Address address = Utils.getFromLocation(location);

        if (address == null) {
            Log.d(TAG, "address not available from Geocoder");
            return;
        }

        mLastKnownAddress = address;

        Log.d(TAG, "saveLocation");
        broadcastLocation(mLastKnownLocation, mLastKnownAddress);

        FB.saveLocation(mLastKnownLocation, mLastKnownAddress, new FB.CompleteListener() {
            @Override
            public void onSuccess() {
                DBUtil.saveSentLocation(mLastKnownLocation, mLastKnownAddress);
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
                        new FB.CompleteListener() {
                            @Override
                            public void onSuccess() {
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
        intent.putExtra(MapActivity.LOCATION_UPDATE, location);
        intent.putExtra(MapActivity.ADDRESS_UPDATE, address);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}

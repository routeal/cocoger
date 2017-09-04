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
import com.routeal.cocoger.ui.main.MapActivity;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;

/**
 * Created by hwatanabe on 4/11/17.
 */

public class MainService extends BasePeriodicService
        implements LocationListener {

    enum LocationMode {
        NONE,
        BACKGROUND,
        FOREGROUND
    }

    private static final String TAG = "MainService";

    private final static long BACKGROUND_INTERVAL = 20000;

    private final static long FOREGROUND_INTERVAL = 2000;

    private final static int PASTLOCATION_QUEUE_SIZE = 3;

    private final static float FOREGROUND_MIN_MOVEMENT = 2.0f;

    private final static float BACKGROUND_MIN_MOVEMENT = 10.0f;

    public final static String LOCATION_UPDATE = "location_update";

    public final static String ADDRESS_UPDATE = "address_update";

    public static MainService mActiveService;

    private static LocationRequest mLocationRequest;

    private static LocationMode mLocationMode = LocationMode.NONE;

    private static LocationMode mRequestedLocationMode = LocationMode.BACKGROUND;

    private static Location mLastKnownLocation;

    private static long mServiceInterval = BACKGROUND_INTERVAL;

    private GoogleApiClient mGoogleApiClient;

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
            new PriorityQueue<>(10, new LocationAscendingOrder());

    public static void setForegroundMode() {
        mRequestedLocationMode = LocationMode.FOREGROUND;
        mServiceInterval = FOREGROUND_INTERVAL;
        if (mActiveService != null) {
            mActiveService.execTask();
        }
    }

    public static void setBackgroundMode() {
        mRequestedLocationMode = LocationMode.BACKGROUND;
        mServiceInterval = BACKGROUND_INTERVAL;
        if (mActiveService != null) {
            mActiveService.execTask();
        }
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, MainService.class);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // set up FB monitoring
        FB.monitorAuthentication();
    }

    // connectGoogleApi in background, called in the background
    private void connectGoogleApi() {
        if (mGeocoder == null) {
            mGeocoder = new Geocoder(this, Locale.ENGLISH);
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mActiveService)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }

        if (!(mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting())) {
            Log.d(TAG, "connecting googleapiclient");
            mGoogleApiClient.connect();
        }
    }

    private void startLocationUpdate() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // do nothing
        } else {
            return;
        }

        if (mLastKnownLocation != null) {
            mLastKnownLocation =
                    LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if (mLocationMode == mRequestedLocationMode) return;

        Log.d(TAG, "startLocationUpdate");

        if (mRequestedLocationMode.equals(LocationMode.BACKGROUND)) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mLocationRequest = LocationRequest.create()
                    .setSmallestDisplacement(10) // when 10 meter moved
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
            mLocationMode = LocationMode.BACKGROUND;
            Log.d(TAG, "start background LocationUpdate");
        } else {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mLocationRequest = LocationRequest.create()
                    .setInterval(2000)
                    .setFastestInterval(1000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
            mLocationMode = LocationMode.FOREGROUND;
            Log.d(TAG, "start foreground LocationUpdate");
        }
    }

    @Override
    protected long getIntervalTime() {
        return mServiceInterval;
    }

    @Override
    protected void execTask() {
        mActiveService = this;

        // connect with google api, will try until success
        connectGoogleApi();

        // start to get a location update, if the location permission is not set, dose nothing
        startLocationUpdate();

        makeNextPlan();
    }

    @Override
    public void makeNextPlan() {
        this.scheduleNextTime();
    }

    public static void stop(Context context) {
        if (mActiveService != null) {
            mActiveService.stopResident(context);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) return;

        if (mLastKnownLocation != null) {
            // distance in meter
            float distance = location.distanceTo(mLastKnownLocation);

            // saveLocation location when
            // 1. in foreground, keep the past 10 locations, then if the minimum move is more
            // than 5 meters, save the location.
            // 2. in background, when moved more than 10 meters

            if (mLocationMode == LocationMode.FOREGROUND) {
                queue.add(new PastLocation(distance, location));
                if (queue.size() == PASTLOCATION_QUEUE_SIZE) {
                    Log.d(TAG, "foreground distance: " + queue.poll().distance);
                    if (queue.poll().distance > FOREGROUND_MIN_MOVEMENT) {
                        saveLocation(queue.poll().location);
                    }
                    queue.clear();
                }
            } else if (mLocationMode == LocationMode.BACKGROUND) {
                Log.d(TAG, "background distance: " + distance);
                if (distance >= BACKGROUND_MIN_MOVEMENT) {
                    saveLocation(location);
                }
            }
        } else {
            saveLocation(location);
        }
    }

    public static Location getLastLocation() {
        return mLastKnownLocation;
    }

    public static GoogleApiClient getGoogleApiClient() {
        if (mActiveService != null) {
            return mActiveService.mGoogleApiClient;
        }
        return null;
    }

    private void saveLocation(Location location) {
        mLastKnownLocation = location;

        Address address = null;
        try {
            List<Address> addresses = mGeocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
            // FIXME: first address always works best???
            address = addresses.get(0);
        } catch (Exception e) {
        }

        if (address == null) {
            Log.d(TAG, "No address for " + location.toString());
            return;
        }

        Log.d(TAG, "saveLocation");

        // notify both location address to the activity
        Intent intent = new Intent(MapActivity.LAST_LOCATION_UPDATE);
        intent.putExtra(LOCATION_UPDATE, location);
        intent.putExtra(ADDRESS_UPDATE, address);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        FB.saveLocation(location, address);
    }
}

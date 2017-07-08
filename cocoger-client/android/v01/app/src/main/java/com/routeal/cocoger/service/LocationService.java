package com.routeal.cocoger.service;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.provider.DB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;

/**
 * Created by hwatanabe on 4/11/17.
 */

public class LocationService extends BasePeriodicService
        implements LocationListener {

    public static final String LAST_LOCATION_UPDATE = "last_location_update";

    enum LocationMode {
        NONE,
        BACKGROUND,
        FOREGROUND
    }

    private static final String TAG = "LocationService";

    private final static String LocationPermission = "locationPermission";

    private final static long backgroundInterval = 20000;

    private final static long foregroundInterval = 2000;

    public static LocationService activeService;

    private static LocationRequest mLocationRequest;

    private static LocationMode mLocationMode = LocationMode.NONE;

    private static LocationMode mRequestedLocationMode = LocationMode.BACKGROUND;

    private static Location mLastKnownLocation;

    private static long interval = backgroundInterval;

    private GoogleApiClient mGoogleApiClient;

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

    private static PriorityQueue<PastLocation> queue = new PriorityQueue<>(10, new LocationAscendingOrder());

    public static void setForegroundMode() {
        mRequestedLocationMode = LocationMode.FOREGROUND;
        interval = foregroundInterval;
        if (activeService != null) {
            activeService.execTask();
        }
    }

    public static void setBackgroundMode() {
        mRequestedLocationMode = LocationMode.BACKGROUND;
        interval = backgroundInterval;
        if (activeService != null) {
            activeService.execTask();
        }
    }

    private Geocoder mGeocoder;

    // connect in background, called in the background
    private void connect() {
        if (mGeocoder == null) {
            mGeocoder = new Geocoder(this, Locale.ENGLISH);
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activeService)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }

        if (!mGoogleApiClient.isConnected()) {
            Log.d(TAG, "connecting googleapiclient");
            mGoogleApiClient.connect();
        }
    }

    private void startLocationUpdate() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return;
        }

        Log.d(TAG, "startLocationUpdate");

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // do nothing
        } else {
            return;
        }

        if (mLastKnownLocation != null) {
            mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if (mLocationMode == mRequestedLocationMode) return;

        if (mRequestedLocationMode.equals(LocationMode.BACKGROUND)) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mLocationRequest = LocationRequest.create()
                    .setSmallestDisplacement(10) // when 10 meter moved
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            mLocationMode = LocationMode.BACKGROUND;
            Log.d(TAG, "start background LocationUpdate");
        } else {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mLocationRequest = LocationRequest.create()
                    .setInterval(2000)
                    .setFastestInterval(1000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            mLocationMode = LocationMode.FOREGROUND;
            Log.d(TAG, "start foreground LocationUpdate");
        }
    }

    @Override
    protected long getIntervalTime() {
        return interval;
    }

    @Override
    protected void execTask() {
        activeService = this;

        //
        // background processing here
        //
        Log.d(TAG, "execTask");

        if (MainApplication.getBool(LocationPermission)) {
            // start to connect with google api client
            connect();
            // start to get a location update
            startLocationUpdate();
        }

        // upload the locations in the db
        uploadLocations();

        makeNextPlan();
    }

    @Override
    public void makeNextPlan() {
        this.scheduleNextTime();
    }

    public static void stopResidentIfActive(Context context) {
        if (activeService != null) {
            activeService.stopResident(context);
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
                if (queue.size() == 10) {
                    Log.d(TAG, "distance: " + queue.poll().distance);
                    if (queue.poll().distance > 5) {
                        saveLocation(queue.poll().location);
                    }
                    queue.clear();
                }
            } else if (mLocationMode == LocationMode.BACKGROUND) {
                Log.d(TAG, "distance: " + distance);
                if (distance >= 10.0) {
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

    private void saveLocation(Location location) {
        mLastKnownLocation = location;

        List<Address> addresses = null;
        try {
            addresses = mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {

        }

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            Log.d(TAG, "address: " + address.toString());
        }

        // notify the location to the activity
        Intent intent = new Intent(LAST_LOCATION_UPDATE);
        intent.putExtra("location", location);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        // save the location into the database
        ContentValues values = new ContentValues();
        values.put(DB.Locations.CREATED, System.currentTimeMillis());
        values.put(DB.Locations.LATITUDE, location.getLatitude());
        values.put(DB.Locations.LONGITUDE, location.getLongitude());
        ContentResolver contentResolver = this.getContentResolver();
        contentResolver.insert(DB.Locations.CONTENT_URI, values);
    }

    private void uploadLocations() {
        List<LocationAddress> locations = new ArrayList<LocationAddress>();

        Cursor cursor = null;
        try {
            Uri uri = DB.Locations.CONTENT_URI;
            ContentResolver contentResolver = this.getContentResolver();
            cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null) {
                int index;
                cursor.moveToFirst();
                do {
                    LocationAddress loc = new LocationAddress();
                    locations.add(loc);
                    index = cursor.getColumnIndex(DB.Locations.LATITUDE);
                    loc.setLatitude(cursor.getDouble(index));
                    index = cursor.getColumnIndex(DB.Locations.LONGITUDE);
                    loc.setLongitude(cursor.getDouble(index));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error to retrieve locations", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (locations.size() == 0) return;

    }

}

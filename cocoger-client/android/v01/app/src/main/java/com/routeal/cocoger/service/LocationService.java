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

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.net.RestClient;
import com.routeal.cocoger.provider.DBUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hwatanabe on 4/11/17.
 */

public class LocationService extends BasePeriodicService
        implements LocationListener {

    enum LocationMode {
        NONE,
        BACKGROUND,
        FOREGROUND
    }

    private static final String TAG = "LocationService";

    public static final String LAST_LOCATION_UPDATE = "last_location_update";

    private final static long BACKGROUND_INTERVAL = 20000;

    private final static long FOREGROUND_INTERVAL = 2000;

    private final static int MAX_LOCATION_UPDATE_NUMBER = 20;

    private final static int PASTLOCATION_QUEUE_SIZE = 3;

    private final static float FOREGROUND_MIN_MOVEMENT = 2.0f;

    private final static float BACKGROUND_MIN_MOVEMENT = 10.0f;

    public static LocationService mActiveService;

    private static LocationRequest mLocationRequest;

    private static LocationMode mLocationMode = LocationMode.NONE;

    private static LocationMode mRequestedLocationMode = LocationMode.BACKGROUND;

    private static Location mLastKnownLocation;

    private static long mServiceInterval = BACKGROUND_INTERVAL;

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

    private Geocoder mGeocoder;

    // connect in background, called in the background
    private void connect() {
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

        if (!mGoogleApiClient.isConnected()) {
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

        if (MainApplication.isLocationPermitted()) {
            Log.d(TAG, "Permission granted already");

            // start to connect with google api client
            connect();

            // start to get a location update
            startLocationUpdate();

            // upload locations if any in the database
            uploadLocations();
        }

        makeNextPlan();
    }

    @Override
    public void makeNextPlan() {
        this.scheduleNextTime();
    }

    public static void stopResidentIfActive(Context context) {
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

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

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
        Intent intent = new Intent(LAST_LOCATION_UPDATE);
        intent.putExtra("location", location);
        intent.putExtra("address", address);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) return;

        String uid = firebaseUser.getUid();

        LocationAddress detail = new LocationAddress();

        detail.setTimestamp(location.getTime());
        detail.setLatitude(location.getLatitude());
        detail.setLongitude(location.getLongitude());
        detail.setAltitude(location.getAltitude());
        detail.setSpeed(location.getSpeed());
        detail.setPostalCode(address.getPostalCode());
        detail.setCountryName(address.getCountryName());
        detail.setAdminArea(address.getAdminArea());
        detail.setSubAdminArea(address.getSubAdminArea());
        detail.setlocality(address.getLocality());
        detail.setSubLocality(address.getSubLocality());
        detail.setThoroughfare(address.getThoroughfare());
        detail.setSubThoroughfare(address.getSubThoroughfare());

        // top level database reference
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

        // location key
        String locId = dbRef.child("locations").push().getKey();

        GeoHash geoHash = new GeoHash(new GeoLocation(location.getLatitude(), location.getLongitude()));
        Map<String, Object> updates = new HashMap<>();
        // location detail
        updates.put("locations/" + locId, detail);
        // geo location
        updates.put("geo_locations/" + locId + "/g", geoHash.getGeoHashString());
        // geo location
        updates.put("geo_locations/" + locId + "/l", Arrays.asList(location.getLatitude(), location.getLongitude()));
        // user locations
        updates.put("users/" + uid + "/locations/" + locId, detail.getTimestamp());

        dbRef.updateChildren(updates);

        // save both location and address into the database
        //DBUtil.saveLocation(location, address);
    }

    private void uploadLocations() {
        final List<LocationAddress> locations = DBUtil.getLocations(MAX_LOCATION_UPDATE_NUMBER);

        if (locations.isEmpty()) return;

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

/*
        Call<Void> upload = RestClient.service().setLocations(RestClient.token(), locations);

        upload.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // on success, remove the locations in the database
                DBUtil.deleteLocations(locations);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
*/
    }

}

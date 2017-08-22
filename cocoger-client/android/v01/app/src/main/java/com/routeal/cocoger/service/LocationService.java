package com.routeal.cocoger.service;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.Device;
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.util.Utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;

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

    public LocationService() {
        super();

        // setting up a listener when the user is authenticated
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "FB authenticated");
                    userAuthenticated(user);
                } else {
                    Log.d(TAG, "FB invalidated");
                    userInvalidated();
                }
            }
        });
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
            connectGoogleApi();

            // start to get a location update
            startLocationUpdate();
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

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fUser == null) return;

        String uid = fUser.getUid();

        LocationAddress loc = new LocationAddress();

        loc.setTimestamp(location.getTime());
        loc.setLatitude(location.getLatitude());
        loc.setLongitude(location.getLongitude());
        loc.setAltitude(location.getAltitude());
        loc.setSpeed(location.getSpeed());
        loc.setPostalCode(address.getPostalCode());
        loc.setCountryName(address.getCountryName());
        loc.setAdminArea(address.getAdminArea());
        loc.setSubAdminArea(address.getSubAdminArea());
        loc.setlocality(address.getLocality());
        loc.setSubLocality(address.getSubLocality());
        loc.setThoroughfare(address.getThoroughfare());
        loc.setSubThoroughfare(address.getSubThoroughfare());

        // top level database reference
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        // location key
        String locId = db.child("locations").push().getKey();

        GeoHash geoHash = new GeoHash(new GeoLocation(location.getLatitude(), location.getLongitude()));
        Map<String, Object> updates = new HashMap<>();
        // location detail
        updates.put("locations/" + locId, loc);
        // geo location
        updates.put("geo_locations/" + locId + "/g", geoHash.getGeoHashString());
        // geo location
        updates.put("geo_locations/" + locId + "/l", Arrays.asList(location.getLatitude(), location.getLongitude()));
        // user locations
        updates.put("users/" + uid + "/locations/" + locId, loc.getTimestamp());

        db.updateChildren(updates);
    }

    // called when the user is authenticated
    private void userAuthenticated(FirebaseUser fbUser) {
        String uid = fbUser.getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");

        // called whenever the user database is updated
        userRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "User database changed");

                User newUser = dataSnapshot.getValue(User.class);

                User currentUser = MainApplication.getUser();

                if (newUser != null) {
                    if (currentUser != null) {
                        updateUser(newUser, currentUser);
                    } else {
                        initUser(dataSnapshot.getKey(), newUser);
                    }
                } else {
                    if (MainApplication.getLoginEmail() == null) {
                        // no user in the database yet, but the user
                        // object is set to MainApplication
                        createUser(currentUser);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void createUser(User user) {
        Log.d(TAG, "createUser:" + user.toString());

        // first (must be first), save the email address to the local preference
        MainApplication.setLoginEmail(user.getEmail());

        // Send email verification to the signup email address
        sendEmailVerification();

        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = fbUser.getUid();

        FirebaseDatabase db = FirebaseDatabase.getInstance();

        // save the device info
        Device device = Utils.getDevice();
        device.setUid(uid);
        DatabaseReference devRef = db.getReference().child("devices");
        String key = devRef.push().getKey();
        devRef.child(key).setValue(device);

        // save the user info to the remote database
        DatabaseReference userRef = db.getReference().child("users");
        userRef.child(uid).setValue(user);

        // add the device key to the user info
        Map<String, String> devices = new HashMap<>();
        devices.put(key, device.getDeviceId());
        userRef.child(uid).child("devices").setValue(devices);
    }

    private void initUser(String uid, User user) {
        Log.d(TAG, "initUser: uid=" + uid);

        // save the user in the memory
        MainApplication.setUser(user);

        // save the email address to the local preference
        MainApplication.setLoginEmail(user.getEmail());

        // the current device
        Device currentDevice = Utils.getDevice();

        String devKey = null;

        // get the device key to match the device id in the user database
        Map<String, String> devList = user.getDevices();
        if (devList != null && !devList.isEmpty()) {
            for (Map.Entry<String, String> entry : devList.entrySet()) {
                // the values is a device id
                if (entry.getValue().equals(currentDevice.getDeviceId())) {
                    devKey = entry.getKey();
                }
            }
        }

        DatabaseReference devRef = FirebaseDatabase.getInstance().getReference().child("devices");

        if (devKey != null) {
            // update the timestamp of the device
            devRef.child(devKey).child("timestamp").setValue(currentDevice.getTimestamp());
        } else {
            // set the uid to the device before save
            currentDevice.setUid(uid);

            // add it as a new device
            String newKey = devRef.push().getKey();
            devRef.child(newKey).setValue(currentDevice);

            // also add it to the user database under 'devices'
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
            userRef.child(uid).child("devices").child(newKey).setValue(currentDevice.getDeviceId());
        }
    }

    private void updateUser(User newUser, User oldUser) {
        Log.d(TAG, "updateUser");

/*
        if (newUser.getPicture() != oldUser.getPicture()) {
            Log.d(TAG, "updateUser=picture");

            FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
            String uid = fbUser.getUid();

            FirebaseDatabase db = FirebaseDatabase.getInstance();

            DatabaseReference userRef = db.getReference().child("users");
            userRef.child(uid).setValue(newUser);
        }
*/
    }

    private void sendEmailVerification() {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        fbUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "sendEmailVerification");
                }
            }
        });
    }

    private void userInvalidated() {
        MainApplication.setUser(null);
    }
}

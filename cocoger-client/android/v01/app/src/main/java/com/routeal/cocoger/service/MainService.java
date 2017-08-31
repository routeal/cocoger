package com.routeal.cocoger.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
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
import com.routeal.cocoger.model.Friend;
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.model.User;
import com.routeal.cocoger.ui.main.PanelMapActivity;
import com.routeal.cocoger.util.CircleTransform;
import com.routeal.cocoger.util.LocationRange;
import com.routeal.cocoger.util.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

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

    public static final String LAST_LOCATION_UPDATE = "last_location_update";

    public static final String USER_AVAILABLE = "user_available";

    public static final String ACTION_FRIEND_REQUEST_ACCEPTED = "FRIEND_REQUEST_ACCEPTED";

    public static final String ACTION_FRIEND_REQUEST_DECLINED = "FRIEND_REQUEST_DECLINED";

    public static final String ACTION_RANGE_REQUEST_ACCEPTED = "RANGE_REQUEST_ACCEPTED";

    public static final String ACTION_RANGE_REQUEST_DECLINED = "RANGE_REQUEST_DECLINED";

    private final static long BACKGROUND_INTERVAL = 20000;

    private final static long FOREGROUND_INTERVAL = 2000;

    private final static int PASTLOCATION_QUEUE_SIZE = 3;

    private final static float FOREGROUND_MIN_MOVEMENT = 2.0f;

    private final static float BACKGROUND_MIN_MOVEMENT = 10.0f;

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

    public static boolean instantiated = false;

    // FIXME: MainService gets called twice for some reason.
    // Needs fix.  instantiated is just a work around.
    public MainService() {
        super();

        if (instantiated) return;

        instantiated = true;

        // setting up a listener when the user is authenticated
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "Firebase authenticated");
                    userAuthenticated(user);
                } else {
                    Log.d(TAG, "Firebase invalidated");
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
            // Log.d(TAG, "Permission granted already");

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

                if (newUser == null) {
                    if (currentUser == null) {
                        signupUser(dataSnapshot.getKey());
                    }
                } else {
                    if (currentUser != null) {
                        updateUser(newUser, currentUser);
                    } else {
                        loginUser(dataSnapshot.getKey(), newUser);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loginUser(String uid, User user) {
        Log.d(TAG, "loginUser:" + user.toString());

        // save the user in the memory
        MainApplication.setUser(user);

        if (MainApplication.getContext() != null) {
            Intent intent = new Intent(USER_AVAILABLE);
            LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(intent);
        }

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

    private void signupUser(String uid) {
        Log.d(TAG, "signupUser: uid=" + uid);

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fUser == null) {
            Log.e(TAG, "no current user from FirebaseUser in signupUser");
            return;
        }

        User user = new User();
        user.setDisplayName(fUser.getDisplayName());
        user.setEmail(fUser.getEmail());

        // save the user in the memory
        MainApplication.setUser(user);

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

        // Send an email verification
        sendEmailVerification();
    }

    private void updateUser(User newUser, User oldUser) {
        Log.d(TAG, "updateUser");

        Map<String, Long> invitees = newUser.getInvitees();
        if (invitees != null && !invitees.isEmpty()) {
            for (Map.Entry<String, Long> entry : invitees.entrySet()) {
                if (entry.getValue() > 0) {
                    if (oldUser.getInvitees() == null ||
                            oldUser.getInvitees().get(entry.getKey()) == null) {
                        Log.d(TAG, "received new friends request");
                        //oldUser.setInvitees(invitees);
                        sendInviteNotification(entry.getKey());
                    } else {
                        Log.d(TAG, "received old friends request");
                    }
                }
            }
        }

        Map<String, Friend> newFriends = newUser.getFriends();
        Map<String, Friend> oldFriends = oldUser.getFriends();

        // added new friends
        if (newFriends.size() > oldFriends.size()) {
        }
        // deleted friends
        else if (newFriends.size() < oldFriends.size()) {
        }
        // range request
        else {
            for (Map.Entry<String, Friend> entry : newFriends.entrySet()) {
                String friendUid = entry.getKey();
                Friend newFriend = entry.getValue();
                Friend oldFriend = oldFriends.get(friendUid);
                int requestedRange = newFriend.getRangeRequest().getRange();
                int currentRange = newFriend.getRange();
                // new range request found
                if (newFriend.getRangeRequest() != null && oldFriend.getRangeRequest() == null) {
                    // send the notification

                    // notification id
                    int nid = new Random().nextInt();

                    Context context = MainApplication.getContext();

                    // accept starts the main activity with the friend view
                    Intent acceptIntent = new Intent(context, PanelMapActivity.class);
                    acceptIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    acceptIntent.setAction(ACTION_RANGE_REQUEST_ACCEPTED);
                    acceptIntent.putExtra("range_request", friendUid);
                    acceptIntent.putExtra("notification_id", nid);
                    PendingIntent pendingAcceptIntent = PendingIntent.getActivity(context, 1, acceptIntent, 0);
                    NotificationCompat.Action acceptAction = new NotificationCompat.Action.Builder(
                            R.drawable.ic_contacts_black_18dp,
                            "Accept", pendingAcceptIntent).build();

                    Intent declineIntent = new Intent(context, OnBootReceiver.class);
                    declineIntent.setAction(ACTION_RANGE_REQUEST_DECLINED);
                    declineIntent.putExtra("range_request", friendUid);
                    declineIntent.putExtra("notification_id", nid);
                    PendingIntent pendingDeclineIntent = PendingIntent.getBroadcast(context, 1, declineIntent, 0);
                    NotificationCompat.Action declineAction = new NotificationCompat.Action.Builder(
                            R.drawable.ic_contacts_black_18dp,
                            "Decline", pendingDeclineIntent).build();

                    String to = LocationRange.toString(requestedRange);
                    String from = LocationRange.toString(currentRange);
                    String content = "You received a range request to " + to + " from " + from;

                    final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_person_pin_circle_white_48dp)
                            .setContentTitle(newFriend.getDisplayName())
                            .setContentText(content)
                            .setAutoCancel(true)
                            .addAction(acceptAction)
                            .addAction(declineAction);

                    // seems not working, use notificationmanager's cancel method
                    Notification notification = mBuilder.build();
                    notification.flags |= Notification.FLAG_AUTO_CANCEL;

                    Picasso.with(MainApplication.getContext()).load(newFriend.getPicture()).transform(new CircleTransform()).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            mBuilder.setLargeIcon(bitmap);
                        }
                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                        }
                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });

                    NotificationManager mNotificationManager =
                            (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

                    mNotificationManager.notify(nid, notification);
                }
                // range request is neither approved or declined
                else if (newFriend.getRangeRequest() != null && oldFriend.getRangeRequest() != null) {
                }
            }
        }

        MainApplication.setUser(newUser);
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

    private void sendInviteNotification(final String invite) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");

        userRef.child(invite).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // notification id
                int nid = new Random().nextInt();

                User inviteUser = dataSnapshot.getValue(User.class);

                Context context = MainApplication.getContext();

                // accept starts the main activity with the friend view
                Intent acceptIntent = new Intent(context, PanelMapActivity.class);
                acceptIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                acceptIntent.setAction(ACTION_FRIEND_REQUEST_ACCEPTED);
                acceptIntent.putExtra("friend_invite", invite);
                acceptIntent.putExtra("notification_id", nid);
                PendingIntent pendingAcceptIntent = PendingIntent.getActivity(context, 1, acceptIntent, 0);
                NotificationCompat.Action acceptAction = new NotificationCompat.Action.Builder(
                        R.drawable.ic_contacts_black_18dp,
                        "Accept", pendingAcceptIntent).build();

                Intent declineIntent = new Intent(context, OnBootReceiver.class);
                declineIntent.setAction(ACTION_FRIEND_REQUEST_DECLINED);
                declineIntent.putExtra("friend_invite", invite);
                declineIntent.putExtra("notification_id", nid);
                PendingIntent pendingDeclineIntent = PendingIntent.getBroadcast(context, 1, declineIntent, 0);
                NotificationCompat.Action declineAction = new NotificationCompat.Action.Builder(
                        R.drawable.ic_contacts_black_18dp,
                        "Decline", pendingDeclineIntent).build();

                String content = "You received a friend request from " + inviteUser.getDisplayName() + ".";

                final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_person_pin_circle_white_48dp)
                        .setContentTitle(inviteUser.getDisplayName())
                        .setContentText(content)
                        .setAutoCancel(true)
                        .addAction(acceptAction)
                        .addAction(declineAction);

                // seems not working, use notificationmanager's cancel method
                Notification notification = mBuilder.build();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;

                Picasso.with(MainApplication.getContext()).load(inviteUser.getPicture()).transform(new CircleTransform()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        mBuilder.setLargeIcon(bitmap);
                    }
                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                    }
                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });

                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

                mNotificationManager.notify(nid, notification);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}

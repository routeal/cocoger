package com.routeal.cocoger.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnSuccessListener;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.provider.DBUtil;
import com.routeal.cocoger.ui.main.PanelMapActivity;
import com.routeal.cocoger.util.Utils;

import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by hwatanabe on 9/30/17.
 */

public class LocationUpdate {

    public final static int DEFAULT_FOREGROUND_LOCATION_UPDATE_INTERVAL = 15 * 60 * 1000; // 15 mins
    public final static int DEFAULT_LOCATION_UPDATE_INTERVAL = 60 * 1000; // 1 min
    public final static String FOREGROUND_LOCATION_UPDATE_INTERVAL = "foreground_location_update_interval";
    public final static String LOCATION_UPDATE_INTERVAL = "location_update_interval";

    final static String ACTION_START_FROM_NOTIFICATION = "notification";
    final static String NOTIFICATION_CHANNEL_ID = "location_update_notification";
    final static int NOTIFICATION_ID = (int) System.currentTimeMillis();

    private final static String TAG = "tako";
    private final static long LOCATION_UPDATE_FASTEST_INTERVAL = 10000; // 10 seconds
    private final static float FOREGROUND_MIN_MOVEMENT = 40.0f;
    private final static float BACKGROUND_MIN_MOVEMENT = 100.0f;
    private final static int PAST_LOCATION_QUEUE_MAX = 100;
    private final static int MAX_FOREGROUND_LOCATION_UPDATE_INTERVAL = 15 * 60 * 1000; // 15 mins

    private static LocationUpdate thisInstance = new LocationUpdate();
    private static PriorityQueue<PastLocation> mLocationQueue =
            new PriorityQueue<>(PAST_LOCATION_QUEUE_MAX, new LocationAscendingOrder());

    private GoogleApiClient mGoogleApi;
    private Geocoder mGeocoder;
    private NotificationManager mNotificationManager;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Handler mServiceHandler;
    private Location mLocation;
    private Address mAddress;
    private ServiceMode mIsServiceForeground = ServiceMode.NONE;
    private int mForegroundServiceLocationUpdateInterval = -1;
    private int mServiceLocationUpdateInterval = -1;
    private int mCurrentLocationUpdateInterval = -1;
    private long mLocationUpdateIntervalResetTime = 0;

    private LocationUpdate() {
        //FB.monitorAuthentication();
    }

    static LocationUpdate getInstance() {
        /*
        if (thisInstance == null) {
            thisInstance = new LocationUpdate();
        }
        */
        return thisInstance;
    }

    void exec(Context context) {
        Log.d(TAG, "exec");
        connectGoogleApi(context);

        startLocationUpdate(context);
    }

    private void connectGoogleApi(Context context) {
        if (mGeocoder == null) {
            mGeocoder = new Geocoder(context, Locale.getDefault());
        }

        if (mGoogleApi == null) {
            mGoogleApi = new GoogleApiClient.Builder(context)
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

    private void startLocationUpdate(final Context context) {
        if (mGoogleApi == null || !mGoogleApi.isConnected()) {
            Log.d(TAG, "google api not connected");
            return;
        }

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "startLocationUpdate: permission not granted");
            return;
        }

        Log.d(TAG, "startLocationUpdate");

        if (mFusedLocationClient == null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        }

        if (mLocationCallback == null) {
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    onNewLocation(context, locationResult.getLastLocation());
                    if (mLocationUpdateIntervalResetTime < System.currentTimeMillis()) {
                        MainApplication.putInt(LocationUpdate.FOREGROUND_LOCATION_UPDATE_INTERVAL, DEFAULT_FOREGROUND_LOCATION_UPDATE_INTERVAL);
                        mLocationUpdateIntervalResetTime = 0;
                    }
                }
            };
        }

        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        }

        if (mServiceHandler == null) {
            HandlerThread handlerThread = new HandlerThread(TAG);
            handlerThread.start();
            mServiceHandler = new Handler(handlerThread.getLooper());
        }

        if (mLocation == null) {
            getLastLocation(context);
        }

        if (!FB.isAuthenticated()) return;

        if (FB.getUser() != null && FB.getUser().getTest()) {
            Log.d(TAG, "Test mode: no background location update");
            return;
        }

        ServiceMode isForeground = isServiceRunningInForeground(context);

        if (mIsServiceForeground == isForeground) {
            if (isForeground == ServiceMode.FOREGROUND) {
                int interval = MainApplication.getInt(FOREGROUND_LOCATION_UPDATE_INTERVAL, DEFAULT_FOREGROUND_LOCATION_UPDATE_INTERVAL);
                if (interval == mForegroundServiceLocationUpdateInterval) {
                    Log.d(TAG, "FG Service Interval Not Changed");
                    return;
                } else {
                    Log.d(TAG, "FG Service Interval Changed=" + mForegroundServiceLocationUpdateInterval);
                }
            } else {
                int interval = MainApplication.getInt(LOCATION_UPDATE_INTERVAL, DEFAULT_LOCATION_UPDATE_INTERVAL);
                if (interval == mServiceLocationUpdateInterval) {
                    Log.d(TAG, "Service Interval Not Changed");
                    return;
                } else {
                    Log.d(TAG, "Service Interval Changed=" + mServiceLocationUpdateInterval);
                }
            }
        } else {
            mIsServiceForeground = isForeground;
        }

        mFusedLocationClient.removeLocationUpdates(mLocationCallback);

        LocationRequest locationRequest = null;

        if (isForeground == ServiceMode.FOREGROUND) { // service foreground is the background process
            mForegroundServiceLocationUpdateInterval = MainApplication.getInt(FOREGROUND_LOCATION_UPDATE_INTERVAL, DEFAULT_FOREGROUND_LOCATION_UPDATE_INTERVAL);
            if (mForegroundServiceLocationUpdateInterval > 0) {
                locationRequest = LocationRequest.create()
                        .setInterval(mForegroundServiceLocationUpdateInterval)
                        .setFastestInterval(mForegroundServiceLocationUpdateInterval)
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            } else {
                locationRequest = null;
            }
            mCurrentLocationUpdateInterval = mForegroundServiceLocationUpdateInterval;
            Log.d(TAG, "start service foreground LocationUpdate:" + mCurrentLocationUpdateInterval / 1000 / 60);
        } else {
            mServiceLocationUpdateInterval = MainApplication.getInt(LOCATION_UPDATE_INTERVAL, DEFAULT_LOCATION_UPDATE_INTERVAL);
            if (mServiceLocationUpdateInterval > 0) {
                locationRequest = LocationRequest.create()
                        .setInterval(mServiceLocationUpdateInterval)
                        .setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL) // 5 sec
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            } else {
                locationRequest = null;
            }
            mCurrentLocationUpdateInterval = mServiceLocationUpdateInterval;
            Log.d(TAG, "start service LocationUpdate:" + mCurrentLocationUpdateInterval / 1000 / 60);
        }

        Log.d(TAG, "Cancel periodic update");
        LocationUpdateReceiver.cancelUpdate(context, (AlarmManager) context.getSystemService(ALARM_SERVICE));

        if (mCurrentLocationUpdateInterval < MAX_FOREGROUND_LOCATION_UPDATE_INTERVAL) {
            mLocationUpdateIntervalResetTime = System.currentTimeMillis() + MAX_FOREGROUND_LOCATION_UPDATE_INTERVAL;
        } else {
            mLocationUpdateIntervalResetTime = 0;
        }

        if (locationRequest != null) {
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
        } else {
            Log.d(TAG, "LocationUpdate has been canceled.");
        }
    }

    private void onNewLocation(Context context, Location location) {
        Log.i(TAG, "New location: " + location);

        if (mLocation == null) {
            mLocation = location;
            return;
        }

        float distance = location.distanceTo(mLocation);

        if (mIsServiceForeground == ServiceMode.FOREGROUND) {
            if (distance >= BACKGROUND_MIN_MOVEMENT) {
                saveLocation(context, location);
            } else {
                Log.d(TAG, "NOT ENOUGH MOVE TO SAVE FOR BACKGROUND MOVE");
            }
            mNotificationManager.notify(NOTIFICATION_ID, getNotification(context));
        } else {
            mLocationQueue.add(new PastLocation(distance, location));
            int interval = (int) (location.getTime() - mLocation.getTime());
            PastLocation pl = mLocationQueue.poll();
            Log.d(TAG, "Interval=" + interval + " Current Interval=" + mCurrentLocationUpdateInterval);
            if (interval > mCurrentLocationUpdateInterval) {
                if (mLocationQueue.size() > PAST_LOCATION_QUEUE_MAX ||
                        mLocation.distanceTo(pl.location) > FOREGROUND_MIN_MOVEMENT) {
                    saveLocation(context, pl.location);
                    mLocationQueue.clear();
                }
            }
            broadcastLocation(context, location, mAddress);
        }
    }

    private void getLastLocation(Context context) {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            mLocation = location;
                            mAddress = Utils.getFromLocation(location);
                        }
                    });

        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private ServiceMode isServiceRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (LocationUpdateService.class.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return ServiceMode.FOREGROUND;
                }
            }
        }
        return ServiceMode.NOT_FOREGROUND;
    }

    Notification getNotification(Context context) {
        Intent intent = new Intent(context, PanelMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        PendingIntent activityPendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        String text = context.getString(R.string.last_location_update, formatter.format(new Date()));

        return new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentIntent(activityPendingIntent)
                .setContentTitle(text)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setSmallIcon(R.drawable.ic_person_pin_circle_white_48dp)
                .setTicker(text)
                .setWhen(System.currentTimeMillis()).build();
    }

    void destroy() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        if (mServiceHandler != null) {
            mServiceHandler.removeCallbacksAndMessages(null);
            mServiceHandler = null;
        }
        mForegroundServiceLocationUpdateInterval = -1;
        mServiceLocationUpdateInterval = -1;
        mCurrentLocationUpdateInterval = -1;
        mLocationUpdateIntervalResetTime = 0;
        mIsServiceForeground = ServiceMode.NONE;
        mLocationQueue.clear();
    }

    private void saveLocation(Context context, final Location location) {
        Address address = Utils.getFromLocation(location);
        if (address == null) {
            Log.d(TAG, "address not available from Geocoder");
            return;
        }
        Log.d(TAG, "saveLocation");

        location.setSpeed(Utils.getSpeed(location, mLocation));

        mLocation = location;
        mAddress = address;

        FB.saveLocation(mLocation, mAddress, new FB.SaveLocationListener() {
            @Override
            public void onSuccess(String key) {
                DBUtil.saveSentLocation(mLocation, mAddress, key);
            }

            @Override
            public void onFail(String err) {
                // save the database
                Log.d(TAG, "saveLocation: failed to save location");
                DBUtil.saveUnsentLocation(mLocation, mAddress);
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

    private void broadcastLocation(Context context, Location location, Address address) {
        Intent intent = new Intent(FB.USER_LOCATION_UPDATE);
        intent.putExtra(FB.LOCATION, location);
        intent.putExtra(FB.ADDRESS, address);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private enum ServiceMode {
        NONE,
        FOREGROUND,
        NOT_FOREGROUND
    }

    private static class LocationAscendingOrder implements Comparator<PastLocation> {
        @Override
        public int compare(PastLocation o1, PastLocation o2) {
            return (int) (o1.distance - o2.distance);
        }
    }

    private class PastLocation {
        float distance;
        Location location;

        PastLocation(float distance, Location location) {
            this.distance = distance;
            this.location = location;
        }
    }
}

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
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.model.LocationAddress;
import com.routeal.cocoger.provider.DBUtil;
import com.routeal.cocoger.ui.main.MapActivity;
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

    public final static int DEFAULT_FOREGROUND_LOCATION_UPDATE_INTERVAL = 15 * 60 * 1000;
    public final static int DEFAULT_LOCATION_UPDATE_INTERVAL = 1 * 60 * 1000;
    public final static String FOREGROUND_LOCATION_UPDATE_INTERVAL = "foreground_location_update_interval";
    public final static String LOCATION_UPDATE_INTERVAL = "location_update_interval";
    public final static String ACTION_START_FROM_NOTIFICATION = "notification";
    public final static String SERVICE_STARTED_FROM_BOOT = "boot";
    static final int NOTIFICATION_ID = (int) System.currentTimeMillis();
    static final String SERVICE_EXTRA_STARTED = "service_extra_started";
    private final static long LOCATION_UPDATE_FASTEST_INTERVAL = 5000; // 5 seconds
    private final static float FOREGROUND_MIN_MOVEMENT = 40.0f;
    private final static float BACKGROUND_MIN_MOVEMENT = 100.0f;
    private final static String TAG = "tako";
    private final static int PAST_LOCATION_QUEUE_MAX = 100;
    private static LocationUpdate thisInstance = new LocationUpdate();
    private static PriorityQueue<PastLocation> mLocationQueue =
            new PriorityQueue<>(PAST_LOCATION_QUEUE_MAX, new LocationAscendingOrder());
    private GoogleApiClient mGoogleApi;
    private Geocoder mGeocoder;
    private NotificationManager mNotificationManager;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Handler mServiceHandler;
    private Location mLocation;
    private Address mAddress;
    private ServiceMode mIsServiceForeground = ServiceMode.NONE;
    private int mForegroundServiceLocationUpdateInterval = 0;
    private int mServiceLocationUpdateInterval = 0;
    private int mCurrentLocationUpdateInterval = 0;

    private LocationUpdate() {
        FB.monitorAuthentication();
    }

    static LocationUpdate getInstance() {
        return thisInstance;
    }

    private static String getLocationTitle(Context context) {
        return context.getString(R.string.last_location_update,
                DateFormat.getDateTimeInstance().format(new Date()));
    }

    void exec(Context context) {
        //Log.d(TAG, "exec");
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
            getLastLocation();
        }

        if (!FB.isAuthenticated()) return;

        ServiceMode isForeground = isServiceRunningInForeground(context);

        if (mIsServiceForeground == isForeground) {
            if (isForeground == ServiceMode.FOREGROUND) {
                int interval = MainApplication.getInt(FOREGROUND_LOCATION_UPDATE_INTERVAL, DEFAULT_FOREGROUND_LOCATION_UPDATE_INTERVAL);
                if (interval == mForegroundServiceLocationUpdateInterval) {
                    Log.d(TAG, "FG Service Interval Not Changed");
                    return;
                } else {
                    mForegroundServiceLocationUpdateInterval = interval;
                    Log.d(TAG, "FG Service Interval Changed=" + mForegroundServiceLocationUpdateInterval);
                }
            } else {
                int interval = MainApplication.getInt(LOCATION_UPDATE_INTERVAL, DEFAULT_LOCATION_UPDATE_INTERVAL);
                if (interval == mServiceLocationUpdateInterval) {
                    Log.d(TAG, "Service Interval Not Changed");
                    return;
                } else {
                    mServiceLocationUpdateInterval = interval;
                    Log.d(TAG, "Service Interval Changed=" + mServiceLocationUpdateInterval);
                }
            }
        } else {
            mIsServiceForeground = isForeground;
        }

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApi, mLocationCallback);

        if (isForeground == ServiceMode.FOREGROUND) {
            mForegroundServiceLocationUpdateInterval = MainApplication.getInt(FOREGROUND_LOCATION_UPDATE_INTERVAL, DEFAULT_FOREGROUND_LOCATION_UPDATE_INTERVAL);
            mLocationRequest = LocationRequest.create()
                    .setInterval(mForegroundServiceLocationUpdateInterval)
                    .setFastestInterval(mForegroundServiceLocationUpdateInterval)
                    //.setSmallestDisplacement(BACKGROUND_MIN_MOVEMENT)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mCurrentLocationUpdateInterval = mForegroundServiceLocationUpdateInterval;
            Log.d(TAG, "start service foreground LocationUpdate");
        } else {
            mServiceLocationUpdateInterval = MainApplication.getInt(LOCATION_UPDATE_INTERVAL, DEFAULT_LOCATION_UPDATE_INTERVAL);
            mLocationRequest = LocationRequest.create()
                    .setInterval(mServiceLocationUpdateInterval)
                    .setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL) // 5 sec
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mCurrentLocationUpdateInterval = mServiceLocationUpdateInterval;
            Log.d(TAG, "start service LocationUpdate");
        }

        Log.d(TAG, "Changed periodic update:" + mCurrentLocationUpdateInterval/1000);
        LocationUpdateReceiver.setUpdateInterval(mCurrentLocationUpdateInterval/1000);
        LocationUpdateReceiver.scheduleUpdate(context, (AlarmManager) context.getSystemService(ALARM_SERVICE));

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApi,
                mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private void onNewLocation(Context context, Location location) {
        //Log.i(TAG, "New location: " + location);

        if (mLocation == null) {
            mLocation = location;
            return;
        }

        float distance = location.distanceTo(mLocation);

        if (mIsServiceForeground == ServiceMode.FOREGROUND) {
            if (distance >= BACKGROUND_MIN_MOVEMENT) {
                saveLocation(context, location);
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

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
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
        /*  Not supported
        Intent intent = new Intent(context, LocationUpdateService.class);
        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.setAction(ACTION_START_FROM_NOTIFICATION);
        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        */

        Intent intent = new Intent(context, PanelMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        CharSequence text = getLocationTitle(context);

        return new NotificationCompat.Builder(context)
                /* Not supported
                .addAction(android.R.drawable.ic_menu_view, "launch app",
                        activityPendingIntent)
                .addAction(android.R.drawable.ic_delete, "remove update",
                        servicePendingIntent)
                */
                .setContentIntent(activityPendingIntent)
                //.setContentText(text)
                .setContentTitle(text)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .setSmallIcon(R.drawable.ic_person_pin_circle_white_48dp)
                .setTicker(text)
                .setWhen(System.currentTimeMillis()).build();
    }

    void requestLocationUpdates(Context context) {
        Log.i(TAG, "Requesting location updates");
        context.startService(new Intent(context, LocationUpdateService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            if (mFusedLocationClient != null) {
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            }
            //stopSelf();
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    void destroy() {
        removeLocationUpdates();
        if (mServiceHandler != null) {
            mServiceHandler.removeCallbacksAndMessages(null);
            mServiceHandler = null;
        }
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
        Intent intent = new Intent(MapActivity.USER_LOCATION_UPDATE);
        intent.putExtra(MapActivity.NEW_LOCATION, location);
        intent.putExtra(MapActivity.NEW_ADDRESS, address);
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

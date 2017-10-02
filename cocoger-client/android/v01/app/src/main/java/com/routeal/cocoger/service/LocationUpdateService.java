package com.routeal.cocoger.service;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.routeal.cocoger.MainApplication;

import static com.routeal.cocoger.service.LocationUpdate.DEFAULT_FOREGROUND_LOCATION_UPDATE_INTERVAL;
import static com.routeal.cocoger.service.LocationUpdate.FOREGROUND_LOCATION_UPDATE_INTERVAL;

public class LocationUpdateService extends Service {
    private final static String TAG = "tako";

    private final IBinder mBinder = new LocalBinder();

    private boolean mChangingConfiguration = false;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        // consecutive periodic location update
        LocationUpdateReceiver.scheduleUpdate(this, (AlarmManager) getSystemService(ALARM_SERVICE));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            LocationUpdate update = LocationUpdate.getInstance();
            update.exec(getApplicationContext());
        }
    };

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        LocationUpdateReceiver.cancelUpdate(this, (AlarmManager) getSystemService(ALARM_SERVICE));
        LocationUpdate.getInstance().destroy();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.d(TAG, "onBind");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.d(TAG, "onRebind");
        stopForeground(true);
        LocationUpdate update = LocationUpdate.getInstance();
        update.exec(getApplicationContext());
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        Log.d(TAG, "onUnbind: start in foreground service");
        if (!mChangingConfiguration) {
            startForeground(LocationUpdate.NOTIFICATION_ID, LocationUpdate.getInstance().getNotification(getApplicationContext()));
            LocationUpdate update = LocationUpdate.getInstance();
            update.exec(getApplicationContext());
        }
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        if (intent != null) {
            String mode = intent.getStringExtra(LocationUpdate.SERVICE_EXTRA_STARTED);
            if (mode != null) {
                if (mode.equals(LocationUpdate.SERVICE_STARTED_FROM_NOTIFICATION)) {
                    Log.d(TAG, "onStartCommand: startedFromNotification");
                    LocationUpdate.getInstance().removeLocationUpdates();
                } else if (mode.equals(LocationUpdate.SERVICE_STARTED_FROM_BOOT)) {
                    Log.d(TAG, "onStartCommand: startedFromBoot");
                    startForeground(LocationUpdate.NOTIFICATION_ID, LocationUpdate.getInstance().getNotification(getApplicationContext()));
                }
            }
        }

        return START_STICKY;
    }

    public class LocalBinder extends Binder {
        public LocationUpdateService getService() {
            return LocationUpdateService.this;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    public void requestLocationUpdate() {
        LocationUpdate update = LocationUpdate.getInstance();
        update.requestLocationUpdates(getApplicationContext());
    }

    public void removeLocationUpdate() {
        LocationUpdate update = LocationUpdate.getInstance();
        update.removeLocationUpdates();
    }
}

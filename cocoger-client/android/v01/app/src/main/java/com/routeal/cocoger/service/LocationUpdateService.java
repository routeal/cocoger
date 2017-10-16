package com.routeal.cocoger.service;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.util.Notifi;

public class LocationUpdateService extends Service {
    private final static String TAG = "tako";

    private final IBinder mBinder = new LocalBinder();

    private boolean mChangingConfiguration = false;
    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    Context context = LocationUpdateService.this;
                    LocationUpdateReceiver.scheduleUpdate(context, (AlarmManager) context.getSystemService(ALARM_SERVICE));
                }
            };

    public static void start() {
        Context context = MainApplication.getContext();
        Intent intent = new Intent(context, LocationUpdateService.class);
        context.startService(intent);
    }

    public static void stop() {
        // stop the location update service
        Context context = MainApplication.getContext();
        Intent intent = new Intent(context, LocationUpdateService.class);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        // consecutive periodic location update
        LocationUpdateReceiver.scheduleUpdate(this, (AlarmManager) getSystemService(ALARM_SERVICE));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);

        LocationUpdateReceiver.cancelUpdate(this, (AlarmManager) getSystemService(ALARM_SERVICE));

        LocationUpdate.getInstance().destroy();
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
            String action = intent.getAction();
            if (action != null) {
                Log.d(TAG, action);
                switch (action) {
                    case Intent.ACTION_BOOT_COMPLETED:
                        startForeground(LocationUpdate.NOTIFICATION_ID, LocationUpdate.getInstance().getNotification(getApplicationContext()));
                        break;
                    case LocationUpdate.ACTION_START_FROM_NOTIFICATION:
                        break;
                    case FB.ACTION_FRIEND_REQUEST_DECLINED: {
                        // delete the invite and invitee from the database
                        String invite = intent.getStringExtra(FB.NOTIFI_FRIEND_INVITE);
                        FB.declineFriendRequest(invite);
                        int nid = intent.getIntExtra(Notifi.ID, 0);
                        if (nid > 0) {
                            Notifi.remove(nid);
                        }
                        break;
                    }
                    case FB.ACTION_RANGE_REQUEST_DECLINED: {
                        // delete the invite and invitee from the database
                        String requester = intent.getStringExtra(FB.NOTIFI_RANGE_REQUESTER);
                        FB.declineRangeRequest(requester);
                        int nid = intent.getIntExtra(Notifi.ID, 0);
                        if (nid > 0) {
                            Notifi.remove(nid);
                        }
                        break;
                    }
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    public class LocalBinder extends Binder {
        public LocationUpdateService getService() {
            return LocationUpdateService.this;
        }
    }
}

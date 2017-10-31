package com.routeal.cocoger.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;

/**
 * Created by hwatanabe on 9/30/17.
 */

public class LocationUpdateReceiver extends BroadcastReceiver {

    static void scheduleUpdate(Context context, AlarmManager alarmManager) {
        int mInterval = 5; // seconds
        Intent i = new Intent(context, LocationUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + mInterval * 1000 - SystemClock.elapsedRealtime() % 1000, pi);
    }

    static void cancelUpdate(Context context, AlarmManager alarmManager) {
        Intent i = new Intent(context, LocationUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        alarmManager.cancel(pi);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        scheduleUpdate(context, (AlarmManager) context.getSystemService(Context.ALARM_SERVICE));

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LocationUpdateWakelock");
        wl.acquire();

        Handler handler = new Handler();
        Runnable periodicUpdate = new Runnable() {
            @Override
            public void run() {
                LocationUpdate update = LocationUpdate.getInstance();
                update.exec(context);
            }
        };
        handler.post(periodicUpdate);

        wl.release();
    }
}

package com.routeal.cocoger.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by hwatanabe on 4/11/17.
 */

public abstract class BasePeriodicService extends Service {

    protected abstract long getIntervalTime();

    protected abstract void execTask();

    protected abstract void makeNextPlan();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        execTask();
    }

    public void scheduleNextTime() {

        long now = System.currentTimeMillis();

        PendingIntent alarmSender = PendingIntent.getService(
                this,
                0,
                new Intent(this, this.getClass()),
                0
        );
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        am.set(
                AlarmManager.RTC,
                now + getIntervalTime(),
                alarmSender
        );
    }

    public void stopResident() {
        Intent intent = new Intent(this, this.getClass());

        PendingIntent pendingIntent = PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        stopSelf();
    }

}

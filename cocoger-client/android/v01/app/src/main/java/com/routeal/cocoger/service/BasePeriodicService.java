package com.routeal.cocoger.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * Created by hwatanabe on 4/11/17.
 */

public abstract class BasePeriodicService extends Service {

    protected abstract long getIntervalTime();

    protected abstract void execTask();

    protected abstract void makeNextPlan();

    protected final IBinder binder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public BasePeriodicService startResident(Context context) {
        Intent intent = new Intent(context, this.getClass());
        intent.putExtra("type", "start");
        context.startService(intent);

        return this;
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

    public void stopResident(Context context) {
        Intent intent = new Intent(context, this.getClass());

        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        stopSelf();
    }

}
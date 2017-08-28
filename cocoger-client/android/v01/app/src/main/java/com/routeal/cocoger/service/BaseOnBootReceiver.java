package com.routeal.cocoger.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by hwatanabe on 4/11/17.
 */

public abstract class BaseOnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {

        String action = intent.getAction();

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    onDeviceBoot(context);
                }
            }).start();
        }
    }

    protected abstract void onDeviceBoot(Context context);
}

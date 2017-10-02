package com.routeal.cocoger.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by hwatanabe on 4/11/17.
 */

public class OnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(context, LocationUpdateService.class);
                    intent.putExtra(LocationUpdate.SERVICE_EXTRA_STARTED, LocationUpdate.SERVICE_STARTED_FROM_BOOT);
                    context.startService(intent);
                }
            }).start();
        }
    }

}

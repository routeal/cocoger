package com.routeal.cocoger.service;

import android.content.Context;

/**
 * Created by hwatanabe on 4/11/17.
 */

public class OnBootReceiver extends BaseOnBootReceiver {
    @Override
    protected void onDeviceBoot(Context context) {
        new MailSyncService().startResident(context);
    }
}

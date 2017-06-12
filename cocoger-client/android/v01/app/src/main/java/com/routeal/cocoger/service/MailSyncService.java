package com.routeal.cocoger.service;

import android.content.Context;

/**
 * Created by hwatanabe on 4/11/17.
 */

public class MailSyncService extends BasePeriodicService {

    public static BasePeriodicService activeService;

    private static final String TAG = "MailSyncService";

    @Override
    protected long getIntervalTime() {
        return 1000 * 5;
    }

    @Override
    protected void execTask() {
        activeService = this;

        //
        // background processing here
        //

        makeNextPlan();
    }

    @Override
    public void makeNextPlan() {
        this.scheduleNextTime();
    }

    public static void stopResidentIfActive(Context context) {
        if (activeService != null) {
            activeService.stopResident(context);
        }
    }
}

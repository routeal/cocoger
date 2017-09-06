package com.routeal.cocoger.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.routeal.cocoger.fb.FB;
import com.routeal.cocoger.util.Notifi;

/**
 * Created by nabe on 8/27/17.
 */

public class MainReceiver extends BroadcastReceiver {
    private final static String TAG = "MainReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            String action = intent.getAction();
            if (action.equals(FB.ACTION_FRIEND_REQUEST_DECLINED)) {
                Log.d(TAG, action);

                // delete the invite and invitee from the database
                String invite = intent.getStringExtra("friend_invite");
                FB.declineFriendRequest(invite);

                int nid = intent.getIntExtra("notification_id", 0);
                Notifi.remove(nid);
            } else if (action.equals(FB.ACTION_RANGE_REQUEST_DECLINED)) {
                Log.d(TAG, action);

                // delete the invite and invitee from the database
                String requester = intent.getStringExtra("range_requester");
                FB.declineRangeRequest(requester);

                int nid = intent.getIntExtra("notification_id", 0);
                Notifi.remove(nid);
            }

        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
    }
}

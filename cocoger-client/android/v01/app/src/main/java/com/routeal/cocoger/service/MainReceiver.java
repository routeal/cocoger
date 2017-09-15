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

/**
 * Receives the notifications intents which do not want the UI
 * started.
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
                String invite = intent.getStringExtra(FB.NOTIFI_FRIEND_INVITE);
                FB.declineFriendRequest(invite);

                int nid = intent.getIntExtra(Notifi.ID, 0);
                if (nid > 0) {
                    Notifi.remove(nid);
                }
            } else if (action.equals(FB.ACTION_RANGE_REQUEST_DECLINED)) {
                Log.d(TAG, action);

                // delete the invite and invitee from the database
                String requester = intent.getStringExtra(FB.NOTIFI_RANGE_REQUETER);
                FB.declineRangeRequest(requester);

                int nid = intent.getIntExtra(Notifi.ID, 0);
                if (nid > 0) {
                    Notifi.remove(nid);
                }
            }

        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
    }
}

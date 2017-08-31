package com.routeal.cocoger.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.mikepenz.iconics.Iconics.TAG;

/**
 * Created by nabe on 8/27/17.
 */

public class MainReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(MainService.ACTION_FRIEND_REQUEST_DECLINED)) {
            Log.d(TAG, action);

            // delete the invite and invitee from the database
            FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
            String invitee = fbUser.getUid();
            String invite = intent.getStringExtra("friend_invite");

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");

            userRef.child(invitee).child("invitees").child(invite).removeValue();
            userRef.child(invite).child("invites").child(invitee).removeValue();

            int nid = intent.getIntExtra("notification_id", 0);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(nid);
        }
        else if (action.equals(MainService.ACTION_RANGE_REQUEST_DECLINED)) {
            Log.d(TAG, action);

            // delete the invite and invitee from the database
            FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
            String invitee = fbUser.getUid();
            String invite = intent.getStringExtra("range_request");

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");

            userRef.child(invitee).child("friends").child(invite).child("request").removeValue();

            int nid = intent.getIntExtra("notification_id", 0);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(nid);
        }

    }
}

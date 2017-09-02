package com.routeal.cocoger.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Random;

public class NotificationHelper {

    void send(String title, String content, String icon, Intent accept, Intent decline) {
        int nid = new Random().nextInt();

        Context context = MainApplication.getContext();

        PendingIntent pendingAcceptIntent = PendingIntent.getActivity(context, 1, accept, 0);
        NotificationCompat.Action acceptAction = new NotificationCompat.Action.Builder(
            R.drawable.ic_contacts_black_18dp,
            "Accept", pendingAcceptIntent).build();

        PendingIntent pendingDeclineIntent = PendingIntent.getBroadcast(context, 1, decline, 0);
        NotificationCompat.Action declineAction = new NotificationCompat.Action.Builder(
            R.drawable.ic_contacts_black_18dp,
            "Decline", pendingDeclineIntent).build();

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_person_pin_circle_white_48dp)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .addAction(acceptAction)
            .addAction(declineAction);

        // seems not working, use notificationmanager's cancel method
        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        Picasso.with(context).load(icon).transform(new CircleTransform()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        mBuilder.setLargeIcon(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });

        NotificationManager mNotificationManager =
            (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

        mNotificationManager.notify(nid, notification);
    }

}

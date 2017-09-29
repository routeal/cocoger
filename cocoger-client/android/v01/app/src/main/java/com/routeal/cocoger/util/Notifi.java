package com.routeal.cocoger.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;

public class Notifi {

    public final static String ID = "notification_id";

    public final static String TAG = "Notifi";

    public static void send(final int nid, final String title, final String content, String icon, Intent accept, Intent decline) {
        final Context context = MainApplication.getContext();

        accept.putExtra(ID, nid);
        decline.putExtra(ID, nid);

        PendingIntent pendingAcceptIntent =
                PendingIntent.getActivity(context, nid, accept, PendingIntent.FLAG_CANCEL_CURRENT);
        final NotificationCompat.Action acceptAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_contacts_black_18dp,
                context.getResources().getString(R.string.accept), pendingAcceptIntent).build();

        PendingIntent pendingDeclineIntent =
                PendingIntent.getBroadcast(context, nid, decline, PendingIntent.FLAG_CANCEL_CURRENT);
        final NotificationCompat.Action declineAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_contacts_black_18dp,
                context.getResources().getString(R.string.decline), pendingDeclineIntent).build();

        new LoadImage.LoadImageAsync(true, new LoadImage.LoadImageListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_person_pin_circle_white_48dp)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setLargeIcon(bitmap)
                        .addAction(acceptAction)
                        .addAction(declineAction);

                // seems not working, use notificationmanager's cancel method
                Notification notification = mBuilder.build();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;

                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

                mNotificationManager.notify(nid, notification);
            }
        }).execute(icon);
    }

    public static void send(final int nid, final String title, final String content, String icon) {
        final Context context = MainApplication.getContext();

        new LoadImage.LoadImageAsync(true, new LoadImage.LoadImageListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_person_pin_circle_white_48dp)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setLargeIcon(bitmap);

                // seems not working, use notificationmanager's cancel method
                Notification notification = mBuilder.build();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;

                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

                mNotificationManager.notify(nid, notification);
            }
        }).execute(icon);
    }

    public static void remove(int id) {
        if (id == 0) return;
        Context context = MainApplication.getContext();
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(id);
    }

}

package com.dcalabrese22.dan.chatter.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.dcalabrese22.dan.chatter.MainActivity;
import com.dcalabrese22.dan.chatter.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

/**
 * Created by dcalabrese on 10/16/2017.
 */

//class for receiving notifications sent by firebase
public class ChatterFirebaseMessagingService extends FirebaseMessagingService {

    public static final String FROM_NOTIFICATION = "from_notification";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String conversationId = remoteMessage.getData().get("id");
        if (isAppInForeground()) {
            showNotificationWithNoSound(title, body, buildNotificationIntent(conversationId));
        } else {
            showNotificationWithSound(title, body, buildNotificationIntent(conversationId));
        }
    }

    //builds a pendingintent for the notification
    private PendingIntent buildNotificationIntent(String id) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(FROM_NOTIFICATION, id);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        return pendingIntent;
    }

    //builds a notification with sound for when the app is in the background
    private void showNotificationWithSound(String title, String body, PendingIntent pendingIntent) {
        int notifyId = 1;
        String channelId = "com.dcalabrese22.dan.chatter";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setChannelId(channelId);
        notificationBuilder.setDefaults(Notification.DEFAULT_ALL);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }

    //builds a notification without sounds for when the app is in the foreground
    private void showNotificationWithNoSound(String title, String body, PendingIntent pendingIntent) {
        int notifyId = 1;
        String channelId = "com.dcalabrese22.dan.chatter";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setChannelId(channelId);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }

    //determines if the app is in the foreground
    private boolean isAppInForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> services = activityManager.getRunningAppProcesses();
        boolean isActivityFound = false;

        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : services) {
            if (runningAppProcessInfo.processName.equalsIgnoreCase(this.getPackageName()) &&
                    runningAppProcessInfo.importance ==
                            ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                isActivityFound = true;
            }
        }
        return isActivityFound;
    }
}

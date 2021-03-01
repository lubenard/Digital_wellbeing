package com.lubenard.digital_wellbeing;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.lubenard.digital_wellbeing.settings.SettingsFragment;

public class NotificationsHandler {
    public void createPermanentNotification(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("PERMANENT_CHANNEL",
                    context.getString(R.string.notif_permanent_name), NotificationManager.IMPORTANCE_MIN);
            channel.setDescription(context.getString(R.string.notif_permanent_channel_desc));
            // Do not show badge
            channel.setShowBadge(false);
            mNotificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder permNotifBuilder = new NotificationCompat.Builder(context, "PERMANENT_CHANNEL");
        // Set icon
        permNotifBuilder.setSmallIcon(R.drawable.ic_perm_notification);
        // Set main notif name
        permNotifBuilder.setContentTitle(context.getString(R.string.app_name));
        // Set more description of the notif
        permNotifBuilder.setContentText(context.getString(R.string.notif_permanent_notif_desc));
        // Set the notif as not removable by user
        permNotifBuilder.setOngoing(true);
        // Do not show time on the notif
        permNotifBuilder.setShowWhen(false);

        Intent intent = new Intent(context, SettingsFragment.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        permNotifBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, permNotifBuilder.build());

    }

    public void sendNormalNotification(Context context, String title, String content, int drawable) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("NORMAL_CHANNEL",
                    context.getString(R.string.notif_normal_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.notif_normal_channel_desc));
            // Do not show badge
            channel.setShowBadge(false);
            mNotificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder permNotifBuilder = new NotificationCompat.Builder(context, "NORMAL_CHANNEL");
        // Set icon
        permNotifBuilder.setSmallIcon(drawable);
        // Set main notif name
        permNotifBuilder.setContentTitle(title);
        // Set more description of the notif
        permNotifBuilder.setContentText(content);
        // Do not show time on the notif
        //permNotifBuilder.setShowWhen(false);

        Intent intent = new Intent(context, SettingsFragment.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        permNotifBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, permNotifBuilder.build());
    }

}

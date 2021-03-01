package com.lubenard.digital_wellbeing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lubenard.digital_wellbeing.Utils.Utils;

public class NotificationBoradcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DbManager dbManager = new DbManager(context);
        new NotificationsHandler().sendNormalNotification(context, "Reminder:",
                "You spent " + Utils.formatTimeSpent(dbManager.getScreenTime(Utils.getTodayDate())) + " on your screen today",
                R.drawable.ic_perm_notification);
    }
}

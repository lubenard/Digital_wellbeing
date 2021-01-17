package com.lubenard.digital_wellbeing.Utils;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static final String TAG = "Utils";

    /**
     * Get icon from package name
     * @param packageName Example 'com.facebook.messenger'
     * @return The drawable if found, or null if not
     */
    public static Drawable getIconFromPkgName(Context ctx, String packageName) {
        Log.d(TAG,"GetIconFromPKG: "+ packageName);
        try {
            return ctx.getPackageManager().getApplicationIcon(packageName);
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "icon for " + packageName + " not found");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the app name for a given package name
     * For example, submitting com.lubenard.digital_wellbeing will return 'Digital Wellbeing'
     * Only work if the app is installed on your system
     * @param ctx Context
     * @param packageName given package name
     * @return The 'human readable' name of the app, or '(unknown)' if not found
     */
    public static String getAppName(Context ctx, String packageName) {
        final PackageManager pm = ctx.getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
    }

    /**
     * Get the today's date in formatted format dd-MM-yyyy
     * Example: 25 January 2020 is 25-01-2020
     * @return Return today's date
     */
    public static String getTodayDate() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(date);
    }

    /**
     * Get the app in foreground
     * Took this code from: https://github.com/ricvalerio/foregroundappchecker
     * @param context
     * @return the package name of the app in foreground
     */
    public static String getCurrentForegroundApp(Context context) {
        String foregroundApp = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Service.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();

            UsageEvents usageEvents = mUsageStatsManager.queryEvents(time - 1000 * 3600, time);
            UsageEvents.Event event = new UsageEvents.Event();
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND)
                    foregroundApp = event.getPackageName();
            }
        } else {
            ActivityManager am = (ActivityManager) context.getSystemService(Service.ACTIVITY_SERVICE);
            ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
            String foregroundTaskPackageName = foregroundTaskInfo .topActivity.getPackageName();
            PackageManager pm = context.getPackageManager();
            PackageInfo foregroundAppPackageInfo = null;
            try {
                foregroundAppPackageInfo = pm.getPackageInfo(foregroundTaskPackageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            if(foregroundAppPackageInfo != null)
                foregroundApp = foregroundAppPackageInfo.applicationInfo.packageName;
        }
        return foregroundApp;
    }
}
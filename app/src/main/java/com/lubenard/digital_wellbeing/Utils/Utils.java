package com.lubenard.digital_wellbeing.Utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
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

import androidx.core.content.ContextCompat;

import com.lubenard.digital_wellbeing.R;

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return ctx.getDrawable(android.R.mipmap.sym_def_app_icon);
            } else {
                return ctx.getResources().getDrawable(android.R.mipmap.sym_def_app_icon);
            }
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
        return (String) (ai != null ? pm.getApplicationLabel(ai) : packageName);
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
     *
     * @param permRequired
     * @return
     */
    public static boolean checkOrRequestPerm(Activity activity, Context context, String permRequired) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(context, permRequired) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else if (activity.shouldShowRequestPermissionRationale(permRequired)) {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                //new AlertDialog.Builder(context).setTitle(context.getResources().getString(R.string.alertdialog_perm_not_granted_title))
                 //   .setMessage(context.getResources().getString(R.string.alertdialog_perm_not_granted_desc)).setPositiveButton(context.getResources().getString(R.));
                return false;
            } else {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                activity.requestPermissions(new String[]{permRequired}, 1);
                return true;
            }
        }
        return false;
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

    public static String formatTimeSpent(int timeInMinute) {
        return String.format("%d:%02d", timeInMinute / 60, timeInMinute % 60);
    }
}

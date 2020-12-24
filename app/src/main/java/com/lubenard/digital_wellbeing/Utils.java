package com.lubenard.digital_wellbeing;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
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
     * Get the today's date in formatted format
     * @return Return today's date
     */
    public static String getTodayDate() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(date);
    }

}

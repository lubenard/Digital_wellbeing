package com.lubenard.digital_wellbeing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.preference.PreferenceManager;

/**
 * Start the app at boot, if the user decide in the settings
 */
public class Autostart extends BroadcastReceiver {

    public void onReceive(Context context, Intent arg1)
    {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        Boolean autostart_option = sharedPreferences.getBoolean("tweaks_restart_reboot", true);
        Log.d("Autostart", "Autostart option is " + autostart_option);
        if (autostart_option) {
            Intent intent = new Intent(context,BackgroundService.class);
            MainFragment.setBgService(intent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
            Log.d("Autostart", "Autostart has started the service");
        }
    }
}

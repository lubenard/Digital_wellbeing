package com.lubenard.digital_wellbeing;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

/**
 * Decide whether or not the app should launch the main Fragment or the permission Fragment
 */
public class LauncherActivity extends AppCompatActivity {

    private void checkConfig() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        // Create notification if needed
        Boolean permanent_notification_option = sharedPreferences.getBoolean("tweaks_permanent_notification", false);
        if (permanent_notification_option)
            new NotificationsHandler().createPermanentNotification(this);

        String theme_option = sharedPreferences.getString("ui_theme", "dark");
        switch (theme_option) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "white":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "battery_saver":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                break;
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        checkConfig();
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (PermissionsHandler.checkIfUsagePermissionGranted(this)) {
            // If permission is granted
            MainFragment fragment = new MainFragment();
            fragmentTransaction.replace(android.R.id.content, fragment);
        } else {
            // If permissions not granted
            PermissionsHandler fragment = new PermissionsHandler();
            fragmentTransaction.replace(android.R.id.content, fragment);
        }
        fragmentTransaction.commit();
    }
}

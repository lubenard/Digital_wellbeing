package com.lubenard.digital_wellbeing.settings;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Messenger;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.lubenard.digital_wellbeing.BackgroundService;
import com.lubenard.digital_wellbeing.DbManager;
import com.lubenard.digital_wellbeing.MainFragment;
import com.lubenard.digital_wellbeing.NotificationsHandler;
import com.lubenard.digital_wellbeing.R;

import java.util.logging.Logger;

/**
 * Settings page.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_fragment, rootKey);
        getActivity().setTitle(R.string.settings_fragment_title);

        // Theme change listener
        final Preference theme = findPreference("ui_theme");
        theme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("Setting page", "Theme value has changed for " + newValue);
                switch (newValue.toString()) {
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
                Log.d("Test", "Is this notif played ?");
                /*getFragmentManager()
                        .beginTransaction()
                        .detach(SettingsFragment.this)
                        .attach(SettingsFragment.this)
                        .commit();*/
                return true;
            }
        });

        // reset preference click listener
        Preference reset = findPreference("tweaks_erase_data");
        reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete all your data")
                        .setMessage("Are you sure you want to all your data ? All losses are definitive")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Delete DB
                                getContext().deleteDatabase(DbManager.getDBName());
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
            }
        });

        //Permanent notif change listener
        final Preference permanentNotif = findPreference("tweaks_permanent_notification");
        permanentNotif.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("Setting page", "permanent notif value has changed for " + newValue);

                if (newValue.toString().equals("true")) {
                    Log.d("Setting page", "permanent notif is true");
                    new NotificationsHandler().createPermanentNotification(getContext());
                } else {
                    NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(getContext().NOTIFICATION_SERVICE);
                    Log.d("Setting page", "permanent notif is false");
                    mNotificationManager.cancel(0);
                }
                return true;
            }
        });

        // feedback preference click listener
        Preference myPref = findPreference("other_feedback");
        myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","lubenard@protonmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Digital Wellbeing");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                return true;
            }
        });
    }
}

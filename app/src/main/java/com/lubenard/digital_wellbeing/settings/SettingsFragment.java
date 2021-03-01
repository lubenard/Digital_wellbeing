package com.lubenard.digital_wellbeing.settings;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.lubenard.digital_wellbeing.DbManager;
import com.lubenard.digital_wellbeing.NotificationsHandler;
import com.lubenard.digital_wellbeing.R;
import com.lubenard.digital_wellbeing.Utils.Utils;

import java.util.Locale;

/**
 * Settings page.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String TAG = "SettingsFragment";
    private static Activity activity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_fragment, rootKey);
        activity = getActivity();
        activity.setTitle(R.string.settings_fragment_title);

        // Language change listener
        final Preference language = findPreference("ui_language");
        language.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d(TAG, "Language value has changed for " + newValue);
                switch (newValue.toString()) {
                    case "en":
                        setAppLocale("en-us");
                        break;
                    case "fr":
                        setAppLocale("fr");
                        break;
                    case "system":
                        break;
                }
                return true;
            }
        });

        // Theme change listener
        final Preference theme = findPreference("ui_theme");
        theme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d(TAG, "Theme value has changed for " + newValue);
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
                return true;
            }
        });

        Preference importXML = findPreference("tweaks_import_data_xml");
        importXML.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (!Utils.checkOrRequestPerm(getActivity(), getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    return false;
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.custom_restore_title_alertdialog);
                final View customLayout = getLayoutInflater().inflate(R.layout.custom_backup_restore_alertdialog, null);

                ((CheckBox)customLayout.findViewById(R.id.custom_backup_restore_alertdialog_datas)).setText(R.string.custom_restore_alertdialog_save_datas);
                ((CheckBox)customLayout.findViewById(R.id.custom_backup_restore_alertdialog_settings)).setText(R.string.custom_restore_alertdialog_save_settings);

                builder.setView(customLayout);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getContext(), BackupAndRestoreFragment.class);
                        intent.putExtra("mode", 3);

                        boolean isDatasChecked =
                                ((CheckBox)customLayout.findViewById(R.id.custom_backup_restore_alertdialog_datas)).isChecked();
                        boolean isSettingsChecked =
                                ((CheckBox)customLayout.findViewById(R.id.custom_backup_restore_alertdialog_settings)).isChecked();

                        if (!isDatasChecked && !isSettingsChecked)
                            return;

                        intent.putExtra("shouldBackupRestoreDatas", isDatasChecked);
                        intent.putExtra("shouldBackupRestoreSettings", isSettingsChecked);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel,null);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });

        Preference exportXML = findPreference("tweaks_export_data_xml");
        exportXML.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (!Utils.checkOrRequestPerm(getActivity(), getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    return false;
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.custom_backup_title_alertdialog);
                final View customLayout = getLayoutInflater().inflate(R.layout.custom_backup_restore_alertdialog, null);
                builder.setView(customLayout);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getContext(), BackupAndRestoreFragment.class);
                        intent.putExtra("mode", 1);

                        boolean isDatasChecked =
                                ((CheckBox)customLayout.findViewById(R.id.custom_backup_restore_alertdialog_datas)).isChecked();
                        boolean isSettingsChecked =
                                ((CheckBox)customLayout.findViewById(R.id.custom_backup_restore_alertdialog_settings)).isChecked();

                        if (!isDatasChecked && !isSettingsChecked)
                            return;

                        intent.putExtra("shouldBackupRestoreDatas", isDatasChecked);
                        intent.putExtra("shouldBackupRestoreSettings", isSettingsChecked);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel,null);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });

        Preference exportSQLITE = findPreference("tweaks_export_data_sqlite");
        exportSQLITE.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (!Utils.checkOrRequestPerm(getActivity(), getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    return false;

                Intent intent = new Intent(getContext(), BackupAndRestoreFragment.class);
                intent.putExtra("mode", 2);
                startActivity(intent);
                return true;
            }
        });

        // reset preference click listener
        Preference reset = findPreference("tweaks_erase_data");
        reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.settings_alertdialog_erase_title)
                        .setMessage("Are you sure you want to delete all your data ? All losses are definitive. Once completed, the app will shut down.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Delete DB
                                getContext().deleteDatabase(DbManager.getDBName());
                                // This is sending a -9 signal. I do not like this.
                                // TODO: find a way to shut down the app as well as the intent
                                android.os.Process.killProcess(android.os.Process.myPid());
                                //getActivity().finish(); // <-- This is better but does not kill the intent
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
                Log.d(TAG, "permanent notif value has changed for " + newValue);

                if (newValue.toString().equals("true")) {
                    Log.d(TAG, "permanent notif is true");
                    new NotificationsHandler().createPermanentNotification(getContext());
                } else {
                    NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(getContext().NOTIFICATION_SERVICE);
                    Log.d(TAG, "permanent notif is false");
                    mNotificationManager.cancel(0);
                }
                return true;
            }
        });

        // reset preference click listener
        Preference devSettings = findPreference("other_dev_settings");
        devSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                DevFragment devFragment = new DevFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, devFragment)
                        .addToBackStack(null).commit();
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

    public static void restartActivity() {
        activity.recreate();
    }

    private final void setAppLocale(String localeCode) {
        Locale myLocale = new Locale(localeCode);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SettingsFragment fragment = new SettingsFragment();
        fragmentTransaction.replace(android.R.id.content, fragment);
        fragmentTransaction.commit();
    }
}

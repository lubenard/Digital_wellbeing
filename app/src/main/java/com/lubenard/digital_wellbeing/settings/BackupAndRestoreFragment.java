package com.lubenard.digital_wellbeing.settings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.lubenard.digital_wellbeing.DbManager;
import com.lubenard.digital_wellbeing.R;
import com.lubenard.digital_wellbeing.Utils.XmlWriter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class BackupAndRestoreFragment extends Activity {

    public static final String TAG = "BackupAndRestore";
    private static final int MODE_NBR_UNLOCKS = 0;
    private static final int MODE_SCREENTIME = 1;
    private static final int MODE_APPTIME = 2;

    private String mode;
    private int typeOfDatas;
    private AlertDialog dialog;
    private boolean shouldBackupRestoreDatas;
    private boolean shouldBackupRestoreSettings;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        typeOfDatas = getIntent().getIntExtra("mode", -1);
        shouldBackupRestoreDatas = getIntent().getBooleanExtra("shouldBackupRestoreDatas", false);
        shouldBackupRestoreSettings = getIntent().getBooleanExtra("shouldBackupRestoreSettings", false);
        if (typeOfDatas == 1)
            startBackupIntoXML();
        else if (typeOfDatas == 2)
            startBackupIntoSQLITE();
        else if (typeOfDatas == 3)
            startRestoreFromXML();
    }

    private boolean startBackupIntoXML() {
        mode = "backup";
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent dataToFileChooser = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            dataToFileChooser.setType("text/xml");
            dataToFileChooser.putExtra(Intent.EXTRA_TITLE, "myDatas.xml");
            launchIntent(dataToFileChooser);
        } else {
            Log.w(TAG, "Failed to open a file explorer. Save will be at default location");
            Toast.makeText(this, R.string.toast_error_custom_path_backup_restore_android_too_old, Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private boolean startBackupIntoSQLITE() {
        mode = "backup";
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent dataToFileChooser = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            dataToFileChooser.setType("application/vnd.sqlite3");
            dataToFileChooser.putExtra(Intent.EXTRA_TITLE, "myDatas.db");
            launchIntent(dataToFileChooser);
        } else {
            Log.w(TAG, "Android version too old. Save will be at default location");
            Toast.makeText(this, R.string.toast_error_custom_path_backup_restore_android_too_old, Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private boolean startRestoreFromXML() {
        mode = "restore";
        Log.d(TAG, "startRestoreFromXML");
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent dataToFileChooser = new Intent(Intent.ACTION_GET_CONTENT);
            dataToFileChooser.setType("text/xml");
            launchIntent(dataToFileChooser);
        } else {
            Log.w(TAG, "Android version too old. Save will be at default location");
            Toast.makeText(this, R.string.toast_error_custom_path_backup_restore_android_too_old, Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private void launchIntent(Intent dataToFileChooser) {
        try {
            startActivityForResult(dataToFileChooser, 1);
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "Failed to open a file explorer. Save will be at default location");
            Toast.makeText(this, R.string.toast_error_custom_path_backup_restore_fail, Toast.LENGTH_LONG).show();
        }
    }

    private void createAlertDialog() {
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Saving datas");

        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.loading_layout, null);
        builder.setView(customLayout);

        // create and show
        // the alert dialog
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void saveSettingsIntoXml(XmlWriter xmlWriter) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            xmlWriter.writeEntity("settings");

            xmlWriter.writeEntity("ui_language");
            xmlWriter.writeText(preferences.getString("ui_language", "system"));
            xmlWriter.endEntity();

            xmlWriter.writeEntity("ui_theme");
            xmlWriter.writeText(preferences.getString("ui_theme", "dark"));
            xmlWriter.endEntity();

            xmlWriter.writeEntity("tweaks_permanent_notification");
            xmlWriter.writeText(String.valueOf(preferences.getBoolean("datas_permanent_notification", false)));
            xmlWriter.endEntity();

            xmlWriter.writeEntity("tweaks_reminder_notif_enable");
            xmlWriter.writeText(String.valueOf(preferences.getBoolean("tweaks_reminder_notif_enable", false)));
            xmlWriter.endEntity();

            xmlWriter.writeEntity("tweaks_reminder_notif_time");
            xmlWriter.writeText(preferences.getString("tweaks_reminder_notif_time", "00:00"));
            xmlWriter.endEntity();

            xmlWriter.endEntity();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDatasIntoXml(XmlWriter xmlWriter) {
        DbManager dbManager = new DbManager(this);
        ArrayList<String> arrayOfApps = dbManager.getAllApps();

        // Datas containing all saved datas
        try {
            xmlWriter.writeEntity("datas");
            // Contain number of unlocks
            xmlWriter.writeEntity("numberOfUnlocks");
            LinkedHashMap<String, Integer>  datas_numberOfUnlocks = dbManager.getAllUnclocks();
            for (HashMap.Entry<String, Integer> one_data : datas_numberOfUnlocks.entrySet()) {
                xmlWriter.writeEntity("item");
                xmlWriter.writeAttribute("date", one_data.getKey());
                xmlWriter.writeText(String.valueOf(one_data.getValue()));
                xmlWriter.endEntity();
            }
            xmlWriter.endEntity();

            // Contain number of screenTime
            xmlWriter.writeEntity("screenTime");
            LinkedHashMap<String, Integer>  datas_screenTime = dbManager.getAllScreenTime();
            for (HashMap.Entry<String, Integer> one_data : datas_screenTime.entrySet()) {
                xmlWriter.writeEntity("item");
                xmlWriter.writeAttribute("date", one_data.getKey());
                xmlWriter.writeText(String.valueOf(one_data.getValue()));
                xmlWriter.endEntity();
            }
            xmlWriter.endEntity();

            // Contain number of time per app
            xmlWriter.writeEntity("appTime");

            for (int i = 0; i != arrayOfApps.size(); i++) {
                xmlWriter.writeEntity("app");
                xmlWriter.writeAttribute("name", arrayOfApps.get(i));
                LinkedHashMap<String, Integer>  datas_appTime = dbManager.getDetailsForApp(arrayOfApps.get(i), 0, true);

                for (HashMap.Entry<String, Integer> one_data : datas_appTime.entrySet()) {
                    xmlWriter.writeEntity("item");
                    xmlWriter.writeAttribute("date", one_data.getKey());
                    xmlWriter.writeText(String.valueOf(one_data.getValue()));
                    xmlWriter.endEntity();
                }
                xmlWriter.endEntity();
            }
            xmlWriter.endEntity();

            xmlWriter.endEntity();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDatasIntoDB(OutputStream outputStream) {
        try {
            File dbFile = new File(this.getDatabasePath("dataDB").getAbsolutePath());
            FileInputStream fis = new FileInputStream(dbFile);

            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            // Close the streams
            outputStream.flush();
            outputStream.close();
            fis.close();

        } catch (IOException e) {
            Log.e("dbBackup:", e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null && data.getDataString() != null) {
            createAlertDialog();
            if (mode.equals("backup")) {
                Log.d(TAG, "ActivityResult backup at path: " + data.getDataString());
                try {
                    OutputStream outputStream = getContentResolver().openOutputStream(Uri.parse(data.getDataString()));

                    if (typeOfDatas == 1) {
                        XmlWriter xmlWriter = new XmlWriter(outputStream);
                        if (shouldBackupRestoreDatas)
                            saveDatasIntoXml(xmlWriter);
                        if (shouldBackupRestoreSettings)
                            saveSettingsIntoXml(xmlWriter);
                        xmlWriter.close();
                    } else if (typeOfDatas == 2) {
                        saveDatasIntoDB(outputStream);
                    }

                } catch (IOException e) {
                    Log.d(TAG, "something failed during the save of the datas");
                    e.printStackTrace();
                }
                Toast.makeText(this, "Success to save the datas", Toast.LENGTH_LONG).show();
            } else if (mode.equals("restore")) {
                Log.d(TAG, "ActivityResult restore from path: " + data.getDataString());
                try {
                    InputStream inputStream = getContentResolver().openInputStream(Uri.parse((data.getDataString())));

                    if (shouldBackupRestoreDatas)
                        restoreDatasFromXml(inputStream);
                    if (shouldBackupRestoreSettings)
                        restoreSettingsFromXml(inputStream);

                } catch (IOException e) {
                    Log.d(TAG, "something failed during the restore of the datas");
                    e.printStackTrace();
                }
            }
            dialog.dismiss();
            finish();
        } else {
            finish();
        }
    }

    private void restoreDatasFromXml(InputStream inputStream) {
        try {
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myParser = xmlFactoryObject.newPullParser();
            int currentState = -1;
            DbManager dbManager = new DbManager(getApplicationContext());
            String currentDate = null;
            String currentApp = null;

            myParser.setInput(inputStream, null);

            int eventType = myParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (myParser.getName().equals("numberOfUnlocks")) {
                        currentState = MODE_NBR_UNLOCKS;
                        Log.d(TAG, "Actually into MODE_NBR_UNLOCKS");
                    }
                    else if (myParser.getName().equals("screenTime")) {
                        currentState = MODE_SCREENTIME;
                        Log.d(TAG, "Actually into MODE_SCREENTIME");
                    }
                    else if (myParser.getName().equals("appTime")) {
                        currentState = MODE_APPTIME;
                        Log.d(TAG, "Actually into MODE_APPTIME");
                    }
                    if (currentState == MODE_APPTIME && myParser.getName().equals("app")) {
                        currentApp = myParser.getAttributeValue(null, "name");
                        Log.d(TAG, "Current app is " + currentApp);
                    }
                    if (myParser.getName().equals("item")) {
                        currentDate = myParser.getAttributeValue(null, "date");
                        if (currentState == MODE_NBR_UNLOCKS && currentDate != null) {
                            myParser.next();
                            dbManager.updateUnlocks(Integer.parseInt(myParser.getText()), currentDate);
                        }
                        if (currentState == MODE_SCREENTIME && currentDate != null) {
                            myParser.next();
                            dbManager.updateScreenTime(Integer.parseInt(myParser.getText()), currentDate);
                        }
                        if (currentState == MODE_APPTIME && currentDate != null) {
                            myParser.next();
                            HashMap hashMap = new HashMap<String, Integer>();
                            hashMap.put(currentApp, Integer.parseInt(myParser.getText()));
                            dbManager.updateAppData(hashMap, currentDate);
                        }
                    }
                }
                eventType = myParser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: OPTIMIZE THIS FUNCTION
    private void restoreSettingsFromXml(InputStream inputStream) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myParser = xmlFactoryObject.newPullParser();

            myParser.setInput(inputStream, null);

            int eventType = myParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                   if (myParser.getName().equals("ui_language")) {
                       myParser.next();
                       Log.d(TAG, "ui_language setting = " + myParser.getText());
                       preferences.edit().putString("ui_language", myParser.getText()).apply();
                   } else if (myParser.getName().equals("ui_theme")) {
                       myParser.next();
                       Log.d(TAG, "ui_theme setting = " + myParser.getText());
                       preferences.edit().putString("ui_theme", myParser.getText()).apply();
                   } else if (myParser.getName().equals("datas_permanent_notification")) {
                       myParser.next();
                       Log.d(TAG, "datas_permanent_notification setting = " + myParser.getText());
                       preferences.edit().putBoolean("datas_permanent_notification", Boolean.parseBoolean(myParser.getText())).apply();
                   }
                }
                eventType = myParser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        // Recreate activity once all settings have been restored
        SettingsFragment.restartActivity();
    }
}

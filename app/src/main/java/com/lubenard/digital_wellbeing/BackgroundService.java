package com.lubenard.digital_wellbeing;

import android.app.IntentService;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.lubenard.digital_wellbeing.Utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service running in background so we can use other apps without the service being stopped
 */
public class BackgroundService extends IntentService {

    public static final String TAG = "BackgroundService";

    BroadcastReceiver mReceiver;

    private static DbManager dbManager;
    private static String todayDate;
    // This variable is in seconds
    private static short mTimer = 0;

    //This variable is in Minutes
    private short screenTimeToday = 0;
    private HashMap<String, Integer> app_data;
    private static int numberOfUnlocks = 0;
    private static Intent gIntent;

    /**
     * Constructor of the class
     */
    public BackgroundService() {
        super("Launch");
    }

    public static void increaseNumberOfUnlocks() {
        numberOfUnlocks++;
        Log.d(TAG, "Number of unlocking increased : now -> " + numberOfUnlocks);
        dbManager.updateUnlocks(numberOfUnlocks, todayDate);
        sendNumberOfUnlockToUI();
    }

    private static void sendNumberOfUnlockToUI() {
        Bundle bundle = gIntent.getExtras();
        if (bundle != null) {
            bundle.putInt("numberOfUnlocks", numberOfUnlocks);
            Messenger messenger = (Messenger) bundle.get("messenger");
            Message msg = Message.obtain();
            msg.setData(bundle);
            try {
                messenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "There was an error when sending the datas to main UI");
            }
        }
    }

    public static int getNumberOfUnlocks() {
        return numberOfUnlocks;
    }

    /**
     * Get the apps launched daily and they're usage time
     */
    private void getLaunchedApps() {
        //This map contains the app_name (unique), and the time in minutes
        app_data = new HashMap<String, Integer>();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            String string_date = Utils.getTodayDate();

            Log.d(TAG, "request date is " + string_date);
            long milliseconds = 0;
            SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy");
            try {
                Date d = f.parse(string_date);
                milliseconds = d.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            UsageStatsManager manager = (UsageStatsManager) getApplicationContext().getSystemService(USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            Log.d(TAG, "Request made bewteen " + milliseconds + " and " + time);
            List<UsageStats> appList = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, milliseconds, time);
            if (appList != null && appList.size() > 0) {
                for (UsageStats usageStats : appList) {
                    // usageStats.getTotalTimeInForeground() update every time app is put to onPause status.
                    // usageStats.getTotalTimeInForeground() is not a real time counter.
                    if (TimeUnit.MILLISECONDS.toMinutes(usageStats.getTotalTimeInForeground()) > 0) {
                        app_data.put(usageStats.getPackageName(), (int) TimeUnit.MILLISECONDS.toMinutes(usageStats.getTotalTimeInForeground()));
                        Log.d(TAG, "for " + usageStats.getPackageName() + " timeInMsForeground = " + usageStats.getTotalTimeInForeground());
                    }
                }
            }
            printAppDataMap(app_data);
        }
    }

    /**
     * When called, send data with time spent + app usage to main UI to refresh stats and graphs
     * @param screenTimeToSend The screen time (in minutes) to send to UI
     * @param app_data The app data (App Name + Time spent on in minutes) to send to UI
     */
    private void sendDataToMainUi(short screenTimeToSend, HashMap<String,Integer> app_data) {
        Bundle bundle = gIntent.getExtras();
        if (bundle != null) {
            bundle.putSerializable("updateStatsApps", app_data);
            bundle.putInt("updateScreenTime", screenTimeToSend);
            Messenger messenger = (Messenger) bundle.get("messenger");
            Message msg = Message.obtain();
            msg.setData(bundle);
            try {
                messenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "There was an error when sending the datas to main UI");
            }
        }
    }

    /**
     * Only for debug, print app_data Hashmap
     * @param app_data the hashmap to print. Must be a <String, Integer>.
     */
    public void printAppDataMap(HashMap<String, Integer> app_data) {
        for (HashMap.Entry<String, Integer> entry : app_data.entrySet()) {
            Log.d(TAG, "Data in HASHMAP " + entry.getKey() + ":" + entry.getValue().toString());
        }
    }

    /**
     * Main loop for the background service.
     * Launch the BackGround service and send initals datas to main UI.
     * Launch the main loop used to count every minute.
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mTimer = 0;

        Log.d(TAG, "Background service has been started");

        dbManager = new DbManager(getApplicationContext());

        todayDate = Utils.getTodayDate();

        screenTimeToday = dbManager.getScreenTime(todayDate);
        numberOfUnlocks = dbManager.getUnlocks(todayDate);

        gIntent = intent;

        // Launch Broadcast Receiver for screen time
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);

        // Check if this is the 1rst install
        if (PreferenceManager.getDefaultSharedPreferences(this).
                getString("install_date", null).equals(todayDate)
           && dbManager.getScreenTime(todayDate) == 0) {
            getLaunchedApps();

            for (HashMap.Entry<String, Integer> entry : app_data.entrySet()) {
                screenTimeToday += entry.getValue();
            }
            Log.d(TAG, "As this is the 1st day installed, the timer is at least " + screenTimeToday);
            dbManager.updateScreenTime(screenTimeToday, todayDate);
        }

        // Main loop. This loop register if the screen is on which apps are launched.
        while (true) {
            Log.d(TAG, "Service is up and running, screen is " + ScreenReceiver.isScreenOn + " mTimer vaut " + mTimer);

            //Log.d(TAG, "Running app in foreground is " + Utils.getCurrentForegroundApp(getApplicationContext()));

            try {
                if (mTimer == 60) {
                    todayDate = Utils.getTodayDate();
                    // Every minute, update DB + refresh data on main UI
                    Log.d(TAG, "Today Date = " + todayDate + " screenTimeToday = " + screenTimeToday);
                    screenTimeToday++;
                    dbManager.incrementScreenTime(todayDate);
                    dbManager.updateAppData(app_data, todayDate);
                    dbManager.updateUnlocks(numberOfUnlocks, todayDate);
                    sendDataToMainUi(screenTimeToday, app_data);
                    mTimer = 0;
                } else if (ScreenReceiver.isScreenOn) {
                    // Else, increase timer
                    getLaunchedApps();
                    mTimer++;
                }
                // This delay is just here to help synchronising.
                // TODO: Look to remove it ? I will be more accurate.
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "OnDestroy is called");
        if (dbManager != null){
            //dbManager.closeDb();
            Log.i(TAG,"mDBHelper.close() in " + this.getClass());
        }
        dbManager.updateScreenTime(screenTimeToday, todayDate);
        dbManager.updateAppData(app_data, todayDate);
        dbManager.updateUnlocks(numberOfUnlocks, todayDate);
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}

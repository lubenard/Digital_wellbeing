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

import java.text.DateFormat;
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

    private DbManager dbManager;
    private static String todayDate;
    // This variable is in seconds
    private static short mTimer = 0;
    //This variable is in Minutes
    private short screenTimeToday = 0;
    private HashMap<String, Integer> app_data;

    /**
     * Constructor of the class
     */
    public BackgroundService() {
        super("Launch");
    }

    /**
     * Get the apps launched daily and they're usage time
     */
    private void getLaunchedApps()
    {
        //This map contains the app_name (unique), and the time in minutes
         app_data = new HashMap<String, Integer>();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            String string_date = getTodayDate();

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
                        Log.d("BgService", "for " + usageStats.getPackageName() + " timeInMsForeground = " + usageStats.getTotalTimeInForeground());
                    }
                }
            }
            printAppDataMap(app_data);
        }
    }

    /**
     * When called, send data with time spent + app usage to main UI to refresh stats and graphs
     * @param intent The intent to send data to
     * @param screenTimeToSend The screen time (in minutes) to send to UI
     * @param app_data The app data (App Name + Time spent on in minutes) to send to UI
     */
    private void sendDataToMainUi(Intent intent, short screenTimeToSend, HashMap<String,Integer> app_data)
    {
        Bundle bundle = intent.getExtras();
        Bundle dataReturn = intent.getExtras();
        if (dataReturn != null) {
            dataReturn.putSerializable("updateStatsApps", app_data);
            dataReturn.putSerializable("updateStatsApps", app_data);
            dataReturn.putInt("updateScreenTime", screenTimeToSend);
            if (bundle != null) {
                Messenger messenger = (Messenger) bundle.get("updateScreenTime");
                Message msg = Message.obtain();
                msg.setData(dataReturn);
                try {
                    messenger.send(msg);
                } catch (RemoteException e) {
                    Log.i(TAG, "There was an error when sending the datas to main UI");
                }
            }
        }
    }

    /**
     * Get the today's date in formatted format
     * @return Return today's date
     */
    public static String getTodayDate() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        todayDate = dateFormat.format(date);
        return todayDate;
    }

    /**
     * Only for debug, print app_data Hashmap
     * @param app_data the hashmap to print. Must be a <String, Integer>.
     */
    public void printAppDataMap(HashMap<String, Integer> app_data)
    {
        for (HashMap.Entry<String, Integer> entry : app_data.entrySet()) {
            Log.d("BG", "Data in HASHMAP " + entry.getKey() + ":" + entry.getValue().toString());
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

        Log.d("BgService", "Background service has been started");

        dbManager = new DbManager(getApplicationContext());

        screenTimeToday = dbManager.getScreenTime(todayDate);

        // Launch Broadcast Receiver for screen time
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);

        getLaunchedApps();
        sendDataToMainUi(intent, screenTimeToday, app_data);

        // Main loop. This loop register if the screen is on which apps are launched.
        while (true)
        {
            Log.d("BgService", "Service is up and running, screen is " + ScreenReceiver.wasScreenOn + " mTimer vaut " + mTimer);
            try {
                if (mTimer == 60) {
                    // Every minute, update DB + refresh data on main UI
                    Log.d("BG", "Today Date = " + todayDate + " screenTimeToday = " + screenTimeToday);
                    screenTimeToday++;
                    dbManager.updateScreenTime(screenTimeToday, todayDate);
                    dbManager.updateAppData(app_data, todayDate);
                    sendDataToMainUi(intent, screenTimeToday, app_data);
                    mTimer = 0;
                } else if (ScreenReceiver.wasScreenOn) {
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
        if (dbManager != null){
            //dbManager.closeDb();
            Log.i(TAG,"mDBHelper.close() in " + this.getClass());
        }
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}

package com.lubenard.digital_wellbeing;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.lubenard.digital_wellbeing.custom_component.MainFragmentListview;
import com.lubenard.digital_wellbeing.settings.LicenseFragment;
import com.lubenard.digital_wellbeing.settings.SettingsFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Main UI Fragment
 */
public class MainFragment extends Fragment {

    public static final String TAG = "MainFragment";

    private PieChart mainPieChart;
    private TextView mainTextViewScreenTime;

    private int screenTimeToday;
    private String mainTextViewScreenTimeText;

    private HashMap<String, Integer> db_app_data;

    private View mainFragmentView;

    LinearLayout mainLinearlayout;

    HashMap<String, MainFragmentListview> listviewAppPkgHashMap = new HashMap<>();

    public void updateScreenTime(int addTime) {
        screenTimeToday = addTime;
        updateTextViewScreenTime();
    }

    /**
     * Update the stats on the main page
     * @param app_data app data
     */
    public void updateStats(HashMap<String, Integer> app_data) {
        if (app_data != null) {
            for (HashMap.Entry<String, Integer> entry : app_data.entrySet()) {
                Log.d(TAG, "Data in HASHMAP " + entry.getKey() + ":" + entry.getValue().toString());
            }
            updateMainChartData(app_data);
            updateListView(app_data);
        }
        else
            Log.d(TAG, "Data in HASHMAP is NULL");
    }

    /**
     * Update the Time spent text
     */
    public void updateTextViewScreenTime() {
        mainTextViewScreenTimeText = getResources().getString(R.string.main_textView_screen_time);
        mainTextViewScreenTimeText += ":\n" + screenTimeToday / 60 + "h " + screenTimeToday % 60 + "m";

        mainTextViewScreenTime.setText(mainTextViewScreenTimeText);
    }

    /**
     * Update the main Pie chart datas
     * @param app_data new app datas
     */
    private void updateMainChartData(HashMap<String, Integer> app_data) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (HashMap.Entry<String, Integer> HMdata : app_data.entrySet()) {
            // turn your data into Entry objects
            entries.add(new PieEntry(HMdata.getValue(), HMdata.getKey()));
            Log.d(TAG, "Data in HASHMAP UPDATE " + HMdata.getValue() +  " : " + HMdata.getKey());
        }

        Log.d(TAG, "Update the values.");

        //X value : name of label
        //Y value : percentage of label

        PieDataSet dataSet = new PieDataSet(entries, "Apps");

        dataSet.setSliceSpace(1f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.YELLOW);
        mainPieChart.setData(data);
        mainPieChart.invalidate();
    }

    /**
     * When the app launch, set the basic main chart
     */
    private void setupMainChart() {
        // Settings of mainDataCharts
        mainPieChart.setUsePercentValues(true);
        mainPieChart.getDescription().setEnabled(false);
        mainPieChart.setExtraOffsets(5, 0, 5, 10);
        mainPieChart.setDragDecelerationFrictionCoef(0.99f);
        mainPieChart.setDrawHoleEnabled(true);
        //mainPieChart.setHoleColor(Color.WHITE);
        mainPieChart.setTransparentCircleRadius(0);
        mainPieChart.getLegend().setEnabled(false);
        mainPieChart.setCenterText("Apps");
        mainPieChart.setCenterTextSize(40);
        mainPieChart.setRotationEnabled(false);
    }

    /**
     * Get icon from package name
     * @param packageName Example 'com.facebook.messenger'
     * @return The drawable if found, or null if not
     */
    private Drawable getIconFromPkgName(String packageName) {
        try
        {
            return getContext().getPackageManager().getApplicationIcon(packageName);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            Log.d(TAG, "icon for " + packageName + " not found");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create the 3 grey dots menu (top right)
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.main_screen_option_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    /**
     * Handle the click on the 3 grey dots menu (top right)
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                //Launch settings page
                SettingsFragment settingsFrag = new SettingsFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, settingsFrag, "findThisFragment")
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.action_licences:
                //Launch about page
                LicenseFragment aboutFrag = new LicenseFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, aboutFrag, "findThisFragment")
                        .addToBackStack(null)
                        .commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    /**
     * Update the list of app under the main chart
     * @param app_data New datas to update with.
     */
    public void updateListView(HashMap<String, Integer> app_data) {
        MainFragmentListview listViewElement;
        for (Map.Entry<String, Integer> entry : app_data.entrySet()) {
            // Check if the listview element already exist
            // If it does, no need to recreate one, only update it
            if ((listViewElement = listviewAppPkgHashMap.get(entry.getKey())) != null) {
                Log.d(TAG, "listview: Only need to update for " + entry.getKey());
                listViewElement.setApp_name(entry.getKey());
                listViewElement.setPercentage((entry.getValue() / screenTimeToday) * 100);
                listViewElement.setTimer(entry.getValue());
                listViewElement.invalidate();
            } else {
                Log.d(TAG, "listview: View needed to be created for " + entry.getKey());
                listViewElement = new MainFragmentListview(getContext());
                listviewAppPkgHashMap.put(entry.getKey(), listViewElement);
                listViewElement.setApp_name(entry.getKey());
                listViewElement.setPercentage((entry.getValue() / screenTimeToday) * 100);
                listViewElement.setTimer(entry.getValue());
                listViewElement.setIcon(getIconFromPkgName(entry.getKey()));
                mainLinearlayout.addView(listViewElement);
            }
        }
    }

    private void checkConfig() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        Boolean permanent_notification_option = sharedPreferences.getBoolean("tweaks_permanent_notification", true);
        if (permanent_notification_option)
            new NotificationsHandler().createPermanentNotification(getContext());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DbManager dbManager;
        String todayDate;

        Log.d(TAG,"View is created");

        getActivity().setTitle(R.string.app_name);

        mainFragmentView = view;
        mainLinearlayout = mainFragmentView.findViewById(R.id.main_linear_layout);
        mainPieChart = view.findViewById(R.id.main_chart);
        mainTextViewScreenTime = view.findViewById(R.id.main_textView_screnTime);

        setupMainChart();

        //Before launching charts and other graphics elements, let's check the config.
        checkConfig();

        todayDate = BackgroundService.updateTodayDate();

        dbManager = new DbManager(getContext());

        db_app_data = dbManager.getAppStats(todayDate);

        updateScreenTime(dbManager.getScreenTime(todayDate));
        updateStats(db_app_data);
        Log.d(TAG, "screenTimeToday is " + screenTimeToday);

        // Create the app listview at first
        for (Map.Entry<String, Integer> entry : db_app_data.entrySet()) {
            Log.d(TAG, entry.getKey() + " = " + entry.getValue());
            MainFragmentListview app = new MainFragmentListview(getContext());
            // Put all the element in the hashmap, avoiding recreating them
            listviewAppPkgHashMap.put(entry.getKey(), app);
            app.setApp_name(entry.getKey());
            app.setPercentage((entry.getValue() / screenTimeToday) * 100);
            app.setTimer(entry.getValue());
            app.setIcon(getIconFromPkgName(entry.getKey()));
            mainLinearlayout.addView(app);
        }

        setHasOptionsMenu(true);

        // Handle the Handler send every minute to update datas and charts
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "MESSAGE RECEIVED");
                Bundle reply = msg.getData();
                updateScreenTime(reply.getInt("updateScreenTime"));
                updateStats((HashMap<String, Integer>) reply.getSerializable("updateStatsApps"));
            }
        };

        updateTextViewScreenTime();
        getContext().startService(new Intent(MainFragment.this.getActivity(), BackgroundService.class).putExtra("updateScreenTime", new Messenger(handler)));


    }
}
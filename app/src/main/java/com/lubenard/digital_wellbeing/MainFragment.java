package com.lubenard.digital_wellbeing;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.lubenard.digital_wellbeing.Utils.Utils;
import com.lubenard.digital_wellbeing.custom_component.MainFragmentListview;
import com.lubenard.digital_wellbeing.settings.AboutFragment;
import com.lubenard.digital_wellbeing.settings.SettingsFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Main UI Fragment
 */
public class MainFragment extends Fragment {

    public static final String TAG = "MainFragment";

    LinearLayout mainLinearLayout;
    private PieChart mainPieChart;
    private TextView numberofUnlocksTextView;

    private static Intent bgService = null;
    private DbManager dbManager;
    String todayDate;

    //This variable is in minutes
    private int screenTimeToday;

    HashMap<String, MainFragmentListview> listviewAppPkgHashMap = new HashMap<>();
    private Context context;

    private int clickAboutNbr;


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
                if (entry.getKey().equals("")) {
                    app_data.remove(entry.getKey());
                    Log.d(TAG, "Sanitized data");
                }
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
    @SuppressLint("DefaultLocale")
    public void updateTextViewScreenTime() {
        mainPieChart.setCenterText(String.format("%s\n%s", context.getResources().getString(R.string.main_textView_screen_time),
                Utils.formatTimeSpent(screenTimeToday)));
    }

    /**
     * Update the main Pie chart datas
     * @param app_data new app datas
     */
    private void updateMainChartData(HashMap<String, Integer> app_data) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (HashMap.Entry<String, Integer> HMdata : app_data.entrySet()) {
            // turn your data into Entry objects
            entries.add(new PieEntry(HMdata.getValue(), Utils.getAppName(context, HMdata.getKey())));
            Log.d(TAG, "Data in HASHMAP UPDATE " + HMdata.getValue() +  " : " + HMdata.getKey());
        }

        Log.d(TAG, "Update the values on mainChart.");

        //X value : name of label
        //Y value : percentage of label

        PieDataSet dataSet = new PieDataSet(entries, "Apps");

        dataSet.setSliceSpace(1f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(10f);
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
        if (PreferenceManager.getDefaultSharedPreferences(context).getString("ui_theme", "dark").equals("white")) {
            mainPieChart.setHoleColor(Color.WHITE);
            mainPieChart.setCenterTextColor(Color.BLACK);
        } else {
            mainPieChart.setHoleColor(Color.DKGRAY);
            mainPieChart.setCenterTextColor(Color.WHITE);
        }
        mainPieChart.setEntryLabelColor(Color.BLACK);
        mainPieChart.setTransparentCircleRadius(0);
        mainPieChart.getLegend().setEnabled(false);
        mainPieChart.setCenterText(getResources().getString(R.string.main_textView_screen_time) + String.format("\n%d:%02d", screenTimeToday / 60, screenTimeToday % 60));
        mainPieChart.setCenterTextSize(35);
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("is_easter_unlocked", false))
            mainPieChart.setRotationEnabled(true);
        else
            mainPieChart.setRotationEnabled(false);
    }

    /**
     * Update the list of app under the main chart
     * @param app_data New datas to update with.
     */
    public void updateListView(HashMap<String, Integer> app_data) {
        MainFragmentListview listViewElement;
        for (final Map.Entry<String, Integer> entry : app_data.entrySet()) {
            // Check if the listview element already exist
            // If it does, no need to recreate one, only update it
            if ((listViewElement = listviewAppPkgHashMap.get(entry.getKey())) != null) {
                Log.d(TAG, "listview: Only need to update for " + entry.getKey());
                listViewElement.setApp_name(Utils.getAppName(context, entry.getKey()));
                if (screenTimeToday > 0) {
                    //Relative percentage, find a more precise way to tell ?
                    listViewElement.setPercentage(Math.round(((float) entry.getValue() / screenTimeToday) * 100));
                } else
                    listViewElement.setPercentage(0);
                listViewElement.setTimer(entry.getValue());
                listViewElement.invalidate();
            } else {
                Log.d(TAG, "listview: View needed to be created for " + entry.getKey());
                listViewElement = new MainFragmentListview(context);
                listviewAppPkgHashMap.put(entry.getKey(), listViewElement);
                listViewElement.setApp_name(Utils.getAppName(context, entry.getKey()));
                if (screenTimeToday > 0)
                    listViewElement.setPercentage(Math.round(((float) entry.getValue() / screenTimeToday) * 100));
                else
                    listViewElement.setPercentage(0);
                listViewElement.setTimer(entry.getValue());
                listViewElement.setIcon(Utils.getIconFromPkgName(context, entry.getKey()));
                listViewElement.invalidate();
                listViewElement.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AppDetail fragment = new AppDetail();
                        Bundle bundle = new Bundle();
                        bundle.putString("app_pkg", entry.getKey());
                        fragment.setArguments(bundle);
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(android.R.id.content, fragment, null)
                                .addToBackStack(null).commit();
                    }
                });
                mainLinearLayout.addView(listViewElement);
            }
        }
    }

    private void updateNumberOfUnlocksTextView(int numberOfUnlocks) {
        numberofUnlocksTextView.setText(getResources().getString(R.string.number_of_unlocks_textview) + " " + numberOfUnlocks);
    }

    public static void setBgService(Intent newBgService) {
        bgService = newBgService;
    }

    private void getFreshDatas() {
        updateScreenTime(dbManager.getScreenTime(todayDate));
        updateStats(dbManager.getAppStats(todayDate));
        updateNumberOfUnlocksTextView(BackgroundService.getNumberOfUnlocks());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG,"View is created");

        getActivity().setTitle(R.string.app_name);

        mainLinearLayout = view.findViewById(R.id.main_linear_layout);
        mainPieChart = view.findViewById(R.id.main_chart);
        numberofUnlocksTextView = view.findViewById(R.id.numberOfUnlocksTextView);

        setupMainChart();

        todayDate = Utils.getTodayDate();

        dbManager = new DbManager(context);

        Log.d(TAG, "screenTimeToday is " + screenTimeToday);

        setHasOptionsMenu(true);

        // Handle the Handler send every minute to update datas and charts
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "MESSAGE RECEIVED");
                Bundle reply = msg.getData();
                if (reply.containsKey("updateScreenTime"))
                    updateScreenTime(reply.getInt("updateScreenTime"));
                if (reply.containsKey("updateStatsApps"))
                    updateStats((HashMap<String, Integer>) reply.getSerializable("updateStatsApps"));
                if (reply.containsKey("numberOfUnlocks"))
                    updateNumberOfUnlocksTextView(reply.getInt("numberOfUnlocks"));
            }
        };

        // No need to relaunch a service if it has already been started by AutoStart
        if (bgService == null) {
            setBgService(new Intent(MainFragment.this.getActivity(), BackgroundService.class));
            bgService.putExtra("messenger", new Messenger(handler));
        }
        context.startService(bgService);

        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("is_easter_unlocked", false)) {
            mainPieChart.setRotationEnabled(true);
        }
    }

    private void clearListView() {
        Log.d(TAG, "listview is cleared");
        for (final Map.Entry<String, MainFragmentListview> entry : listviewAppPkgHashMap.entrySet()) {
            mainLinearLayout.removeView(entry.getValue());
        }
        listviewAppPkgHashMap.clear();
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
            case R.id.action_refresh:
                getFreshDatas();
                return true;
            case R.id.action_settings:
                //Launch settings page
                SettingsFragment settingsFrag = new SettingsFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, settingsFrag, "findThisFragment")
                        .addToBackStack(null).commit();
                return true;
            case R.id.action_about:
                clickAboutNbr++;
                Log.d(TAG, "Click about: " + clickAboutNbr);
                //Launch about page
                AboutFragment aboutFrag = new AboutFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, aboutFrag, "findThisFragment")
                        .addToBackStack(null).commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        clearListView();
        getFreshDatas();
        if (clickAboutNbr >= 5 && !sharedPreferences.getBoolean("is_easter_unlocked", false)) {
            sharedPreferences.edit().putBoolean("is_easter_unlocked", true).apply();
            Toast.makeText(context, context.getResources().getText(R.string.easter_discovered), Toast.LENGTH_SHORT).show();
            mainPieChart.setRotationEnabled(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        // Used to fix the bug while changing theme.
        // Changing theme restarted the Activity and getContext returned null.
        // This fix this issue
        context = ctx;
    }
}
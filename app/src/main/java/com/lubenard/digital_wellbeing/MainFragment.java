package com.lubenard.digital_wellbeing;

import androidx.fragment.app.Fragment;

import android.content.Intent;
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
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.lubenard.digital_wellbeing.settings.LicenseFragment;
import com.lubenard.digital_wellbeing.settings.SettingsFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainFragment extends Fragment {

    public static final String TAG = "MainFragment";

    private PieChart mainPieChart;
    private TextView mainTextViewScreenTime;

    private int screenTimeToday;
    private String mainTextViewScreenTimeText;

    public void updateScreenTime(int addTime) {
        screenTimeToday = addTime;
        updateTextViewScreenTime();
    }

    public void updateStats(HashMap<String, Integer> app_data)
    {
        if (app_data != null) {
            for (HashMap.Entry<String, Integer> entry : app_data.entrySet()) {
                Log.d(TAG, "Data in HASHMAP " + entry.getKey() + ":" + entry.getValue().toString());
            }
            updateMainChartData(app_data);
        }
        else
            Log.d(TAG, "Data in HASHMAP is NULL");
    }

    public void updateTextViewScreenTime()
    {
        mainTextViewScreenTimeText = getResources().getString(R.string.main_textView_screen_time);
        mainTextViewScreenTimeText += ":\n" + screenTimeToday / 60 + "h " + screenTimeToday % 60 + "m";

        mainTextViewScreenTime.setText(mainTextViewScreenTimeText);
    }

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

    private void setupMainChart(){

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.main_screen_option_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

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
        return inflater.inflate(R.layout.activity_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbManager dbManager;
        String todayDate;

        getActivity().setTitle(R.string.app_name);

        mainPieChart = view.findViewById(R.id.main_chart);
        mainTextViewScreenTime = view.findViewById(R.id.main_textView_screnTime);

        setupMainChart();

        todayDate = BackgroundService.updateTodayDate();

        Log.d(TAG,"View is recreated");

        dbManager = new dbManager(getContext());
        updateScreenTime(dbManager.getScreenTime(todayDate));
        updateStats(dbManager.getAppStats(todayDate));
        Log.d(TAG, "screenTimeToday is " + screenTimeToday);

        setHasOptionsMenu(true);

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
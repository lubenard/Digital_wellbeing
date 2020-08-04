package com.lubenard.digital_wellbeing;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;
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

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private PieChart mainPieChart;
    private TextView mainTextViewScreenTime;

    private int screenTimeToday;
    private String mainTextViewScreenTimeText;

    public void updateScreenTime(int addTime) {
        screenTimeToday = addTime;
        updateTextViewScreenTime();
    }

    private void getLaunchedApps()
    {
        screenTimeToday = 0;

        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfo = am.getRunningAppProcesses();

        for (int i = 0; i < runningAppProcessInfo.size(); i++) {
            Log.d("Running Apps", "This app is running " + runningAppProcessInfo.get(i).processName);
            //app_data_array.add(new App_data(runningAppProcessInfo.get(i).processName));
            /*if(runningAppProcessInfo.get(i).processName.equals("com.the.app.you.are.looking.for") {
                Log.d("");
            }*/
        }

    }

    public void updateTextViewScreenTime()
    {
        mainTextViewScreenTimeText = getResources().getString(R.string.main_textView_screen_time);
        mainTextViewScreenTimeText += ":\n" + screenTimeToday / 60 + "h " + screenTimeToday % 60 + "m";

        mainTextViewScreenTime.setText(mainTextViewScreenTimeText);
    }

    private void setupMainChart(){

        //X value : name of label
        //Y value : percentage of label

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

        /*yValues.add(new PieEntry(34f, "Twitter"));
        yValues.add(new PieEntry(23f, "Facebook"));
        yValues.add(new PieEntry(14f, "Snapchat"));
        yValues.add(new PieEntry(35, "Instagram"));
        yValues.add(new PieEntry(40, "Camera"));
        yValues.add(new PieEntry(23, "Gmail"));*/

        ArrayList<PieEntry> entries = new ArrayList<>();
        /*for (App_data data : app_data_array) {
            // turn your data into Entry objects
            entries.add(new PieEntry(data.getPercentage(), data.getName()));
        }*/

        PieDataSet dataSet = new PieDataSet(entries, "Apps");

        dataSet.setSliceSpace(1f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.YELLOW);
        mainPieChart.setData(data);
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
                SettingsFragment nextFrag = new SettingsFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, nextFrag, "findThisFragment")
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
        int mode;

        AppOpsManager appOps = (AppOpsManager) getContext().getSystemService(Context.APP_OPS_SERVICE);

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getContext().getPackageName());
        } else {
            mode = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getContext().getPackageName());
        }

        if (mode != AppOpsManager.MODE_ALLOWED) {
            // Check for permissions
            // TODO; handle permissions correctly
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }

        setHasOptionsMenu(true);

        mainPieChart = view.findViewById(R.id.main_chart);
        mainTextViewScreenTime = view.findViewById(R.id.main_textView_screnTime);

        List<Entry> entries = new ArrayList<Entry>();

        getLaunchedApps();

        Handler handler = new Handler() {
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(Message msg) {
                Log.d("MainFragment", "MESSAGE RECEIVED");
                Bundle reply = msg.getData();
                updateScreenTime(reply.getInt("updateScreenTime"));
            }
        };

        updateTextViewScreenTime();
        getContext().startService(new Intent(MainFragment.this.getActivity(), BackgroundService.class).putExtra("updateScreenTime", new Messenger(handler)));

        setupMainChart();
    }
}
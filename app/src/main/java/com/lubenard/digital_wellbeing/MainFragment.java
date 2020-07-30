package com.lubenard.digital_wellbeing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.Chart;
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

    private String[] launchedApps;

    private void getLaunchedApps()
    {
        /*ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfo = am.getRunningAppProcesses();*/

        launchedApps = new String[]{"com.twitter.twitter", "com.test.test", "com.abc.abc"};

        /*for (int i = 0; i < runningAppProcessInfo.size(); i++) {

            Log.d("Running Apps", "This app is running " + runningAppProcessInfo.get(i).processName);
            /*if(runningAppProcessInfo.get(i).processName.equals("com.the.app.you.are.looking.for") {
                Log.d("");
            }
        }*/

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

        ArrayList<PieEntry> yValues = new ArrayList<>();

        yValues.add(new PieEntry(34f, "Twitter"));
        yValues.add(new PieEntry(23f, "Facebook"));
        yValues.add(new PieEntry(14f, "Snapchat"));
        yValues.add(new PieEntry(35, "Instagram"));
        yValues.add(new PieEntry(40, "Camera"));
        yValues.add(new PieEntry(23, "Gmail"));

        PieDataSet dataSet = new PieDataSet(yValues, "Apps");

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.activity_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

         mainPieChart = view.findViewById(R.id.main_chart);

        List<Entry> entries = new ArrayList<Entry>();

        getLaunchedApps();

        setupMainChart();
    }
}
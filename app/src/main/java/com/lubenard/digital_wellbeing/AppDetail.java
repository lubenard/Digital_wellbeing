package com.lubenard.digital_wellbeing;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.lubenard.digital_wellbeing.Utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class AppDetail extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.details_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BarChart chart = view.findViewById(R.id.details_chart);

        Bundle bundle = this.getArguments();
        String app_pkg = bundle.getString("app_pkg", null);


        ArrayList<BarEntry> values = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<String>();
        BarDataSet dataSet;


        getActivity().setTitle(getResources().getString(R.string.details_fragment_title) + " " + Utils.getAppName(getContext(), app_pkg));

        //((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (app_pkg != null) {
            ((TextView)view.findViewById(R.id.details_app_name)).setText(Utils.getAppName(getContext(), app_pkg));
            ((TextView)view.findViewById(R.id.details_app_pkg)).setText(app_pkg);
            ((ImageView)view.findViewById(R.id.details_app_icon)).setImageDrawable(Utils.getIconFromPkgName(getContext(), app_pkg));
        }

        // I am not a big fan of recreating a Object for each Fragment.
        // Maybe make DBManager class functions static ?
        DbManager dbManager = new DbManager(getContext());

        //Not a big fan of calling each time getTodayDate.
        // Find a way to make this a global variable instead ?
        LinkedHashMap<String, Integer> app_data = dbManager.getDetailsForApp(app_pkg, 7, false);

        int counter = 0;

        for (HashMap.Entry<String, Integer> oneElemDatas : app_data.entrySet()) {
            // turn your data into Entry objects
            values.add(new BarEntry(counter, oneElemDatas.getValue()));
            labels.add(oneElemDatas.getKey());
            Log.d("AppDetails", "Data in HASHMAP UPDATE " + counter + ":" + oneElemDatas.getValue() +  " @" + oneElemDatas.getKey());
            counter++;
        }

        XAxis xAxis = chart.getXAxis();

        // Set customs labels instead of their index
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        // Set the label position to bottom
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Design the chart
        // Disable the Zoom by pinching
        chart.setScaleEnabled(false);
        //Disable the vertical grid
        xAxis.setDrawGridLines(false);
        //Disable the horizontal grid
        chart.getAxisLeft().setDrawGridLines(false);
        //Disable the horizontal legend grid
        chart.getAxisRight().setEnabled(false);
        //Disable description
        chart.getDescription().setEnabled(false);
        //Disable Text legend
        chart.getLegend().setEnabled(false);

        dataSet = new BarDataSet(values, "Datas");

        //Fix the bug for labels being not aligned
        chart.getXAxis().setLabelCount(dataSet.getEntryCount());

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setBarWidth(0.9f);

        chart.setData(data);
    }

}

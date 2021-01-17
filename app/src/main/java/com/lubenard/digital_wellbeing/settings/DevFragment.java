package com.lubenard.digital_wellbeing.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import androidx.fragment.app.Fragment;

import com.lubenard.digital_wellbeing.DbManager;
import com.lubenard.digital_wellbeing.R;
import com.lubenard.digital_wellbeing.Utils.Utils;

public class DevFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.dev_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button add1Mn = view.findViewById(R.id.dev_add_1_mn);
        Button rm1Mn = view.findViewById(R.id.dev_rm_1_mn);

        final String todayDate = Utils.getTodayDate();
        final DbManager dbManager = new DbManager(getContext());

        add1Mn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbManager.updateScreenTime(dbManager.getScreenTime(todayDate) + 1, todayDate);
                Log.d("DevSettings", "New ScreenTime is now " + dbManager.getScreenTime(todayDate));
                Toast.makeText(getContext(), "New ScreenTime is now " + dbManager.getScreenTime(todayDate), Toast.LENGTH_SHORT).show();
            }
        });

        rm1Mn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbManager.updateScreenTime(dbManager.getScreenTime(todayDate) - 1, todayDate);
                Log.d("DevSettings", "New ScreenTime is now " + dbManager.getScreenTime(todayDate));
                Toast.makeText(getContext(), "New ScreenTime is now " + dbManager.getScreenTime(todayDate), Toast.LENGTH_SHORT).show();
            }
        });

    }
}

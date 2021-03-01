package com.lubenard.digital_wellbeing.settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.fragment.app.Fragment;

import com.lubenard.digital_wellbeing.DbManager;
import com.lubenard.digital_wellbeing.NotificationsHandler;
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
        final EditText customMn = view.findViewById(R.id.dev_custom_mn);

        Button add1Unlock = view.findViewById(R.id.dev_add_1_unlock);
        Button rm1Unlock = view.findViewById(R.id.dev_rm_1_unlock);

        Button sendNormalNotif = view.findViewById(R.id.dev_send_test_notif);

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
                int newScreenTime = dbManager.getScreenTime(todayDate) - 1;
                if (newScreenTime < 0)
                    newScreenTime = 0;
                dbManager.updateScreenTime(newScreenTime, todayDate);
                Log.d("DevSettings", "New ScreenTime is now " + newScreenTime);
                Toast.makeText(getContext(), "New ScreenTime is now " + newScreenTime, Toast.LENGTH_SHORT).show();
            }
        });

        customMn.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keycode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP && keycode == KeyEvent.KEYCODE_ENTER) {
                    if (Integer.parseInt(customMn.getText().toString()) <= 1440) {
                        dbManager.updateScreenTime(Integer.parseInt(customMn.getText().toString()), todayDate);
                        Log.d("DevSettings", "New ScreenTime is now " + customMn.getText().toString());
                        Toast.makeText(getContext(), "New ScreenTime is now " + customMn.getText().toString(), Toast.LENGTH_SHORT).show();
                    } else {
                        customMn.getText().replace(0, 4, "1440");
                    }
                }
                return false;
            }
        });

        add1Unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newUnlockNumber = dbManager.getUnlocks(todayDate) + 1;
                dbManager.updateUnlocks(newUnlockNumber, todayDate);
                Log.d("DevSettings", "Unlocks are now " + newUnlockNumber);
                Toast.makeText(getContext(), "Unlocks are now " + newUnlockNumber, Toast.LENGTH_SHORT).show();
            }
        });

        rm1Unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newUnlockNumber = dbManager.getUnlocks(todayDate) - 1;
                if (newUnlockNumber < 0)
                    newUnlockNumber = 0;
                dbManager.updateScreenTime(newUnlockNumber, todayDate);
                Log.d("DevSettings", "Unlocks are now " + newUnlockNumber);
                Toast.makeText(getContext(), "Unlocks are now " + newUnlockNumber, Toast.LENGTH_SHORT).show();
            }
        });

        sendNormalNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new NotificationsHandler().sendNormalNotification(getContext(), "Test Notif", "This is a test notif!",
                        R.drawable.baseline_settings_white_48);
            }
        });


    }
}

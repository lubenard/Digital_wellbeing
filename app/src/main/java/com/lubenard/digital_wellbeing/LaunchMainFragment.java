package com.lubenard.digital_wellbeing;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class LaunchMainFragment extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        int mode;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Check for permissions at app start
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);

        //Handling different android permissions version
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        } else {
            mode = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        }

        if (mode != AppOpsManager.MODE_ALLOWED) {
            // If permissions not granted
            handlePermissions fragment = new handlePermissions();
            fragmentTransaction.replace(android.R.id.content, fragment);
        } else {
            // If permission is granted
            MainFragment fragment = new MainFragment();
            fragmentTransaction.replace(android.R.id.content, fragment);
        }
        fragmentTransaction.commit();
    }
}

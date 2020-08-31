package com.lubenard.digital_wellbeing;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * Decide whether or not the app should launch the main Fragment or the permission Fragment
 */
public class LaunchMainFragment extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (handlePermissions.checkIfUsagePermissionGranted(this)) {
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

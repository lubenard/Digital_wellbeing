package com.lubenard.digital_wellbeing.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.lubenard.digital_wellbeing.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_fragment, rootKey);
    }
}

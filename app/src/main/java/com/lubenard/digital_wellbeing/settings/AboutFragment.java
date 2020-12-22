package com.lubenard.digital_wellbeing.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.lubenard.digital_wellbeing.R;

/**
 * About Fragment found under 'about'
 */
public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.about_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(R.string.main_screen_option_menu_about);

        Button licenseButton = view.findViewById(R.id.licenceButton);
        licenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LicenseFragment aboutFrag = new LicenseFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, aboutFrag, "findThisFragment")
                        .addToBackStack(null).commit();
            }
        });
    }
}
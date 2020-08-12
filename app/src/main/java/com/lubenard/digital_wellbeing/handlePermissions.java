package com.lubenard.digital_wellbeing;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class handlePermissions extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.handle_permissions, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*int mode;

        AppOpsManager appOps = (AppOpsManager) getContext().getSystemService(Context.APP_OPS_SERVICE);

        //Handling different android permissions version
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getContext().getPackageName());
        } else {
            mode = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getContext().getPackageName());
        }

        if (mode == AppOpsManager.MODE_ALLOWED) {
            LicenseFragment aboutFrag = new LicenseFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, aboutFrag, "findThisFragment")
                    .addToBackStack(null)
                    .commit();
        }*/

        Button permissionButton = view.findViewById(R.id.permission_button);

        permissionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }
        });
    }
}

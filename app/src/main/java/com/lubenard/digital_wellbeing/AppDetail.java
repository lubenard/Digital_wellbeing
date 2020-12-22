package com.lubenard.digital_wellbeing;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

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

        getActivity().setTitle(R.string.details_fragment_title);

        //getActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = this.getArguments();
        String app_pkg = bundle.getString("app_pkg", null);
        if (app_pkg != null) {
            ((TextView)view.findViewById(R.id.details_app_name)).setText(Utils.getAppName(getContext(), app_pkg));
            ((TextView)view.findViewById(R.id.details_app_pkg)).setText(app_pkg);
            ((ImageView)view.findViewById(R.id.details_app_icon)).setImageDrawable(Utils.getIconFromPkgName(getContext(), app_pkg));
        }
    }

}

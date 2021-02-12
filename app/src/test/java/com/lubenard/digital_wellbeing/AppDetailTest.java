package com.lubenard.digital_wellbeing;

import android.os.Bundle;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AppDetailTest {

    @Test
    public void checkAppInstalledTextView() {
        AppDetail fragment = new AppDetail();

        Bundle bundle = new Bundle();
        bundle.putString("app_pkg", "com.idonotexist.fake");
        fragment.setArguments(bundle);

        //TODO: SHould test with Espresso for fragment scenario

        //System.out.println(fragment.getView().findViewById(R.id.details_app_installed).getVisibility());

    }
}

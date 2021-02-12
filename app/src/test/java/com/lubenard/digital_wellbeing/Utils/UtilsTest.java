package com.lubenard.digital_wellbeing.Utils;

import android.graphics.drawable.Drawable;

import androidx.test.platform.app.InstrumentationRegistry;

import com.lubenard.digital_wellbeing.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class UtilsTest {

    @Test
    public void getAppNameNotInstalled() {
        String app_name = Utils.getAppName(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                "com.idonotexist.fake");

        Assert.assertEquals("com.idonotexist.fake", app_name);
    }

    @Test
    public void getAppNameInstalled() {
        String app_name = Utils.getAppName(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                "com.lubenard.digital_wellbeing");

        Assert.assertEquals(app_name, "Digital WellBeing");
    }
}


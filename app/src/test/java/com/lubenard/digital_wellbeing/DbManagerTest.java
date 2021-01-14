package com.lubenard.digital_wellbeing;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(RobolectricTestRunner.class)
public class DbManagerTest {

    private DbManager dbTest;

    @Before
    public void setup() {
        dbTest = new DbManager(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @Test
    public void addMinuteToDB() {
        dbTest.updateUnlocks(1, Utils.getTodayDate());

        Assert.assertEquals(1, dbTest.getUnlocks(Utils.getTodayDate()));
    }

    @Test
    public void addThenRemoveMinuteToDB() {
        dbTest.updateUnlocks(1, Utils.getTodayDate());
        dbTest.updateUnlocks(0, Utils.getTodayDate());

        Assert.assertEquals(0, dbTest.getUnlocks(Utils.getTodayDate()));
    }
}
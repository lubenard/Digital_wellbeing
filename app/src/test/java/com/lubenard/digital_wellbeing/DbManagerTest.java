package com.lubenard.digital_wellbeing;

import androidx.test.platform.app.InstrumentationRegistry;

import com.lubenard.digital_wellbeing.Utils.Utils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.HashMap;
import java.util.LinkedHashMap;

@RunWith(RobolectricTestRunner.class)
public class DbManagerTest {

    private DbManager dbTest;

    @Before
    public void setup() {
        dbTest = new DbManager(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @After
    public void closeDB() {
        dbTest.closeDb();
    }

    @Test
    public void addUnlockDB() {
        dbTest.updateUnlocks(1, Utils.getTodayDate());

        Assert.assertEquals(1, dbTest.getUnlocks(Utils.getTodayDate()));
    }

    @Test
    public void addThenRemoveUnlock() {
        dbTest.updateUnlocks(1, Utils.getTodayDate());
        dbTest.updateUnlocks(0, Utils.getTodayDate());

        Assert.assertEquals(0, dbTest.getUnlocks(Utils.getTodayDate()));
    }

    @Test
    public void unlockDatasWithWrongDate() {
        dbTest.updateUnlocks(1, Utils.getTodayDate());

        Assert.assertEquals(0, dbTest.getUnlocks("10-01-1963"));
    }

    @Test
    public void addScreenTime() {
        dbTest.updateScreenTime(1, Utils.getTodayDate());

        Assert.assertEquals(1, dbTest.getScreenTime(Utils.getTodayDate()));
    }

    @Test
    public void incrementScreenTime() {
        dbTest.updateScreenTime(10, Utils.getTodayDate());

        Assert.assertEquals(10, dbTest.getScreenTime(Utils.getTodayDate()));

        dbTest.incrementScreenTime(Utils.getTodayDate());
        Assert.assertEquals(11, dbTest.getScreenTime(Utils.getTodayDate()));
    }

    @Test
    public void addThenRemoveScreenTime() {
        dbTest.updateScreenTime(1, Utils.getTodayDate());
        dbTest.updateScreenTime(0, Utils.getTodayDate());

        Assert.assertEquals(0, dbTest.getScreenTime(Utils.getTodayDate()));
    }

    @Test
    public void screenTimeWithWrongDate() {
        dbTest.updateScreenTime(1, Utils.getTodayDate());

        Assert.assertEquals(0, dbTest.getScreenTime("10-01-1963"));
    }

    @Test
    public void addOneAppData() {
        HashMap<String, Integer> app_data = new HashMap();
        app_data.put("com.test.test1", 15);

        dbTest.updateAppData(app_data, Utils.getTodayDate());
        HashMap<String, Integer> data_returned = dbTest.getAppStats(Utils.getTodayDate());

        Assert.assertEquals(true, data_returned.containsKey("com.test.test1"));
        Assert.assertEquals((Integer)15, data_returned.get("com.test.test1"));
    }

    @Test
    public void addOneAppDataThenUpdate() {
        HashMap<String, Integer> app_data = new HashMap();
        app_data.put("com.test.test1", 15);

        dbTest.updateAppData(app_data, Utils.getTodayDate());

        app_data.clear();
        app_data.put("com.test.test1", 27);
        dbTest.updateAppData(app_data, Utils.getTodayDate());

        HashMap<String, Integer> data_returned = dbTest.getAppStats(Utils.getTodayDate());

        Assert.assertEquals(true, data_returned.containsKey("com.test.test1"));
        Assert.assertEquals((Integer) 27, data_returned.get("com.test.test1"));
    }

    @Test
    public void addOneAppDataWithWrongDate() {
        HashMap<String, Integer> app_data = new HashMap();
        app_data.put("com.test.test1", 15);

        dbTest.updateAppData(app_data, Utils.getTodayDate());
        HashMap<String, Integer> data_returned = dbTest.getAppStats("10-01-1963");

        Assert.assertEquals(false, data_returned.containsKey("com.test.test1"));
        Assert.assertEquals(false, data_returned.containsValue(15));
    }

    @Test
    public void addMultipleAppData() {
        HashMap<String, Integer> app_data = new HashMap();
        app_data.put("com.test.test1", 15);
        app_data.put("com.test.test2", 3);
        app_data.put("com.test.test3", 1000);
        app_data.put("com.test.test4", 600);

        dbTest.updateAppData(app_data, Utils.getTodayDate());
        HashMap<String, Integer> data_returned = dbTest.getAppStats(Utils.getTodayDate());

        Assert.assertEquals(true, data_returned.containsKey("com.test.test1"));
        Assert.assertEquals((Integer)15, data_returned.get("com.test.test1"));

        Assert.assertEquals(true, data_returned.containsKey("com.test.test2"));
        Assert.assertEquals((Integer)3, data_returned.get("com.test.test2"));

        Assert.assertEquals(true, data_returned.containsKey("com.test.test3"));
        Assert.assertEquals((Integer)1000, data_returned.get("com.test.test3"));

        Assert.assertEquals(true, data_returned.containsKey("com.test.test4"));
        Assert.assertEquals((Integer)600, data_returned.get("com.test.test4"));
    }

    @Test
    public void addOneAppDataWithMultipleDates() {
        HashMap<String, Integer> app_data = new HashMap();

        app_data.put("com.test.test1", 15);
        dbTest.updateAppData(app_data, "17-02-2020");

        app_data.clear();
        app_data.put("com.test.test1", 27);
        dbTest.updateAppData(app_data, "18-02-2020");

        app_data.clear();
        app_data.put("com.test.test1", 1);
        dbTest.updateAppData(app_data, "19-02-2020");


        LinkedHashMap<String, Integer> data_returned =
                dbTest.getDetailsForApp("com.test.test1", 0, true);

        Assert.assertEquals(true, data_returned.containsKey("17-02-2020"));
        Assert.assertEquals((Integer)15, data_returned.get("17-02-2020"));

        Assert.assertEquals(true, data_returned.containsKey("18-02-2020"));
        Assert.assertEquals((Integer)27, data_returned.get("18-02-2020"));

        Assert.assertEquals(true, data_returned.containsKey("19-02-2020"));
        Assert.assertEquals((Integer)1, data_returned.get("19-02-2020"));


        //System.out.println(data_returned.entrySet().iterator().next());
        //Assert.assertEquals(data_returned.entrySet().iterator().next());

        /*for (LinkedHashMap.Entry<String, Integer> entry : data_returned.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }*/
    }
}
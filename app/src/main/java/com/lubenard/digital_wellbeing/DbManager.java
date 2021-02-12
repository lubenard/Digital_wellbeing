package com.lubenard.digital_wellbeing;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.lubenard.digital_wellbeing.Utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Handle the DB used to save datas.
 * Theses datas are sent to Musk in order to build Skynet.
 */
public class DbManager extends SQLiteOpenHelper {

    public static final String TAG = "DBManager";

    static final String dbName = "dataDB";

    // screen time table
    static final String screenTimeTable = "screenTime";
    static final String screenTimeTableDate = "date";
    static final String screenTimeTableScreenTime = "screenTime";

    // number of unlock table
    static final String unlockTable = "unlocks";
    static final String unlockTableDate = "date";
    static final String unlockTableUnlockNbr = "unlockNbr";

    // appTime table
    static final String appTimeTable = "appTime";
    static final String appTimeTableDate = "date";
    static final String appTimeTableTimeSpent = "timeSpent";
    static final String appTimeTableAppId = "appId";

    // apps table
    static final String appsTable = "apps";
    static final String appsTableId = "id";
    static final String appsTablePkgName = "appPkgName";
    static final String appsTableName = "appName";

    // Join from appTime and apps tables
    static final String viewAppsTables = "ViewAppsTables";

    private Context context;

    private SQLiteDatabase readableDB;
    private SQLiteDatabase writableDB;

    public DbManager(Context context) {
        super(context, dbName , null,1);
        this.context = context;
        this.readableDB = this.getReadableDatabase();
        this.writableDB = this.getWritableDatabase();
    }

    /**
     * If the db does not exist, create it with thoses fields.
     * @param db The database Object
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create screenTime table
        db.execSQL("CREATE TABLE " + screenTimeTable + " (" + screenTimeTableDate + " DATE PRIMARY KEY, " + screenTimeTableScreenTime + " INTEGER)");

        db.execSQL("CREATE TABLE " + unlockTable + " (" + unlockTableDate + " DATE PRIMARY KEY, " + unlockTableUnlockNbr + " INTEGER)");

        // Create appTime table
        db.execSQL("CREATE TABLE " + appTimeTable + " (" + appTimeTableAppId + " INTEGER, " + appTimeTableDate + " DATE, " + appTimeTableTimeSpent + " INTEGER, " +
                "PRIMARY KEY(" + appTimeTableAppId + ", " +appTimeTableDate + "))");

        // Create apps table
        db.execSQL("CREATE TABLE " + appsTable + " (" + appsTableId + " INTEGER PRIMARY KEY AUTOINCREMENT, " + appsTablePkgName + " TEXT, " + appsTableName + " TEXT)");

        // Create view joining appTime and apps tables
        db.execSQL("CREATE VIEW " + viewAppsTables + " AS SELECT " +
                appsTable + "." + appsTablePkgName + ", " +
                appsTable + "." + appsTableName + ", " +
                appTimeTable + "." + appTimeTableTimeSpent + ", " +
                appTimeTable + "." + appTimeTableDate + "" +
                " FROM " + appTimeTable + " JOIN " + appsTable +
                " ON " + appTimeTable + "." + appTimeTableAppId + " = " + appsTable + "." + appsTableId
        );

        Log.d(TAG, "The db has been created, this message should only appear once.");
    }

    public static String getDBName() {
        return dbName;
    }

    /**
     * If you plan to improve the database, you might want to use this function as a automated
     * upgrade tool for db.
     * @param sqLiteDatabase
     * @param i Old DB version
     * @param i1 New DB version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * Create a app Entry only if non existent:
     * Example: you just installed a app, which is not added in the db yet
     */
    //TODO: Might need to recode with insertWithOnConflict
    private void createAppRow(String date, String appPkgName) {
        String[] columns = new String[]{appsTablePkgName};
        Cursor c = readableDB.query(appsTable, columns, appsTablePkgName + "=?",
                new String[]{appPkgName}, null, null, null);

        if (c.getCount() == 0) {
            Log.d(TAG, "AppData: I create the entry for " + appPkgName);
            ContentValues cv = new ContentValues();
            cv.put(appsTablePkgName, appPkgName);
            cv.put(appsTableName, Utils.getAppName(context, appPkgName));
            writableDB.insert(appsTable, null, cv);
        } else
            Log.d(TAG, "AppData: The entry for " + appPkgName + " already seems to exist");
    }

    /**
     * Get the screen time for a specific date
     * @param date date to which you want to get the datas
     * @return The screen time fetched from the DB
     */
    public short getScreenTime(String date) {
        Log.d(TAG, "getScreenTime request made for date: " + date);
        short value = 0;

        String [] columns = new String[]{screenTimeTableScreenTime};
        Cursor c = readableDB.query(screenTimeTable, columns, screenTimeTableDate + "=?",
                new String[]{date}, null, null, null);

        if (c.moveToFirst())
            value = c.getShort(0);
        c.close();

        return value;
    }

    /**
     * get The screen time for all days, inserted into a LinkedHashMap
     * @return the screenTime for all days
     */
    public LinkedHashMap<String, Integer> getAllScreenTime() {
        Log.d(TAG, "getAllScreenTime request");
        LinkedHashMap<String, Integer> app_datas = new LinkedHashMap<>();

        String [] columns = new String[]{screenTimeTableDate, screenTimeTableScreenTime};
        Cursor cursor = readableDB.query(screenTimeTable, columns, null, null, null, null, screenTimeTableDate + " DESC");

        while (cursor.moveToNext()) {
            app_datas.put(cursor.getString(cursor.getColumnIndex(screenTimeTableDate)), cursor.getInt(cursor.getColumnIndex(screenTimeTableScreenTime)));
            Log.d(TAG, "getAllScreenTime adding " + cursor.getString(cursor.getColumnIndex(screenTimeTableDate)) + " and value " + cursor.getInt(cursor.getColumnIndex(screenTimeTableScreenTime)));
        }
        cursor.close();
        return app_datas;
    }

    /**
     * Create a Screen Time only if non existent:
     * Example: Past midnight, this is a new day, no entry exist in the db for that day
     * @param date date to which you want to get the datas
     */
    public void incrementScreenTime(String date) {
        ContentValues cv = new ContentValues();
        cv.put(screenTimeTableScreenTime, getScreenTime(date) + 1);

        Log.d(TAG, "incrementScreenTime: increment for date = " + date);

        int u = writableDB.update(screenTimeTable, cv, screenTimeTableDate + "=?", new String []{date});
        if (u == 0) {
            Log.d(TAG, "updateScreenTime: increment does not seems to work, insert data for date = " + date);
            cv.put(screenTimeTableDate, date);
            writableDB.insertWithOnConflict(screenTimeTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    /**
     * Create a Screen Time only if non existent:
     * Example: Past midnight, this is a new day, no entry exist in the db for that day
     * @param newScreenTime new screen time
     * @param date date to which you want to get the datas
     */
    public void updateScreenTime(int newScreenTime, String date) {
        ContentValues cv = new ContentValues();
        cv.put(screenTimeTableScreenTime, newScreenTime);

        Log.d(TAG, "updateScreenTime: update with new value (time = " + newScreenTime + ") for date = " + date);
        int u = writableDB.update(screenTimeTable, cv, screenTimeTableDate + "=?", new String []{date});
        if (u == 0) {
            Log.d(TAG, "updateScreenTime: update does not seems to work, insert data: (time = " + newScreenTime + ") for date = " + date);
            cv.put(screenTimeTableDate, date);
            writableDB.insertWithOnConflict(screenTimeTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    /**
     * Get the number of unlocks for a specific date
     * @param date date to which you want to get the datas
     * @return The number of u(TextView)nlocks is fetched from the DB
     */
    public short getUnlocks(String date) {
        Log.d(TAG, "getUnlocks request made for date: " + date);
        short value = 0;

        String [] columns = new String[]{unlockTableUnlockNbr};
        Cursor c = readableDB.query(unlockTable, columns, unlockTableDate + "=?",
                new String[]{date}, null, null, null);

        if (c.moveToFirst())
            value = c.getShort(0);
        c.close();

        return value;
    }

    /**
     * Update the number of unlocks in the db for the given date
     * @param newUnlockNbr new unlock number
     * @param date which date to insert ?
     */
    public void updateUnlocks(int newUnlockNbr, String date) {
        ContentValues cv = new ContentValues();
        cv.put(unlockTableUnlockNbr, newUnlockNbr);

        Log.d(TAG, "updateScreenTime: update with new value (time = " + newUnlockNbr + ") for date = " + date);
        int u = writableDB.update(unlockTable, cv, unlockTableDate + "=?", new String []{date});
        if (u == 0) {
            Log.d(TAG, "unlockNbr: update does not seems to work, insert data: (time = " + newUnlockNbr + ") for date = " + date);
            cv.put(unlockTableDate, date);
            writableDB.insertWithOnConflict(unlockTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    /**
     * get The unlock number for all days, inserted into a LinkedHashMap
     * @return all the unlocks for all days
     */
    public LinkedHashMap<String, Integer> getAllUnclocks() {
        Log.d(TAG, "getAllUnlocks request");
        LinkedHashMap<String, Integer> app_datas = new LinkedHashMap<>();

        String [] columns = new String[]{unlockTableDate, unlockTableUnlockNbr};
        Cursor cursor = readableDB.query(unlockTable, columns, null, null, null, null, unlockTableDate + " DESC");

        while (cursor.moveToNext()) {
            app_datas.put(cursor.getString(cursor.getColumnIndex(unlockTableDate)), cursor.getInt(cursor.getColumnIndex(unlockTableUnlockNbr)));
            Log.d(TAG, "getAllUnlocks adding " + cursor.getString(cursor.getColumnIndex(unlockTableDate)) + " and value " + cursor.getInt(cursor.getColumnIndex(unlockTableUnlockNbr)));
        }
        cursor.close();
        return app_datas;
    }

    /**
     * Get the app datas for a specific date
     * @param date date to which you want to get the datas
     * @return The datas fetched from the DB
     */
    public HashMap<String, Integer> getAppStats(String date) {
        Log.d(TAG, "getAppStats request made for date: " + date);
        HashMap<String, Integer> app_data = new HashMap<>();

        String [] columns = new String[]{appsTablePkgName, appTimeTableTimeSpent};
        Cursor cursor = readableDB.query(viewAppsTables, columns, appTimeTableDate + "=?",
                new String[]{date}, null, null, null);

        while (cursor.moveToNext()) {
            app_data.put(cursor.getString(cursor.getColumnIndex(appsTablePkgName)), cursor.getInt(cursor.getColumnIndex(appTimeTableTimeSpent)));
            Log.d(TAG, "getStatApp adding " + cursor.getString(cursor.getColumnIndex(appsTablePkgName)) + " and value " + cursor.getInt(cursor.getColumnIndex(appTimeTableTimeSpent)));
        }
        cursor.close();
        return app_data;
    }

    /**
     * Update the app data in the db
     * @param app_data Contains the data of the apps to update
     * @param date Set at which date insert data
     */
    public void updateAppData(HashMap<String, Integer> app_data, String date) {
        for (HashMap.Entry<String, Integer> entry : app_data.entrySet()) {
            ContentValues cv = new ContentValues();

            createAppRow(date, entry.getKey());

            Log.d(TAG, "updateAppData: Data in HASHMAP " + entry.getKey() + ":" + entry.getValue().toString());
            cv.put(appTimeTableTimeSpent, entry.getValue());

            Log.d(TAG, "updateScreenTime: update with new value (timeSpent = " + entry.getValue()+ "IdFromPkgName = " + getIdFromPkgName(entry.getKey()) + ") for date = " + date);

            int u = writableDB.update(appTimeTable, cv, appTimeTableDate + "=? AND " + appTimeTableAppId + "=?", new String []{date, String.valueOf(getIdFromPkgName(entry.getKey()))});
            if (u == 0) {
                Log.d(TAG, "updateAppData: update does not seems to work, insert data: (timeSpent = " + entry.getValue()+ "IdFromPkgName = " + getIdFromPkgName(entry.getKey()) + ") for date = " + date);
                cv.put(appTimeTableDate, date);
                cv.put(appTimeTableAppId, getIdFromPkgName(entry.getKey()));
                writableDB.insertWithOnConflict(appTimeTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            }
        }
    }

    /**
     * Get the id from the appsTable table with specified app package
     * @param pkgName Package name
     * @return The id of that package name in the table
     */
    public int getIdFromPkgName(String pkgName) {
        int value;
        String[] columns = new String[]{appsTableId};
        Cursor c = readableDB.query(appsTable, columns, appsTablePkgName + "=?",
                new String[]{pkgName}, null, null, null);
        if (c.moveToFirst())
            value = c.getInt(0);
        else
            value = -1;
        return value;
    }

    /**
     * Get listed apps
     * @return a String array containing all apps in the db
     */
    public ArrayList<String> getAllApps() {
        ArrayList<String> arrayOfApps = new ArrayList<String>();

        Log.d(TAG, "getAllApps request made for all apps");

        String[] columns = new String[]{appsTablePkgName};
        Cursor cursor = readableDB.query(appsTable, columns,null,
                null, null, null, null);

        while (cursor.moveToNext()) {
            arrayOfApps.add(cursor.getString(cursor.getColumnIndex(appsTablePkgName)));
            Log.d(TAG, "getAllApps getting " + cursor.getString(cursor.getColumnIndex(appsTablePkgName)));
        }
        cursor.close();

        return arrayOfApps;
    }

    /**
     * Get the datas for Details page.
     * LinkedHashMap is used instead of HashMap because it remember the order
     * in which items are put in
     * @param pkgName Name of package. Example: "com.lubenard.digital_wellbeing"
     * @param limit Limit of result to return. Default is 4 (Why 4 ? Because of MpAndroidCharts -> Dates descriptions are too close to
     *              each other if there is more than 5 datas)
     * @param allDatas Bypass limit option. if this option is set to true, all datas will be returned
     * @return A hashmap containing date and corresponding datas
     */
    public LinkedHashMap<String, Integer> getDetailsForApp(String pkgName, int limit, boolean allDatas) {
        //SELECT date, timeSpent FROM ViewAppsTables WHERE appPkgName="com.lubenard.digital_wellbeing" ORDER BY date DESC LIMIT 7
        Log.d(TAG, "getDetailsForApp request made for app " + pkgName);
        LinkedHashMap<String, Integer> app_datas = new LinkedHashMap<>();

        String [] columns = new String[]{appTimeTableDate, appTimeTableTimeSpent};
        Cursor cursor = readableDB.query(viewAppsTables, columns, appsTablePkgName + "=?",
                new String[]{pkgName}, null, null, appTimeTableDate + " DESC",
                (allDatas) ? null : String.valueOf(limit));

        while (cursor.moveToNext()) {
            app_datas.put(cursor.getString(cursor.getColumnIndex(appTimeTableDate)), cursor.getInt(cursor.getColumnIndex(appTimeTableTimeSpent)));
            Log.d(TAG, "getDetailsForApp adding " + cursor.getString(cursor.getColumnIndex(appTimeTableDate)) + " and value " + cursor.getInt(cursor.getColumnIndex(appTimeTableTimeSpent)));
        }
        cursor.close();
        return app_datas;
    }

    /**
     * DEBUG FUNCTION
     * Use this function for testing. Print all the content of a given table
     * @param tableName Table name to print
     */
    public void getTableAsString(String tableName) {
        Log.d(TAG, "getTableAsString called for " + tableName);
        Log.d(TAG, String.format("Table %s:\n", tableName));
        Cursor allRows  = readableDB.rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    Log.d(TAG, String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name))));
                }
                Log.d(TAG,"\n");

            } while (allRows.moveToNext());
        }
    }

    /**
     * Close the db when finished using it.
     */
    public void closeDb() {
        if (writableDB != null) { writableDB.close();}
        if (readableDB != null) { readableDB.close();}
    }
}

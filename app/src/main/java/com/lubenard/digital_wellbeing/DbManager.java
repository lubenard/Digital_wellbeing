package com.lubenard.digital_wellbeing;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

/**
 * Handle the DB used to save datas.
 * Theses datas are sent to Musk in order to build Skynet.
 */
public class DbManager extends SQLiteOpenHelper {

    public static final String TAG = "DB";

    static final String dbName = "dataDB";

    // screen time table
    static final String screenTimeTable = "screenTime";
    static final String screenTimeTableDate = "date";
    static final String screenTimeTableScreenTime = "screenTime";

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

        Log.d("DB", "The db has been created, this message should only appear once.");
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
     * Transform package name to App name
     * For example: 'com.facebook.messenger' -> 'Messenger'
     * @param pkgName The package name you want
     * @return The name of the app
     */
    public String getAppNameFromPkgName(String pkgName) {
        final PackageManager pm = context.getPackageManager();
        String appName;
        try {
            appName = (String) pm.getApplicationLabel(pm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA));
        } catch (final PackageManager.NameNotFoundException e) {
            appName = "";
        }
        return appName;
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
            cv.put(appsTableName, getAppNameFromPkgName(appPkgName));
            writableDB.insert(appsTable, null, cv);
        } else
            Log.d(TAG, "AppData: The entry for " + appPkgName + " already seems to exist");
    }

    /**
     * Create a Screen Time only if non existent:
     * Example: Past midnight, this is a new day, no entry exist in the db for that day
     */
    public void updateScreenTime(int addTime, String date) {
        ContentValues cv = new ContentValues();
        cv.put(screenTimeTableScreenTime, addTime);

        Log.d(TAG, "updateScreenTime: update with new value (time = " + addTime + ") for date = " + date);
        int u = writableDB.update(screenTimeTable, cv, screenTimeTableDate + "=?", new String []{date});
        if (u == 0) {
            Log.d(TAG, "updateScreenTime: update does not seems to work, insert data: (time = " + addTime + ") for date = " + date);
            cv.put(screenTimeTableDate, date);
            writableDB.insertWithOnConflict(screenTimeTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    /**
     * Get the id from the appsTable table with specified app package
     * @param pkgName Package name
     * @return The id of that package name in the table
     */
    public int getIdFromPkgName(String pkgName)
    {
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
     * Update the app data in the db
     * @param app_data Contains the data of the apps to update
     * @param date Set at which date insert data
     */
    public void updateAppData(HashMap<String, Integer> app_data, String date) {

        String[] columns = new String[]{appTimeTableDate, appTimeTableAppId};

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
     * Use this function for testing. Print all the content of a given table
     * @param tableName Table name to print
     */
    public void getTableAsString(String tableName) {
        Log.d("DB", "getTableAsString called for " + tableName);
        Log.d("DB", String.format("Table %s:\n", tableName));
        Cursor allRows  = readableDB.rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    Log.d("DB", String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name))));
                }
                Log.d("DB","\n");

            } while (allRows.moveToNext());
        }
    }

    /**
     * Get the app datas for a specific date
     * @param date date to which you want to get the datas
     * @return The datas fetched from the DB
     */
    public HashMap<String, Integer> getAppStats(String date) {
        HashMap<String, Integer> app_data = new HashMap<>();

        String [] columns = new String[]{appsTablePkgName, appTimeTableTimeSpent};
        Cursor cursor = readableDB.query(viewAppsTables, columns, appTimeTableDate + "=?",
                new String[]{date}, null, null, null);

        while (cursor.moveToNext()) {
            app_data.put(cursor.getString(cursor.getColumnIndex(appsTablePkgName)), cursor.getInt(cursor.getColumnIndex(appTimeTableTimeSpent)));
            Log.d("DB", "getStatApp adding " + cursor.getString(cursor.getColumnIndex(appsTablePkgName)) + " and value " + cursor.getInt(cursor.getColumnIndex(appTimeTableTimeSpent)));
        }
        cursor.close();
        getTableAsString(appsTable);
        getTableAsString(appTimeTable);
        return app_data;
    }

    /**
     * Get the screen time for a specific date
     * @param date date to which you want to get the datas
     * @return The screen time fetched from the DB
     */
    public short getScreenTime(String date) {
        short value = 0;

        String [] columns = new String[]{screenTimeTableScreenTime};
        Cursor c = readableDB.query(screenTimeTable, columns, screenTimeTableDate + "=?",
                new String[]{date}, null, null, null);

        Log.d("DB", "Cursor is " + c.moveToFirst() + " date is " + date);

        if (c.moveToFirst())
            value = c.getShort(0);
        else
            getTableAsString(screenTimeTable);
        c.close();

        return value;
    }

    /**
     * Close the db when finished using it.
     */
    public void closeDb() {
        if (writableDB != null) { writableDB.close();}
        if (readableDB != null) { readableDB.close();}
    }
}

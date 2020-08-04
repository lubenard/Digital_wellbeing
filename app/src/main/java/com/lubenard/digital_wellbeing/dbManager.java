package com.lubenard.digital_wellbeing;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class dbManager extends SQLiteOpenHelper {

    static final String dbName = "dataDB";

    static final String screenTimeTable = "screenTime";
    static final String screenTimeTableDate = "date";
    static final String screenTimeTableScreenTimeData = "screenTimeData";

    static final String appNameTable = "appName";
    static final String appNameTableName = "name";

    static final String appScreenTimeTable = "appData";
    static final String appScreenTimeTableDate = "date";
    static final String appScreenTimeTableAppTime = "appTime";

    public dbManager(Context context) {
        super(context, dbName , null,33);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create screen time table
        db.execSQL("CREATE TABLE " + screenTimeTable + " (" + screenTimeTableDate + " DATE , " + screenTimeTableScreenTimeData + " INTEGER)");

        // Create appName table
        db.execSQL("CREATE TABLE " + appNameTable + " (" + appNameTableName + " TEXT)");

        // Create appScreenTime table
        db.execSQL("CREATE TABLE " + appScreenTimeTable + " (" + appScreenTimeTableDate + " DATE, " + appScreenTimeTableAppTime + "INTEGER)");

        Log.d("DB", "The db has been created, this message should only appear once.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    // Create a empty column only if there is none existing
    private void createScreenTimeDate(String date) {
        SQLiteDatabase readableDb = this.getReadableDatabase();
        String[] columns = new String[]{screenTimeTableDate};
        Cursor c = readableDb.query(screenTimeTable, columns, screenTimeTableDate + "=?",
                new String[]{date}, null, null, null);

        if (c.getCount() == 0) {
            BackgroundService.startNewDay();
            Log.d("DB", "I create the row for " + date);
            readableDb.close();
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(screenTimeTableDate, date);
            cv.put(screenTimeTableScreenTimeData, 1);
            db.insert(screenTimeTable, null, cv);
            getTableAsString(screenTimeTable);
            db.close();
        } else
            Log.d("DB", "The row for " + date + " already seems to exist");

        readableDb.close();
    }

    public void updateScreenTime(int addTime, String date) {
        createScreenTimeDate(date);

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(screenTimeTableScreenTimeData, addTime);
        db.update(screenTimeTable, cv, screenTimeTableDate + "=?", new String []{date});

        db.close();
    }

    public void getTableAsString(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d("DB", "getTableAsString called");
        Log.d("DB", String.format("Table %s:\n", tableName));
        Cursor allRows  = db.rawQuery("SELECT * FROM " + tableName, null);
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

    public short getScreenTime(String date) {
        short value = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        String [] columns = new String[]{screenTimeTableScreenTimeData};
        Cursor c = db.query(screenTimeTable, columns, screenTimeTableDate + "=?",
                new String[]{date}, null, null, null);

        Log.d("DB", "Cursor is " + c.moveToFirst() + " date is " + date);

        if (c.moveToFirst())
            value = c.getShort(0);
        else
            getTableAsString(screenTimeTable);
        c.close();

        Log.d("DB", "value is " + value);
        return value;
    }
}

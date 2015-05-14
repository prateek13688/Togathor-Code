package com.uf.togathor.db.localdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.uf.togathor.Togathor;

/**
 * Created by Alok on 3/31/2015.
 */
public class EventDB extends SQLiteOpenHelper {

    public SQLiteDatabase localDB;

    public static final String TABLE_PREFIX = "events";
    private static final String DATABASE_NAME = "events.db";
    private static final int DATABASE_VERSION = 1;
    public static final String COLUMN_UUID = "message_uuid";
    public static final String COLUMN_DATA = "message_data";
    public static final String COLUMN_TIMESTAMP = "message_timestamp";

    // Database creation sql statement
    private static final String DATABASE_CREATE =  "(" + COLUMN_UUID + " text not null, "
            + COLUMN_DATA + " text not null, "
            + COLUMN_TIMESTAMP + " text not null);";

    public EventDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        localDB = db;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ChatDB.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREFIX);
        onCreate(db);
    }

    public String createTable(String tableName)  {
        if(localDB == null)
            localDB = getWritableDatabase();
        localDB.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PREFIX +
                Togathor.getPreferences().getUserId() + "_" + tableName + DATABASE_CREATE);
        return (TABLE_PREFIX + Togathor.getPreferences().getUserId() + "_" + tableName);
    }

}

package com.uf.togathor.db.localdb;

/**
 * Created by alok on 1/4/15.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.uf.togathor.Togathor;

public class ChatDB extends SQLiteOpenHelper {

    public SQLiteDatabase localDB;

    public static final String TABLE_PREFIX = "messages";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_MESSAGE_TYPE = "message_type";
    public static final String COLUMN_MESSAGE_FROM_USERID = "message_from_user_id";
    public static final String COLUMN_MESSAGE_TO_USERID = "message_to_user_id";
    public static final String COLUMN_MESSAGE_TO_GROUPID = "message_to_group_id";
    public static final String COLUMN_MESSAGE_FROM_USER_NAME = "message_from_user_name";
    public static final String COLUMN_MESSAGE_TO_USER_NAME = "message_to_user_name";
    public static final String COLUMN_MESSAGE_TO_GROUP_NAME = "message_to_group_name";
    public static final String COLUMN_MESSAGE_VOICE_FILE_ID = "message_voice_file_id";
    public static final String COLUMN_MESSAGE_VIDEO_FILE_ID = "message_video_file_id";
    public static final String COLUMN_MESSAGE_LOCAL_IMG_FILE_ID = "message_local_img_file_id";
    public static final String COLUMN_MESSAGE_LOCAL_IMG_THUMB_FILE_ID = "message_local_img_thumb_file_id";
    public static final String COLUMN_MESSAGE_IMG_FILE_ID = "message_img_file_id";
    public static final String COLUMN_MESSAGE_IMG_THUMB_FILE_ID = "message_img_thumb_file_id";
    public static final String COLUMN_MESSAGE_LATITUDE = "message_latitude";
    public static final String COLUMN_MESSAGE_LONGITUDE = "message_longitude";
    public static final String COLUMN_MESSAGE_LOCAL_VIDEO_FILE_ID = "message_local_video_file_id";
    public static final String COLUMN_MESSAGE_LOCAL_VOICE_FILE_ID = "message_local_voice_file_id";
    public static final String COLUMN_CREATED = "message_created";

    private static final String DATABASE_NAME = "messages.db";
    private static final int DATABASE_VERSION = 2;

    // Database creation sql statement
    private static final String DATABASE_CREATE =  "(" + COLUMN_ID
            + " text not null, " + COLUMN_MESSAGE
            + " text, " + COLUMN_MESSAGE_TYPE + " text not null, " + COLUMN_MESSAGE_FROM_USERID
            + " text not null, " + COLUMN_MESSAGE_FROM_USER_NAME + " text not null, " + COLUMN_MESSAGE_TO_USERID
            + " text, " + COLUMN_MESSAGE_TO_USER_NAME + " text, " + COLUMN_MESSAGE_TO_GROUPID
            + " text, " + COLUMN_MESSAGE_TO_GROUP_NAME + " text, " + COLUMN_MESSAGE_LOCAL_IMG_FILE_ID
            + " text, " + COLUMN_MESSAGE_LOCAL_IMG_THUMB_FILE_ID + " text, " + COLUMN_MESSAGE_VIDEO_FILE_ID
            + " text, " + COLUMN_MESSAGE_VOICE_FILE_ID + " text, " + COLUMN_MESSAGE_LATITUDE
            + " text, " + COLUMN_MESSAGE_LONGITUDE + " text, " + COLUMN_MESSAGE_IMG_FILE_ID
            + " text, " + COLUMN_MESSAGE_IMG_THUMB_FILE_ID + " text, " + COLUMN_MESSAGE_LOCAL_VIDEO_FILE_ID
            + " text, " + COLUMN_MESSAGE_LOCAL_VOICE_FILE_ID + " text, " + COLUMN_CREATED + " text);";

    public ChatDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        localDB = database;
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
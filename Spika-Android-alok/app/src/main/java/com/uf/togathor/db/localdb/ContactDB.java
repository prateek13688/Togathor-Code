package com.uf.togathor.db.localdb;

/**
 * Created by alok on 1/4/15.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.uf.togathor.Togathor;

public class ContactDB extends SQLiteOpenHelper {

    public SQLiteDatabase localDB;

    public static final String USER_TABLE_PREFIX = "users";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_REV = "rev";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LAST_LOGIN = "last_login";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_BIRTHDAY = "birthday";
    public static final String COLUMN_ABOUT = "about";
    public static final String COLUMN_ANDROID_PUSH_TOKEN = "android_push_token";
    public static final String COLUMN_TOKEN = "token";
    public static final String COLUMN_TOKEN_TIMESTAMP = "token_timestamp";
    public static final String COLUMN_ONLINE_STATUS = "online_status";
    public static final String COLUMN_AVATAR_FILE_ID = "avatar_file_id";
    public static final String COLUMN_MAX_CONTACT_COUNT = "max_contact_count";
    public static final String COLUMN_MAX_FAVORITE_COUNT = "max_favorite_count";
    public static final String COLUMN_AVATAR_THUMB_FILE_ID = "avatar_thumb_file_id";

    public static final String GROUP_TABLE_PREFIX = "groups";
    public static final String COLUMN_KEY = "key";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_CATEGORY_ID = "category_id";
    public static final String COLUMN_CATEGORY_NAME = "category_name";
    public static final String COLUMN_DELETED = "deleted";

    private static final String DATABASE_NAME = "contacts.db";
    private static final int DATABASE_VERSION = 2;

    // Database creation sql statement
    private static final String USER_DATABASE_CREATE = "(" + COLUMN_ID + " text not null, "
            + COLUMN_REV + " text, "
            + COLUMN_EMAIL + " text, "
            + COLUMN_PASSWORD + " text, "
            + COLUMN_TYPE + " text, "
            + COLUMN_NAME + " text, "
            + COLUMN_LAST_LOGIN + " text, "
            + COLUMN_GENDER + " text, "
            + COLUMN_BIRTHDAY + " text, "
            + COLUMN_ABOUT + " text, "
            + COLUMN_ANDROID_PUSH_TOKEN + " text, "
            + COLUMN_TOKEN + " text, "
            + COLUMN_TOKEN_TIMESTAMP + " text, "
            + COLUMN_ONLINE_STATUS + " text, "
            + COLUMN_AVATAR_FILE_ID + " text, "
            + COLUMN_MAX_CONTACT_COUNT + " text, "
            + COLUMN_MAX_FAVORITE_COUNT + " text, "
            + COLUMN_AVATAR_THUMB_FILE_ID + " text);";

    private static final String GROUP_DATABASE_CREATE = "(" + COLUMN_ID + " text not null, "
            + COLUMN_REV + " text, "
            + COLUMN_KEY + " text, "
            + COLUMN_TYPE + " text, "
            + COLUMN_NAME + " text, "
            + COLUMN_PASSWORD + " text, "
            + COLUMN_USER_ID + " text, "
            + COLUMN_DESCRIPTION + " text, "
            + COLUMN_AVATAR_FILE_ID + " text, "
            + COLUMN_CATEGORY_ID + " text, "
            + COLUMN_CATEGORY_NAME + " text, "
            + COLUMN_AVATAR_THUMB_FILE_ID + " text);";

    public ContactDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        localDB = database;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ContactDB.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_PREFIX);
        db.execSQL("DROP TABLE IF EXISTS " + GROUP_TABLE_PREFIX);
        onCreate(db);
    }

    public String createUserTable() {

        if(localDB == null)
            localDB = getWritableDatabase();

        localDB.execSQL("CREATE TABLE IF NOT EXISTS " + USER_TABLE_PREFIX +
                Togathor.getPreferences().getUserId() + USER_DATABASE_CREATE);
        return USER_TABLE_PREFIX +
                Togathor.getPreferences().getUserId();
    }

    public String createGroupTable() {

        if(localDB == null)
            localDB = getWritableDatabase();

        localDB.execSQL("CREATE TABLE IF NOT EXISTS " + GROUP_TABLE_PREFIX +
                Togathor.getPreferences().getUserId() + GROUP_DATABASE_CREATE);
        return GROUP_TABLE_PREFIX +
                Togathor.getPreferences().getUserId();
    }
}
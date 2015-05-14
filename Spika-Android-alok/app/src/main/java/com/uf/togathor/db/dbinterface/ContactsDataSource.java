package com.uf.togathor.db.dbinterface;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.db.couchdb.model.User;
import com.uf.togathor.db.localdb.ContactDB;
import com.uf.togathor.management.SyncModule;

import java.util.HashMap;

/**
 * Created by alok on 1/4/15.
 */
public class ContactsDataSource {

    private boolean open;
    private Context activity;
    private SQLiteDatabase database;
    private ContactDB dbHelper;
    private String[] allUserColumns = {ContactDB.COLUMN_ID,
            ContactDB.COLUMN_REV, ContactDB.COLUMN_EMAIL,
            ContactDB.COLUMN_PASSWORD, ContactDB.COLUMN_TYPE,
            ContactDB.COLUMN_NAME, ContactDB.COLUMN_LAST_LOGIN,
            ContactDB.COLUMN_GENDER, ContactDB.COLUMN_BIRTHDAY,
            ContactDB.COLUMN_ABOUT, ContactDB.COLUMN_ANDROID_PUSH_TOKEN,
            ContactDB.COLUMN_TOKEN, ContactDB.COLUMN_TOKEN_TIMESTAMP,
            ContactDB.COLUMN_ONLINE_STATUS, ContactDB.COLUMN_AVATAR_FILE_ID,
            ContactDB.COLUMN_MAX_CONTACT_COUNT, ContactDB.COLUMN_MAX_FAVORITE_COUNT,
            ContactDB.COLUMN_AVATAR_THUMB_FILE_ID};

    private String[] allGroupColumns = {ContactDB.COLUMN_ID,
            ContactDB.COLUMN_REV, ContactDB.COLUMN_KEY,
            ContactDB.COLUMN_TYPE, ContactDB.COLUMN_NAME,
            ContactDB.COLUMN_PASSWORD, ContactDB.COLUMN_USER_ID,
            ContactDB.COLUMN_DESCRIPTION, ContactDB.COLUMN_AVATAR_FILE_ID,
            ContactDB.COLUMN_CATEGORY_ID, ContactDB.COLUMN_CATEGORY_NAME,
            ContactDB.COLUMN_AVATAR_THUMB_FILE_ID};

    public ContactsDataSource(Context context) {
        dbHelper = new ContactDB(context);
        this.activity = context;
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
        open = true;
    }

    public void close() {
        open = false;
    }

    public void finish() {
        dbHelper.close();
    }

    public boolean isOpen() {
        return open;
    }

    public void insertUser(User contact) {
        ContentValues values = userToValue(contact);
        long insertId = database.insert(dbHelper.createUserTable(), null,
                values);
    }

    public void insertGroup(Group contact) {
        ContentValues values = groupToValue(contact);
        long insertId = database.insert(dbHelper.createGroupTable(), null,
                values);
    }

    public void replaceUser(User contact) {
        String id = contact.getId() + "";
        database.replace(dbHelper.createUserTable(), ContactDB.COLUMN_ID
                + " = " + id, userToValue(contact));
    }

    public void replaceGroup(Group contact) {
        String id = contact.getId() + "";
        database.replace(dbHelper.createGroupTable(), ContactDB.COLUMN_ID
                + " = " + id, groupToValue(contact));
    }

    public void deleteUser(User contact) {
        String id = contact.getId() + "";
        database.delete(dbHelper.createUserTable(), ContactDB.COLUMN_ID
                + " = " + id, null);
    }

    public void deleteGroup(Group contact) {
        String id = contact.getId() + "";
        database.delete(dbHelper.createGroupTable(), ContactDB.COLUMN_ID
                + " = " + id, null);
    }

    public HashMap<String, User> getAllUserContacts() {
        HashMap<String, User> users = new HashMap<>();

        Cursor cursor;
        cursor = database.query(dbHelper.createUserTable(),
                allUserColumns, null, null, null, null, null);

        if (cursor.getCount() <= 0) {
            for (User user : SyncModule.getAllUserContactsFromServer())
                insertUser(user);
            cursor = database.query(dbHelper.createUserTable(),
                    allUserColumns, null, null, null, null, null);
        }

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            User user = cursorToUser(cursor);
            users.put(user.getId(), user);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return users;
    }

    public HashMap<String, Group> getAllUserGroups() {
        HashMap<String, Group> groups = new HashMap<>();

        Cursor cursor;
        cursor = database.query(dbHelper.createGroupTable(),
                allGroupColumns, null, null, null, null, null);

        if (cursor.getCount() <= 0)
            for (Group group : SyncModule.getAllUserGroupsFromServer())
                insertGroup(group);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Group group = cursorToGroup(cursor);
            groups.put(group.getId(), group);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return groups;
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getString(0));
        user.setRev(cursor.getString(1));
        user.setEmail(cursor.getString(2));
        user.setPassword(cursor.getString(3));
        user.setType(cursor.getString(4));
        user.setName(cursor.getString(5));
        user.setLastLogin(cursor.getLong(6));
        user.setGender(cursor.getString(7));
        user.setBirthday(cursor.getLong(8));
        user.setAbout(cursor.getString(9));
        user.setAndroidToken(cursor.getString(10));
        user.setToken(cursor.getString(11));
        user.setTokenTimestamp(cursor.getLong(12));
        user.setOnlineStatus(cursor.getString(13));
        user.setAvatarFileId(cursor.getString(14));
        user.setMaxContactCount(cursor.getInt(15));
        user.setMaxFavoriteCount(cursor.getInt(16));
        user.setAvatarThumbFileId(cursor.getString(17));
        return user;
    }

    private ContentValues userToValue(User user) {
        ContentValues values = new ContentValues();
        values.put(ContactDB.COLUMN_ID, user.getId());
        values.put(ContactDB.COLUMN_REV, user.getRev());
        values.put(ContactDB.COLUMN_EMAIL, user.getEmail());
        values.put(ContactDB.COLUMN_PASSWORD, user.getPassword());
        values.put(ContactDB.COLUMN_TYPE, user.getType());
        values.put(ContactDB.COLUMN_NAME, user.getName());
        values.put(ContactDB.COLUMN_LAST_LOGIN, user.getLastLogin());
        values.put(ContactDB.COLUMN_GENDER, user.getGender());
        values.put(ContactDB.COLUMN_BIRTHDAY, user.getBirthday());
        values.put(ContactDB.COLUMN_ABOUT, user.getAbout());
        values.put(ContactDB.COLUMN_ANDROID_PUSH_TOKEN, user.getAndroidToken());
        values.put(ContactDB.COLUMN_TOKEN, user.getToken());
        values.put(ContactDB.COLUMN_TOKEN_TIMESTAMP, user.getTokenTimestamp());
        values.put(ContactDB.COLUMN_ONLINE_STATUS, user.getOnlineStatus());
        values.put(ContactDB.COLUMN_AVATAR_FILE_ID, user.getAvatarFileId());
        values.put(ContactDB.COLUMN_MAX_CONTACT_COUNT, user.getMaxContactCount());
        values.put(ContactDB.COLUMN_MAX_FAVORITE_COUNT, user.getMaxFavoriteCount());
        values.put(ContactDB.COLUMN_AVATAR_THUMB_FILE_ID, user.getAvatarThumbFileId());
        return values;
    }

    private Group cursorToGroup(Cursor cursor) {
        Group group = new Group();
        group.setId(cursor.getString(0));
        group.setRev(cursor.getString(1));
        group.setKey(cursor.getString(2));
        group.setType(cursor.getString(3));
        group.setName(cursor.getString(4));
        group.setPassword(cursor.getString(5));
        group.setUserId(cursor.getString(6));
        group.setDescription(cursor.getString(7));
        group.setAvatarFileId(cursor.getString(8));
        group.setCategoryId(cursor.getString(9));
        group.setCategoryName(cursor.getString(10));
        group.setDeleted(false);
        group.setAvatarThumbFileId(cursor.getString(11));
        return group;
    }

    private ContentValues groupToValue(Group group) {
        ContentValues values = new ContentValues();
        values.put(ContactDB.COLUMN_ID, group.getId());
        values.put(ContactDB.COLUMN_REV, group.getRev());
        values.put(ContactDB.COLUMN_KEY, group.getKey());
        values.put(ContactDB.COLUMN_TYPE, group.getType());
        values.put(ContactDB.COLUMN_NAME, group.getName());
        values.put(ContactDB.COLUMN_PASSWORD, group.getPassword());
        values.put(ContactDB.COLUMN_USER_ID, group.getUserId());
        values.put(ContactDB.COLUMN_DESCRIPTION, group.getDescription());
        values.put(ContactDB.COLUMN_AVATAR_FILE_ID, group.getAvatarFileId());
        values.put(ContactDB.COLUMN_CATEGORY_ID, group.getCategoryId());
        values.put(ContactDB.COLUMN_CATEGORY_NAME, group.getCategoryName());
        values.put(ContactDB.COLUMN_AVATAR_THUMB_FILE_ID, group.getAvatarThumbFileId());
        return values;
    }
}

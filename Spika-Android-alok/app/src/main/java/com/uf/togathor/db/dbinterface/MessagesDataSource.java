package com.uf.togathor.db.dbinterface;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.db.couchdb.model.User;
import com.uf.togathor.db.localdb.ChatDB;
import com.uf.togathor.management.SyncModule;
import com.uf.togathor.model.Message;
import com.uf.togathor.utils.constants.Const;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by alok on 1/4/15.
 */
public class MessagesDataSource {

    private boolean open;
    private Context activity;
    private SQLiteDatabase database;
    private ChatDB dbHelper;
    private String[] allColumns = {ChatDB.COLUMN_ID,
            ChatDB.COLUMN_MESSAGE, ChatDB.COLUMN_MESSAGE_TYPE,
            ChatDB.COLUMN_MESSAGE_FROM_USERID, ChatDB.COLUMN_MESSAGE_FROM_USER_NAME,
            ChatDB.COLUMN_MESSAGE_TO_USERID, ChatDB.COLUMN_MESSAGE_TO_USER_NAME,
            ChatDB.COLUMN_MESSAGE_TO_GROUPID, ChatDB.COLUMN_MESSAGE_TO_GROUP_NAME,
            ChatDB.COLUMN_MESSAGE_LOCAL_IMG_FILE_ID, ChatDB.COLUMN_MESSAGE_LOCAL_IMG_THUMB_FILE_ID,
            ChatDB.COLUMN_MESSAGE_VIDEO_FILE_ID, ChatDB.COLUMN_MESSAGE_VOICE_FILE_ID,
            ChatDB.COLUMN_MESSAGE_LATITUDE, ChatDB.COLUMN_MESSAGE_LONGITUDE, ChatDB.COLUMN_MESSAGE_IMG_FILE_ID,
            ChatDB.COLUMN_MESSAGE_IMG_THUMB_FILE_ID, ChatDB.COLUMN_MESSAGE_LOCAL_VIDEO_FILE_ID,
            ChatDB.COLUMN_MESSAGE_LOCAL_VOICE_FILE_ID, ChatDB.COLUMN_CREATED};

    public MessagesDataSource(Context context) {
        dbHelper = new ChatDB(context);
        this.activity = context;
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
        open = true;
    }

    public void close() {
        open = false;
    }

    public void finish()    {
        dbHelper.close();
    }

    public boolean isOpen() {
        return open;
    }

    public void insertMessage(Message message, User toUser) {
        ContentValues values = messageToValue(message);
        String rowId = null;
        Cursor cursor = database.query(dbHelper.createTable(toUser.getId()),
                allColumns, null, null, null, null, null);
        if(cursor.getCount() > Const.DB_MAX_MESSAGE_LIMIT)
        {
            if(cursor.moveToFirst())
                rowId = cursor.getString(cursor.getColumnIndex(ChatDB.COLUMN_ID));
            int rows  = database.delete(dbHelper.createTable(toUser.getId()), ChatDB.COLUMN_ID +  "=?", new String[]{rowId});
            Log.d("MessageDataSource", "The Number of Rows Deleted in insertMessages for one user" + rows);
        }
        long insertId = database.insert(dbHelper.createTable(toUser.getId()), null,
                values);
    }

    public void insertMessage(Message message, Group toGroup) {
        ContentValues values = messageToValue(message);
        String rowId = null;
        int rows = 0;
        Cursor cursor = database.query(dbHelper.createTable(toGroup.getId()),
                allColumns, null, null, null, null, null);
        if(cursor.getCount() > Const.DB_MAX_MESSAGE_LIMIT)
        {
            if(cursor.moveToFirst())
                cursor.getString(cursor.getColumnIndex(ChatDB.COLUMN_ID));
            rows  = database.delete(dbHelper.createTable(toGroup.getId()), ChatDB.COLUMN_ID +  "=?", new String[]{rowId});
            Log.d("MessageDataSource", "The Number of Rows Deleted in insertMessages for Group" + rows);
        }
        long insertId = database.insert(dbHelper.createTable(toGroup.getId()), null,
                values);
    }

    public void replaceMessage(Message message, User toUser) {
        String id = message.getCreated() + "";
        if (toUser != null)
            database.replace(dbHelper.createTable(toUser.getId()), ChatDB.COLUMN_ID
                    + " = " + id, messageToValue(message));
    }

    public void replaceMessage(Message message, Group toGroup) {
        String id = message.getCreated() + "";
        database.replace(dbHelper.createTable(toGroup.getId()), ChatDB.COLUMN_ID
                + " = " + id, messageToValue(message));
    }

    public void deleteMessage(Message message, User toUser) {
        String id = message.getCreated() + "";
        System.out.println("Comment deleted with id: " + id);
        database.delete(dbHelper.createTable(toUser.getId()), ChatDB.COLUMN_ID
                + " = " + id, null);
    }

    public void deleteMessage(Message message, Group toGroup) {
        String id = message.getCreated() + "";
        System.out.println("Comment deleted with id: " + id);
        database.delete(dbHelper.createTable(toGroup.getId()), ChatDB.COLUMN_ID
                + " = " + id, null);
    }

    public ArrayList<Message> getMessagesByPage(int currentPage)
    {
        ArrayList<Message> messages = new ArrayList<>();
        messages = SyncModule.getAllMessagesFromServer(currentPage);
        Collections.sort(messages, new Comparator<Message>() {
            @Override
            public int compare(Message lhs, Message rhs) {
                if(lhs.getCreated() == rhs.getCreated())
                    return 0;
                else if(lhs.getCreated() > rhs.getCreated())
                    return 1;
                else
                    return -1;
            }
        });

        return messages;
    }
    public ArrayList<Message> getAllMessages(User toUser, Group toGroup) {
        ArrayList<Message> messages = new ArrayList<>();

        Cursor cursor;

        if (toUser != null)
            cursor = database.query(dbHelper.createTable(toUser.getId()),
                    allColumns, null, null, null, null, null);
        else
            cursor = database.query(dbHelper.createTable(toGroup.getId()),
                    allColumns, null, null, null, null, null);

        if (cursor.getCount() <= 0) {
            messages = SyncModule.getAllMessagesFromServer(1);
            //TODO Check the message ordering
            //Collections.reverse(messages);
            for (Message message : messages) {
                if (toUser != null)
                    insertMessage(message, toUser);
                else
                    insertMessage(message, toGroup);
            }
        }

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Message message = cursorToMessage(cursor);
            messages.add(message);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        Collections.sort(messages, new Comparator<Message>() {
            @Override
            public int compare(Message lhs, Message rhs) {
                if(lhs.getCreated() == rhs.getCreated())
                    return 0;
                else if(lhs.getCreated() > rhs.getCreated())
                    return 1;
                else
                    return -1;
            }
        });

        return messages;
    }

    private Message cursorToMessage(Cursor cursor) {
        Message message = new Message();
        message.setId(cursor.getString(0));
        message.setBody(cursor.getString(1));
        message.setMessageType(cursor.getString(2));
        message.setFromUserId(cursor.getString(3));
        message.setFromUserName(cursor.getString(4));
        message.setToUserId(cursor.getString(5));
        message.setToUserName(cursor.getString(6));
        message.setToGroupId(cursor.getString(7));
        message.setToGroupName(cursor.getString(8));
        message.setImageLocalFileId(cursor.getString(9));
        message.setImageThumbLocalFileId(cursor.getString(10));
        message.setVideoFileId(cursor.getString(11));
        message.setVoiceFileId(cursor.getString(12));
        message.setLatitude(cursor.getString(13));
        message.setLongitude(cursor.getString(14));
        message.setImageFileId(cursor.getString(15));
        message.setImageThumbFileId(cursor.getString(16));
        message.setVideoLocalFileId(cursor.getString(17));
        message.setVoiceLocalFileId(cursor.getString(18));
        message.setCreated(Long.parseLong(cursor.getString(19)));
        return message;
    }

    private ContentValues messageToValue(Message message) {
        ContentValues values = new ContentValues();
        values.put(ChatDB.COLUMN_ID, message.getCreated() + "");
        values.put(ChatDB.COLUMN_MESSAGE, message.getBody());
        values.put(ChatDB.COLUMN_MESSAGE_TYPE, message.getMessageType());
        values.put(ChatDB.COLUMN_MESSAGE_FROM_USERID, message.getFromUserId());
        values.put(ChatDB.COLUMN_MESSAGE_FROM_USER_NAME, message.getFromUserName());
        values.put(ChatDB.COLUMN_MESSAGE_TO_USERID, message.getToUserId());
        values.put(ChatDB.COLUMN_MESSAGE_TO_USER_NAME, message.getToUserName());
        values.put(ChatDB.COLUMN_MESSAGE_TO_GROUPID, message.getToGroupId());
        values.put(ChatDB.COLUMN_MESSAGE_TO_GROUP_NAME, message.getToGroupName());
        values.put(ChatDB.COLUMN_MESSAGE_LOCAL_IMG_FILE_ID, message.getImageLocalFileId());
        values.put(ChatDB.COLUMN_MESSAGE_LOCAL_IMG_THUMB_FILE_ID, message.getImageThumbLocalFileId());
        values.put(ChatDB.COLUMN_MESSAGE_VIDEO_FILE_ID, message.getVideoFileId());
        values.put(ChatDB.COLUMN_MESSAGE_VOICE_FILE_ID, message.getVoiceFileId());
        values.put(ChatDB.COLUMN_MESSAGE_LATITUDE, message.getLatitude());
        values.put(ChatDB.COLUMN_MESSAGE_LONGITUDE, message.getLongitude());
        values.put(ChatDB.COLUMN_MESSAGE_IMG_FILE_ID, message.getImageFileId());
        values.put(ChatDB.COLUMN_MESSAGE_IMG_THUMB_FILE_ID, message.getImageThumbFileId());
        values.put(ChatDB.COLUMN_MESSAGE_LOCAL_VIDEO_FILE_ID, message.getVideoLocalFileId());
        values.put(ChatDB.COLUMN_MESSAGE_LOCAL_VOICE_FILE_ID, message.getVoiceLocalFileId());
        values.put(ChatDB.COLUMN_CREATED, message.getCreated());
        return values;
    }
}

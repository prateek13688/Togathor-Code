package com.uf.togathor.db.dbinterface;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.uf.togathor.Togathor;
import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.db.localdb.ChatDB;
import com.uf.togathor.db.localdb.EventDB;
import com.uf.togathor.model.timeline.event.EventMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alok on 1/4/15.
 */
public class EventMessagesDataSource {

    private boolean open;
    private Context activity;
    private SQLiteDatabase database;
    private EventDB dbHelper;
    private String[] allColumns = {EventDB.COLUMN_UUID,
            EventDB.COLUMN_DATA, EventDB.COLUMN_TIMESTAMP};

    public EventMessagesDataSource(Context context) {
        dbHelper = new EventDB(context);
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

    public void insertEvent(EventMessage message, Group eventGroup) {
        ContentValues values = messageToValue(message);
        long insertId = database.insert(dbHelper.createTable(eventGroup.getId()), null,
                values);
    }

    public void replaceEvent(EventMessage message, Group toGroup) {
        String id = message.getGroupID() + "";
        database.replace(dbHelper.createTable(toGroup.getId()), EventDB.COLUMN_UUID
                + " = " + id, messageToValue(message));
    }

    public void deleteEvent(EventMessage message, Group toGroup) {
        String id = message.getGroupID() + "";
        database.delete(dbHelper.createTable(toGroup.getId()), EventDB.COLUMN_UUID
                + " = " + id, null);
    }

    public HashMap<String, EventMessage> getAllPendingEvents() {
        HashMap<String, EventMessage> messages = new HashMap<>();
        Cursor cursor;

        for(Group event : Togathor.getEventService().timelineGroups) {
            cursor = database.query(dbHelper.createTable(event.getId()),
                    allColumns, null, null, null, null, null);

            if (cursor.getCount() <= 0) {
                for (Map.Entry<String, EventMessage> entry : Togathor.getEventService()
                        .getPendingEventsFor(event).entrySet()) {
                    insertEvent(entry.getValue(), event);
                }
            }

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                EventMessage message = cursorToMessage(cursor);
                messages.put(message.getGroupID(), message);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return messages;
    }

    private EventMessage cursorToMessage(Cursor cursor) {
        EventMessage message = new EventMessage();
        JSONObject eventData;

        message.setUUID(cursor.getString(0));
        try {
            eventData = new JSONObject(cursor.getString(1));
            message.setEventData(eventData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        message.setTimestamp(Long.parseLong(cursor.getString(2)));
        return message;
    }

    private ContentValues messageToValue(EventMessage message) {
        ContentValues values = new ContentValues();
        values.put(EventDB.COLUMN_UUID, message.getUUID() + "");
        values.put(EventDB.COLUMN_DATA, message.getEventData().toString());
        values.put(EventDB.COLUMN_TIMESTAMP, message.getTimestamp());
        return values;
    }
}

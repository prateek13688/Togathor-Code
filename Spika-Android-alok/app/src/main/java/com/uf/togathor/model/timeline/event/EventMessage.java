package com.uf.togathor.model.timeline.event;

import org.json.JSONObject;

/**
 * Created by Alok on 3/19/2015
 */
public class EventMessage {

    public final static String LOCATION = "location";
    public final static String TIME = "time";
    public final static String BLE = "ble";
    public final static String UI = "ui";
    public final static String NOTIFY = "notify";
    public final static String LISTENER = "listener";
    public final static String CALLBACK = "callback";
    public final static String RESPONSE = "response";

    public final static String TYPE = "type";
    public final static String SUB_TYPE = "subtype";
    public final static String FROM_USER_NAME = "from_user_name";
    public final static String FROM_USER_ID = "from_user_id";

    private String groupID;
    private String eventType;
    private long timestamp;

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    private String UUID;
    private JSONObject eventData;

    public EventMessage() {
        eventData = new JSONObject();
        UUID = java.util.UUID.randomUUID().toString();
        timestamp = System.currentTimeMillis();
    }

    public void setEventData(JSONObject eventData) {
        this.eventData = eventData;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getUUID() {
        return UUID;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public JSONObject getEventData() {
        return eventData;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}


package com.uf.togathor.utils.appservices;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.uf.togathor.Togathor;
import com.uf.togathor.db.couchdb.CouchDB;
import com.uf.togathor.db.couchdb.ResultListener;
import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.management.UsersManagement;
import com.uf.togathor.management.SyncModule;
import com.uf.togathor.model.GroupCategory;
import com.uf.togathor.model.timeline.applet.GenericTimelineApplet;
import com.uf.togathor.modules.timeline.geofence.GeoFenceApplet;
import com.uf.togathor.modules.timeline.meetup.MeetupApplet;
import com.uf.togathor.model.timeline.event.EventBusMessage;
import com.uf.togathor.model.timeline.event.EventMessage;
import com.uf.togathor.utils.Utils;
import com.uf.togathor.utils.constants.Const;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import de.greenrobot.event.EventBus;

/**
 * Created by Alok on 3/18/2015
 */
public class EventService extends Service {

    private static final int SERVICE_STARTUP = 1;
    private static final int EVENT_REGISTRATION = 2;
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    public List<Group> timelineGroups;
    private HashMap<String, GenericTimelineApplet> runningApplets;
    public static EventBus eventBus;

    @Override
    public void onCreate() {

        runningApplets = new HashMap<>();
        eventBus = new EventBus();

        CouchDB.findGroupByCategoryIdAsync(Const.TIMELINE_GROUP_ID, new ResultListener<List<Group>>() {

            @Override
            public void onResultsSucceeded(List<Group> result) {
                if (result != null && result.size() != 0) {
                    timelineGroups = result;
                }
            }

            @Override
            public void onResultsFail() {

            }
        }, this, false);

        HandlerThread handlerThread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();

        Togathor.setEventService(this);

        serviceLooper = handlerThread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message message = serviceHandler.obtainMessage();
        serviceHandler.sendMessage(message);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (Map.Entry<String, GenericTimelineApplet> appletEntry : runningApplets.entrySet()) {
            appletEntry.getValue().stopApplet();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public HashMap<String, EventMessage> getPendingEventsFor(Group group) {
        HashMap<String, EventMessage> events = new HashMap<>();
        List<com.uf.togathor.model.Message> messages;

        messages = SyncModule.getEventsFromServerFor(group);

        for (com.uf.togathor.model.Message message : messages) {
            events.put(message.getId(), parseMessage(message));
        }

        return events;
    }

    public HashMap<String, EventMessage> getAllPendingEvents() {
        HashMap<String, EventMessage> events = new HashMap<>();

        for (Group eventGroup : timelineGroups) {
            events.putAll(getPendingEventsFor(eventGroup));
        }

        return events;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {

                case SERVICE_STARTUP:
                    break;

                case EVENT_REGISTRATION:
                    break;
            }
        }
    }

    public void sendEventMessage(EventMessage eventMessage, Group eventGroup) {

        com.uf.togathor.model.Message message = new com.uf.togathor.model.Message();
        message.setId(Const._ID);
        message.setFromUserId(UsersManagement.getLoginUser().getId());
        message.setFromUserName(UsersManagement.getLoginUser().getName());
        message.setToGroupId(eventGroup.getId());
        message.setToGroupName(eventGroup.getName());
        message.setMessageType(Const.NEWS);
        message.setCreated(System.currentTimeMillis());

        message.setBody(eventMessage.getEventData().toString());
        SyncModule.sendMessage(message, this, null, eventGroup);
    }

    public static EventMessage parseMessage(com.uf.togathor.model.Message message) {
        EventMessage eventMessage = new EventMessage();
        JSONObject jsonObject;
        eventMessage.setGroupID(message.getToGroupId());
        try {
            jsonObject = new JSONObject(message.getBody());
            if (jsonObject.getString(EventMessage.TYPE).equals(EventMessage.LISTENER)) {
                eventMessage.setEventType(EventMessage.LISTENER);
            } else if (jsonObject.getString(EventMessage.TYPE).equals(EventMessage.CALLBACK)) {
                eventMessage.setEventType(EventMessage.CALLBACK);
            } else if (jsonObject.getString(EventMessage.TYPE).equals(EventMessage.RESPONSE)) {
                eventMessage.setEventType(EventMessage.RESPONSE);
            }
            eventMessage.setEventData(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return eventMessage;
    }

    public void processEvent(EventMessage eventMessage, Group eventGroup) {
        Togathor.getEventMessagesDataSource().close();
        Togathor.getEventMessagesDataSource().open();
        Togathor.getEventMessagesDataSource().insertEvent(eventMessage, eventGroup);

        GenericTimelineApplet applet = buildApplet(eventGroup);

        if (eventMessage.getEventType().equals(EventMessage.LISTENER)) {
            applet.processListener(eventMessage, eventGroup);
        } else if (eventMessage.getEventType().equals(EventMessage.RESPONSE)) {
            eventBus.post(new EventBusMessage(eventMessage, eventGroup));
        }
    }

    //TODO Reflection-based implementation
    public GenericTimelineApplet buildApplet(Group eventGroup) {
        String name = eventGroup.getName();
        GenericTimelineApplet applet = null;

        if(runningApplets.containsKey(eventGroup.getName()))
            return runningApplets.get(eventGroup.getName());

        name = new StringTokenizer(name, ":").nextToken();

        switch (name) {
            case Const.TIMELINE_MEETUP_APPLET:
                applet = new MeetupApplet(Togathor.getEventService());
                break;

            case Const.TIMELINE_GEOFENCE_APPLET:
                applet = new GeoFenceApplet(Togathor.getEventService());
                break;
        }

        runningApplets.put(eventGroup.getName(), applet);
        return applet;
    }

    public static Group createEventGroup(String appletType) {
        String groupID;
        Group eventGroup;

        GroupCategory timelineCat = new GroupCategory();
        timelineCat.setId(Const.TIMELINE_GROUP_ID);
        timelineCat.setTitle("Timeline");

        eventGroup = new Group();
        eventGroup.setName(appletType + ":" + System.currentTimeMillis());
        eventGroup.setCategoryName(timelineCat.getTitle());
        eventGroup.setCategoryId(timelineCat.getId());
        eventGroup.setPassword(Const.GROUP_PASSWORD);
        groupID = Utils.createGroup(Togathor.getEventService(), eventGroup, false);
        if (groupID != null) {
            eventGroup.setId(groupID);
            Log.d("Timeline created", "with group id " + eventGroup.getId());
        }

        return eventGroup;
    }

    public static boolean verifyAppletInstance(Group eventGroup, String eventName)  {
        return eventGroup.getName().equals(eventName);
    }
}

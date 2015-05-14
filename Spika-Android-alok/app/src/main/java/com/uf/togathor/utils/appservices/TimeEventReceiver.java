package com.uf.togathor.utils.appservices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.uf.togathor.Togathor;
import com.uf.togathor.db.couchdb.CouchDB;
import com.uf.togathor.db.couchdb.ResultListener;
import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.model.timeline.applet.GenericTimelineApplet;
import com.uf.togathor.model.timeline.event.EventMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alok on 4/4/2015.
 */
public class TimeEventReceiver extends BroadcastReceiver {

    private HashMap<String, EventMessage> pendingEvents;
    private Group eventGroup;
    private String eventGroupID;
    private EventMessage eventMessage;
    private GenericTimelineApplet applet;

    @Override
    public void onReceive(Context context, final Intent intent) {
        eventGroupID = intent.getStringExtra("groupid");
        CouchDB.findGroupByIdAsync(eventGroupID, new ResultListener<Group>() {
            @Override
            public void onResultsSucceeded(Group result) {
                eventGroup = result;
                applet = Togathor.getEventService().buildApplet(eventGroup);
                pendingEvents = Togathor.getEventService().getPendingEventsFor(eventGroup);

                for(Map.Entry<String, EventMessage> eventMessageEntry : pendingEvents.entrySet()){
                    eventMessage = eventMessageEntry.getValue();
                    if(eventMessage.getEventType().equals(EventMessage.CALLBACK))
                        applet.processCallback(eventMessage, eventGroup);
                }
            }
            @Override
            public void onResultsFail() {

            }
        }, context, false);
    }
}

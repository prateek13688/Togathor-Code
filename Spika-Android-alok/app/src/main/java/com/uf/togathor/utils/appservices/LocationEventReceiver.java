package com.uf.togathor.utils.appservices;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.uf.togathor.R;
import com.uf.togathor.Togathor;
import com.uf.togathor.db.couchdb.CouchDB;
import com.uf.togathor.db.couchdb.ResultListener;
import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.model.timeline.applet.GenericTimelineApplet;
import com.uf.togathor.model.timeline.event.EventMessage;
import com.uf.togathor.model.timeline.event.location.LocationEventCallback;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alok on 4/22/2015.
 */
public class LocationEventReceiver extends IntentService {
    protected static final String TAG = "geofence-transitions-service";
    private String eventGroupID;
    private Group eventGroup;
    private EventMessage eventMessage;
    private HashMap<String, EventMessage> pendingEvents;
    private GenericTimelineApplet applet;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public LocationEventReceiver() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            return;
        }

        // Get the transition type.
        final int geofenceTransition = geofencingEvent.getGeofenceTransition();

        eventGroupID = intent.getStringExtra("groupid");
        CouchDB.findGroupByIdAsync(eventGroupID, new ResultListener<Group>() {
            @Override
            public void onResultsSucceeded(Group result) {
                eventGroup = result;
                applet = Togathor.getEventService().buildApplet(eventGroup);
                pendingEvents = Togathor.getEventService().getPendingEventsFor(eventGroup);

                for (Map.Entry<String, EventMessage> eventMessageEntry : pendingEvents.entrySet()) {
                    eventMessage = eventMessageEntry.getValue();
                    try {
                        eventMessage.getEventData().put(LocationEventCallback.TRANSITION_TYPE, geofenceTransition);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (eventMessage.getEventType().equals(EventMessage.CALLBACK))
                        applet.processCallback(eventMessage, eventGroup);
                }
            }

            @Override
            public void onResultsFail() {

            }
        }, this, false);
    }
}

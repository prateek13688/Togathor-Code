package com.uf.togathor.modules.timeline.geofence;

import android.content.Context;
import android.content.Intent;

import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.model.timeline.applet.GenericTimelineApplet;
import com.uf.togathor.model.timeline.event.EventMessage;
import com.uf.togathor.model.timeline.event.GenericEventCallback;
import com.uf.togathor.model.timeline.event.GenericEventListener;
import com.uf.togathor.model.timeline.event.location.LocationEventCallback;
import com.uf.togathor.model.timeline.event.location.LocationEventListener;
import com.uf.togathor.model.timeline.event.time.TimeEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Alok on 4/5/2015
 */

public class GeoFenceApplet implements GenericTimelineApplet {

    public static final String NAME = "geofencename";

    private List<GenericEventCallback> runningCallbacks = new ArrayList<>();
    private Context context;

    public GeoFenceApplet(Context context) {
        this.context = context;
    }

    @Override
    public void createAppletInstance() {
        context.startActivity(new Intent(context, GeoFenceCreateActivity.class));
    }

    @Override
    public void processListener(EventMessage eventMessage, Group eventGroup) {
        try {
            switch (eventMessage.getEventData().getString(EventMessage.SUB_TYPE)) {
                case EventMessage.LOCATION:
                    GenericEventListener locationEventListener;
                    JSONObject locationListener = eventMessage.getEventData();
                    locationEventListener = new LocationEventListener(
                            locationListener.getString(LocationEventCallback.PLACE_NAME),
                            locationListener.getDouble(LocationEventCallback.PLACE_LAT),
                            locationListener.getDouble(LocationEventCallback.PLACE_LON),
                            context);
                    locationEventListener.scheduleListener();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processCallback(EventMessage eventMessage, Group eventGroup) {
        try {
            switch (eventMessage.getEventData().getString(EventMessage.SUB_TYPE)) {
                case EventMessage.LOCATION:
                    GenericEventCallback locationEventCallback =
                            new LocationEventCallback(eventGroup);
                    locationEventCallback.startCallbackService();
                    runningCallbacks.add(locationEventCallback);
                    break;

                case EventMessage.UI:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopApplet() {
        for (GenericEventCallback callback : runningCallbacks) {
            callback.stopCallbackService();
        }
    }
}

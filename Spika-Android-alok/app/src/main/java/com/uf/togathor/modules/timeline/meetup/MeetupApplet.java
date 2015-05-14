package com.uf.togathor.modules.timeline.meetup;

import android.content.Context;
import android.content.Intent;

import com.dexafree.materialList.model.Card;
import com.uf.togathor.R;
import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.model.timeline.applet.GenericTimelineApplet;
import com.uf.togathor.model.timeline.event.EventMessage;
import com.uf.togathor.model.timeline.event.GenericEventCallback;
import com.uf.togathor.model.timeline.event.GenericEventListener;
import com.uf.togathor.model.timeline.event.location.LocationEventCallback;
import com.uf.togathor.model.timeline.event.time.TimeEventListener;
import com.uf.togathor.modules.timeline.TimelineActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Alok on 4/5/2015
 */
public class MeetupApplet implements GenericTimelineApplet {

    public static final String NAME = "meetupname";

    private List<GenericEventCallback> runningCallbacks = new ArrayList<>();
    private Context context;

    public MeetupApplet(Context context) {
        this.context = context;
    }

    @Override
    public void createAppletInstance() {
        context.startActivity(new Intent(context, MeetupCreateActivity.class));
    }

    private Card generateCard(EventMessage callbackMessage, Group eventGroup) {
        return new MeetupTimelineCard(R.layout.card_timeline_meetup, callbackMessage, eventGroup);
    }

    @Override
    public void processListener(EventMessage eventMessage, Group eventGroup) {
        try {
            switch (eventMessage.getEventData().getString(EventMessage.SUB_TYPE)) {
                case EventMessage.TIME:
                    GenericEventListener timeEventListener;
                    Calendar calendar = Calendar.getInstance();
                    JSONObject timeListener = eventMessage.getEventData();

                    try {
                        calendar.set(Calendar.MONTH, timeListener.getInt(TimeEventListener.MONTH));
                        calendar.set(Calendar.YEAR, timeListener.getInt(TimeEventListener.YEAR));
                        calendar.set(Calendar.DAY_OF_MONTH, timeListener.getInt(TimeEventListener.DATE));
                        calendar.set(Calendar.HOUR_OF_DAY, timeListener.getInt(TimeEventListener.HOUR));
                        calendar.set(Calendar.MINUTE, timeListener.getInt(TimeEventListener.MINUTE));
                        calendar.set(Calendar.SECOND, 0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    timeEventListener = new TimeEventListener(calendar, eventGroup, context);
                    timeEventListener.scheduleListener();
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
                            new LocationEventCallback(
                                    LocationEventCallback
                                            .decodeEventLocation(eventMessage.getEventData()),
                                    eventGroup);
                    locationEventCallback.startCallbackService();
                    runningCallbacks.add(locationEventCallback);
                    break;

                case EventMessage.UI:
                    TimelineActivity.getInstance()
                            .addEventCard(generateCard(eventMessage, eventGroup));
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

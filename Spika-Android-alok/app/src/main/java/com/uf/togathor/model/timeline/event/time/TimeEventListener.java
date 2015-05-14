package com.uf.togathor.model.timeline.event.time;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.model.timeline.event.EventMessage;
import com.uf.togathor.model.timeline.event.GenericEventListener;
import com.uf.togathor.utils.appservices.TimeEventReceiver;

import java.util.Calendar;

/**
 * Created by Alok on 4/3/2015
 */
public class TimeEventListener implements GenericEventListener {
    public final static String DATE = "date";
    public final static String MONTH = "month";
    public final static String YEAR = "year";

    public final static String HOUR = "hour";
    public final static String MINUTE = "minute";

    private Calendar calendar;
    private Group eventGroup;
    private Context context;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

    public TimeEventListener(Calendar calendar, Group eventGroup, Context context)   {
        this.calendar = calendar;
        this.eventGroup = eventGroup;
        this.context = context;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public void scheduleListener() {
        Intent callbackIntent = new Intent(context, TimeEventReceiver.class);
        callbackIntent.putExtra("groupid", eventGroup.getId());
        callbackIntent.putExtra(EventMessage.TYPE, EventMessage.TIME);
        pendingIntent = PendingIntent.getBroadcast(context, 0, callbackIntent, 0);

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    @Override
    public void cancelListener() {
        alarmManager.cancel(pendingIntent);
    }
}

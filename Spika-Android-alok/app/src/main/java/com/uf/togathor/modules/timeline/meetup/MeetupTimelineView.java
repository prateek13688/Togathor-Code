package com.uf.togathor.modules.timeline.meetup;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.dexafree.materialList.model.CardItemView;
import com.uf.togathor.R;
import com.uf.togathor.Togathor;
import com.uf.togathor.model.timeline.event.location.LocationEventCallback;
import com.uf.togathor.model.timeline.event.time.TimeEventListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Alok on 3/10/2015
 */
public class MeetupTimelineView extends CardItemView<MeetupTimelineCard> implements View.OnClickListener {

    Context context;
    MeetupTimelineCard currentCard;
    TextView meetupName;
    TextView meetupPlace;
    TextView meetupTime;

    public MeetupTimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MeetupTimelineView(Context context) {
        super(context);
        this.context = context;
    }

    public MeetupTimelineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    public void build(MeetupTimelineCard meetupTimelineCard) {
        meetupName = (TextView) findViewById(R.id.meetup_name);
        meetupPlace = (TextView) findViewById(R.id.meetup_place);
        meetupTime = (TextView) findViewById(R.id.meetup_time);
        currentCard = meetupTimelineCard;
        meetupName.setOnClickListener(this);
        parseMessage();
    }

    private void parseMessage()    {
        JSONObject jsonObject = currentCard.getEventMessage().getEventData();
        String time;
        int hourOfDay;
        int minute;
        String ampm;
        try {
            meetupName.setText(jsonObject.getString(MeetupApplet.NAME));
            meetupPlace.setText(jsonObject.getString(LocationEventCallback.PLACE_NAME));
            hourOfDay = jsonObject.getInt(TimeEventListener.HOUR);
            ampm = hourOfDay >= 12 ? " PM" : " AM";
            hourOfDay = hourOfDay > 12 ? (hourOfDay - 12) : hourOfDay;
            minute = jsonObject.getInt(TimeEventListener.MINUTE);
            time = hourOfDay + ":";
            time += String.format("%02d", minute);
            time += ampm;
            meetupTime.setText(time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(Togathor.getEventService().getBaseContext(),
                MeetupDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("eventname", currentCard.getEventGroup().getName());
        intent.putExtra("groupid", currentCard.getEventGroup().getId());
        Togathor.getEventService().startActivity(intent);
    }
}

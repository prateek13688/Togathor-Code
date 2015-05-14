package com.uf.togathor.modules.timeline;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.view.MaterialListView;
import com.gc.materialdesign.views.Button;
import com.gc.materialdesign.views.ButtonFloat;
import com.uf.togathor.R;
import com.uf.togathor.modules.timeline.geofence.GeoFenceCreateActivity;
import com.uf.togathor.modules.timeline.meetup.MeetupCreateActivity;
import com.uf.togathor.model.timeline.event.EventMessage;
import com.uf.togathor.utils.Utils;
import com.uf.togathor.utils.appservices.EventService;
import com.uf.togathor.utils.constants.Const;

import java.util.ArrayList;
import java.util.List;

public class TimelineActivity extends ActionBarActivity {

    private static final String TAG = "TimelineFragment";
    public static MaterialListView eventListView;
    public static List<EventMessage> currentEvents;

    private Button addEvent;
    private MaterialDialog addEventDialog;
    public static TimelineActivity instance;

    private String[] applets = {Const.TIMELINE_MEETUP_APPLET, Const.TIMELINE_GEOFENCE_APPLET};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        instance = this;

        if(!Utils.isServiceRunning(EventService.class)) {
            Intent eventServiceIntent = new Intent(this, EventService.class);
            startService(eventServiceIntent);
        }


        eventListView = (MaterialListView) findViewById(R.id.list_of_events);
        eventListView.setCardAnimation(MaterialListView.CardAnimation.SCALE_IN);
        currentEvents = new ArrayList<>();

        //TODO Get events that need UI

        addEvent = (ButtonFloat) findViewById(R.id.add_event);
        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEventDialog.show();
            }
        });

        addEventDialog = new MaterialDialog.Builder(this)
                .title("Choose Applet")
                .items(applets)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        switch (charSequence.toString())   {
                            case Const.TIMELINE_MEETUP_APPLET :
                                startActivity(new Intent(TimelineActivity.this, MeetupCreateActivity.class));
                                break;

                            case Const.TIMELINE_GEOFENCE_APPLET:
                                startActivity(new Intent(TimelineActivity.this, GeoFenceCreateActivity.class));
                        }
                    }
                }).build();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        }
        return false;
    }

    public static TimelineActivity getInstance() {
        return instance;
    }

    public void addEventCard(Card eventCard)  {
        eventListView.add(eventCard);
    }
}
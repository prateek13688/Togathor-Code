package com.uf.togathor.modules.timeline.geofence;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.uf.togathor.R;
import com.uf.togathor.db.couchdb.CouchDB;
import com.uf.togathor.db.couchdb.TogathorException;
import com.uf.togathor.db.couchdb.TogathorForbiddenException;
import com.uf.togathor.db.couchdb.model.Member;
import com.uf.togathor.model.timeline.event.EventBusMessage;
import com.uf.togathor.model.timeline.event.EventMessage;
import com.uf.togathor.model.timeline.event.EventResponseListener;
import com.uf.togathor.model.timeline.event.location.LocationEventCallback;
import com.uf.togathor.utils.appservices.EventService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Alok on 4/6/2015.
 */
public class GeoFenceDetailActivity extends ActionBarActivity implements EventResponseListener {

    private String eventName;
    private GoogleMap locationMap;
    private HashMap<Member, Marker> markers;
    private HashMap<String, Member> groupMembers;

    private String groupID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_geofence);
        markers = new HashMap<>();
        groupMembers = new HashMap<>();
        eventName = getIntent().getStringExtra("eventname");
        groupID = getIntent().getStringExtra("groupid");

        locationMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        locationMap.setMyLocationEnabled(false);
        locationMap.getUiSettings().setMyLocationButtonEnabled(true);
        locationMap.getUiSettings().setZoomGesturesEnabled(true);
        locationMap.getUiSettings().setScrollGesturesEnabled(true);
        locationMap.getUiSettings().setCompassEnabled(true);

        new GetGroupMembers().execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventService.eventBus.unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventService.eventBus.unregister(this);
    }

    public void updateUI(EventMessage eventMessage)    {
        JSONObject jsonObject = eventMessage.getEventData();
        String userID;
        Member user = null;
        int transitionType = 0;
        double latitude = 0.0, longitude = 0.0;

        try {
            userID = jsonObject.getString(EventMessage.FROM_USER_ID);
            user = groupMembers.get(userID);
            latitude = jsonObject.getDouble(LocationEventCallback.PLACE_LAT);
            longitude = jsonObject.getDouble(LocationEventCallback.PLACE_LON);
            transitionType = jsonObject.getInt(LocationEventCallback.TRANSITION_TYPE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assert user != null;

        if(transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            markers.get(user).setVisible(true);
            markers.get(user).setPosition(new LatLng(latitude, longitude));
            markers.get(user).setTitle(user.getName());
        } else if(transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
            markers.get(user).setVisible(false);
        }
    }

    @Override
    public void registerForResponse() {
        EventService.eventBus.register(this);
    }

    @Override
    public void onEvent(final EventBusMessage eventResponse) {
        String responseType = "";

        if (EventService.verifyAppletInstance(eventResponse.getEventGroup(), eventName)) {
            try {
                JSONObject jsonObject = eventResponse.getEventMessage().getEventData();
                responseType = jsonObject.getString(EventMessage.SUB_TYPE);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            switch (responseType) {
                case EventMessage.UI:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUI(eventResponse.getEventMessage());
                        }
                    });
                    break;
            }
        }
    }

    public class GetGroupMembers extends AsyncTask<Void, Void, Void> {

        List<Member> localList;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                localList = CouchDB.findMembersByGroupId(groupID, 0, 200);
            } catch (IOException | JSONException | TogathorException | TogathorForbiddenException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            for(Member member : localList){
                groupMembers.put(member.getId(), member);
                markers.put(member, locationMap.addMarker(new MarkerOptions()
                        .position(new LatLng(10, 10))
                        .title(member.getName() + ": Location Unknown")));
                markers.get(member).setVisible(false);
            }
            registerForResponse();
        }
    }
}

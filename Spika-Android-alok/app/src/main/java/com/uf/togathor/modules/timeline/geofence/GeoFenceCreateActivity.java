package com.uf.togathor.modules.timeline.geofence;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.uf.togathor.R;
import com.uf.togathor.Togathor;
import com.uf.togathor.db.couchdb.CouchDB;
import com.uf.togathor.db.couchdb.ResultListener;
import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.db.couchdb.model.User;
import com.uf.togathor.management.UsersManagement;
import com.uf.togathor.model.timeline.event.EventMessage;
import com.uf.togathor.model.timeline.event.location.LocationEventCallback;
import com.uf.togathor.utils.appservices.EventService;
import com.uf.togathor.utils.constants.Const;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Alok on 4/2/2015.
 */
public class GeoFenceCreateActivity extends ActionBarActivity {

    public static final int PLACE_PICKER_REQUEST = 4096;

    private EditText geofenceName;
    private TextView geofencePlace;
    private TextView geofenceUsers;
    private List<String> userContacts;
    private HashMap<String, String> userContactsID;
    private FloatingActionButton finish;

    private GoogleMap locationMap;
    private LatLng currentLocation;
    private Place chosenPlace;
    private MarkerOptions marker;

    private PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
    private Group eventGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_geofence);

        userContacts = new ArrayList<>();
        userContactsID = new HashMap<>();

        geofenceName = (EditText) findViewById(R.id.meetup_name);
        geofencePlace = (TextView) findViewById(R.id.pick_place);
        geofenceUsers = (TextView) findViewById(R.id.pick_users);

        geofencePlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivityForResult(builder.build(GeoFenceCreateActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        //TODO Replace with a generic user contacts provider
        geofenceUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CouchDB.findUserContactsAsync(UsersManagement.getLoginUser().getId(), new ResultListener<List<User>>() {
                    @Override
                    public void onResultsSucceeded(List<User> result) {
                        userContacts.clear();
                        userContactsID.clear();
                        if (eventGroup == null)
                            eventGroup = EventService.createEventGroup(Const.TIMELINE_GEOFENCE_APPLET);

                        for (User contact : result) {
                            userContacts.add(contact.getName());
                            userContactsID.put(contact.getName(), contact.getId());
                        }

                        new MaterialDialog.Builder(GeoFenceCreateActivity.this)
                                .title("Contacts")
                                .items(userContacts.toArray(new String[userContacts.size()]))
                                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                        /**
                                         * If you use alwaysCallMultiChoiceCallback(), which is discussed below,
                                         * returning false here won't allow the newly selected check box to actually be selected.
                                         * See the limited multi choice dialog example in the sample project for details.
                                         **/

                                        for (CharSequence userID : text) {

                                            CouchDB.addFavoriteGroupAsync(eventGroup.getId(), userContactsID.get(userID.toString()), new ResultListener<Boolean>() {
                                                @Override
                                                public void onResultsSucceeded(Boolean result) {
                                                    Log.d("AddUserToTimeline", result + "");
                                                }

                                                @Override
                                                public void onResultsFail() {
                                                    Log.d("AddUserToTimeline", "Failed!");
                                                }
                                            }, GeoFenceCreateActivity.this, true);
                                        }

                                        return true;
                                    }
                                })
                                .positiveText("DONE")
                                .show();
                    }

                    @Override
                    public void onResultsFail() {

                    }
                }, GeoFenceCreateActivity.this, true);
            }
        });

        finish = (FloatingActionButton) findViewById(R.id.finish_setup);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                EventMessage eventMessage = new EventMessage();
                eventMessage.setGroupID(eventGroup.getId());
                try {
                    jsonObject.put(EventMessage.TYPE, EventMessage.LISTENER);
                    jsonObject.put(EventMessage.SUB_TYPE, EventMessage.LOCATION);
                    jsonObject.put(LocationEventCallback.PLACE_NAME, chosenPlace.getName());
                    jsonObject.put(LocationEventCallback.PLACE_LAT, chosenPlace.getLatLng().latitude + "");
                    jsonObject.put(LocationEventCallback.PLACE_LON, chosenPlace.getLatLng().longitude + "");
                    eventMessage.setEventData(jsonObject);

                    Togathor.getEventService().sendEventMessage(eventMessage, eventGroup);

                    jsonObject = new JSONObject();
                    jsonObject.put(EventMessage.TYPE, EventMessage.CALLBACK);
                    jsonObject.put(EventMessage.SUB_TYPE, EventMessage.LOCATION);
                    eventMessage.setEventData(jsonObject);

                    Togathor.getEventService().sendEventMessage(eventMessage, eventGroup);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent hurry = new Intent(GeoFenceCreateActivity.this, GeoFenceDetailActivity.class);
                hurry.putExtra("eventname", eventGroup.getName());
                hurry.putExtra("groupid", eventGroup.getId());
                startActivity(hurry);
                finish();
            }
        });

        locationMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        locationMap.setMyLocationEnabled(true);
        locationMap.getUiSettings().setMyLocationButtonEnabled(true);
        locationMap.getUiSettings().setZoomGesturesEnabled(true);
        locationMap.getUiSettings().setScrollGesturesEnabled(true);
        locationMap.getUiSettings().setCompassEnabled(true);

        locationMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location arg0) {
                setNewLocation(new LatLng(arg0.getLatitude(), arg0.getLongitude()));
                locationMap.setOnMyLocationChangeListener(null);
            }
        });
    }

    private void setNewLocation(LatLng location)  {
        locationMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
        currentLocation = location;
    }

    private void setNewLocation(Place location)  {
        locationMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location.getLatLng(), 16));
        currentLocation = location.getLatLng();
        locationMap.clear();
        locationMap.addMarker(new MarkerOptions().position(location.getLatLng())
                .title(location.getName().toString()));
        geofencePlace.setText(location.getName());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GeoFenceCreateActivity.PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                setNewLocation(place);
                chosenPlace = place;
            }
        }
    }
}

package com.uf.togathor.modules.timeline.meetup;

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
import com.uf.togathor.model.timeline.event.time.TimeEventListener;
import com.uf.togathor.utils.appservices.EventService;
import com.uf.togathor.utils.constants.Const;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Alok on 4/2/2015.
 */
public class MeetupCreateActivity extends ActionBarActivity implements
        TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    public static final int PLACE_PICKER_REQUEST = 4096;

    private EditText meetupName;
    private TextView meetupPlace;
    private TextView meetupDate;
    private TextView meetupTime;
    private TextView meetupUsers;
    private List<String> userContacts;
    private HashMap<String, String> userContactsID;
    private FloatingActionButton finish;

    private GoogleMap locationMap;
    private LatLng currentLocation;
    private Place chosenPlace;
    private MarkerOptions marker;

    private PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
    private Group eventGroup;

    private int year;
    private int month;
    private int date;
    private int hour;
    private int minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meetup);

        userContacts = new ArrayList<>();
        userContactsID = new HashMap<>();

        meetupName = (EditText) findViewById(R.id.meetup_name);
        meetupPlace = (TextView) findViewById(R.id.pick_place);
        meetupDate = (TextView) findViewById(R.id.pick_date);
        meetupTime = (TextView) findViewById(R.id.pick_time);
        meetupUsers = (TextView) findViewById(R.id.pick_users);

        meetupPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivityForResult(builder.build(MeetupCreateActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        meetupDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        MeetupCreateActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });

        meetupTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                        MeetupCreateActivity.this,
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        false
                );
                timePickerDialog.show(getFragmentManager(), "Timepickerdialog");
            }
        });


        //TODO Replace with a generic user contacts provider
        meetupUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CouchDB.findUserContactsAsync(UsersManagement.getLoginUser().getId(), new ResultListener<List<User>>() {
                    @Override
                    public void onResultsSucceeded(List<User> result) {

                        if(eventGroup == null)
                            eventGroup = EventService.createEventGroup(Const.TIMELINE_MEETUP_APPLET);

                        for(User contact : result)  {
                            userContacts.add(contact.getName());
                            userContactsID.put(contact.getName(), contact.getId());
                        }

                        new MaterialDialog.Builder(MeetupCreateActivity.this)
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
                                            }, MeetupCreateActivity.this, true);
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
                }, MeetupCreateActivity.this, true);
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
                    jsonObject.put(EventMessage.SUB_TYPE, EventMessage.TIME);
                    jsonObject.put(TimeEventListener.DATE, date);
                    jsonObject.put(TimeEventListener.MONTH, month);
                    jsonObject.put(TimeEventListener.YEAR, year);
                    jsonObject.put(TimeEventListener.HOUR, hour);
                    jsonObject.put(TimeEventListener.MINUTE, minute);
                    eventMessage.setEventData(jsonObject);

                    Togathor.getEventService().sendEventMessage(eventMessage, eventGroup);

                    jsonObject = new JSONObject();
                    jsonObject.put(EventMessage.TYPE, EventMessage.CALLBACK);
                    jsonObject.put(EventMessage.SUB_TYPE, EventMessage.LOCATION);
                    jsonObject.put(LocationEventCallback.PLACE_ID, chosenPlace.getId());
                    jsonObject.put(LocationEventCallback.PLACE_NAME, chosenPlace.getName());
                    jsonObject.put(LocationEventCallback.PLACE_LAT, chosenPlace.getLatLng().latitude + "");
                    jsonObject.put(LocationEventCallback.PLACE_LON, chosenPlace.getLatLng().longitude + "");
                    eventMessage.setEventData(jsonObject);

                    Togathor.getEventService().sendEventMessage(eventMessage, eventGroup);

                    jsonObject = new JSONObject();
                    jsonObject.put(EventMessage.TYPE, EventMessage.CALLBACK);
                    jsonObject.put(EventMessage.SUB_TYPE, EventMessage.UI);
                    jsonObject.put(MeetupApplet.NAME, meetupName.getText().toString());
                    jsonObject.put(LocationEventCallback.PLACE_NAME, chosenPlace.getName());
                    jsonObject.put(TimeEventListener.HOUR, hour);
                    jsonObject.put(TimeEventListener.MINUTE, minute);
                    eventMessage.setEventData(jsonObject);

                    Togathor.getEventService().sendEventMessage(eventMessage, eventGroup);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
        meetupPlace.setText(location.getName());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MeetupCreateActivity.PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                setNewLocation(place);
                chosenPlace = place;
            }
        }
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int monthOfYear, int dayOfMonth) {
        String date = (monthOfYear+1)+"/"+dayOfMonth+"/"+year;
        this.month = monthOfYear;
        this.date = dayOfMonth;
        this.year = year;

        meetupDate.setText(date);
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
        String time;

        this.hour = hourOfDay;
        this.minute = minute;

        hourOfDay = hourOfDay > 12 ? (hourOfDay - 12) : hourOfDay;
        time = hourOfDay+":";

        time += String.format("%02d", minute);
        if(radialPickerLayout.getIsCurrentlyAmOrPm() == 0)
            time += " AM";
        else
            time += " PM";

        meetupTime.setText(time);
    }
}

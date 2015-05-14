package com.uf.togathor.model.timeline.event.location;

import android.location.Location;
import android.os.AsyncTask;

import com.goebl.david.Response;
import com.goebl.david.Webb;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.uf.togathor.Togathor;
import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.model.timeline.event.EventMessage;
import com.uf.togathor.model.timeline.event.GenericEventCallback;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Alok on 3/19/2015
 */
public class LocationEventCallback implements GenericEventCallback, LocationListener {

    public final static String PLACE_ID = "placeid";
    public final static String PLACE_NAME = "placename";
    public final static String PLACE_LAT = "placelat";
    public final static String PLACE_LON = "placelon";
    public final static String TRANSITION_TYPE = "geofence_transition";

    private Webb webb = Webb.create();
    private LatLng destination;
    private Group eventGroup;

    public LocationEventCallback(LatLng destination, Group eventGroup) {
        this.destination = destination;
        this.eventGroup = eventGroup;
    }

    public LocationEventCallback(Group eventGroup) {
        this.destination = null;
        this.eventGroup = eventGroup;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (destination != null)
            new GetTravelInfo(location).execute();
        else
            onLocationEventResult("", location);
    }

    @Override
    public void startCallbackService() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi
                .requestLocationUpdates(Togathor.getGoogleAPIService().getGoogleApiClient(),
                        mLocationRequest, this);
    }

    @Override
    public void stopCallbackService() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                Togathor.getGoogleAPIService().getGoogleApiClient(), this);
    }

    private class GetTravelInfo extends AsyncTask<Void, Void, Void> {

        Location location;
        Response<String> response;

        private GetTravelInfo(Location location) {
            this.location = location;
        }

        @Override
        protected Void doInBackground(Void... params) {
            response = webb.post("https://maps.googleapis.com/maps/api/distancematrix/json?" +
                    "origins=" + location.getLatitude() + "," + location.getLongitude() +
                    "&destinations=" + destination.latitude + "," + destination.longitude +
                    "&mode=driving" +
                    "&language=en-US")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "text/plain")
                    .asString();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            onLocationEventResult(response.getBody(), location);
        }
    }

    public void onLocationEventResult(String response, Location currentLocation) {
        JSONObject jsonObject;
        EventMessage eventMessage = new EventMessage();
        try {
            if(response.length() != 0)
                jsonObject = new JSONObject(response);
            else
                jsonObject = new JSONObject();
            jsonObject.put(EventMessage.TYPE, EventMessage.RESPONSE);
            jsonObject.put(EventMessage.SUB_TYPE, EventMessage.UI);
            jsonObject.put(EventMessage.FROM_USER_ID, Togathor.getPreferences().getUserId());
            jsonObject.put(EventMessage.FROM_USER_NAME, Togathor.getPreferences().getUserName());
            jsonObject.put(LocationEventCallback.PLACE_LAT, currentLocation.getLatitude());
            jsonObject.put(LocationEventCallback.PLACE_LON, currentLocation.getLongitude());
            eventMessage.setGroupID(eventGroup.getId());
            eventMessage.setEventData(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Togathor.getEventService().sendEventMessage(eventMessage, eventGroup);
    }

    public static LatLng decodeEventLocation(JSONObject callback) {
        double lat = 0.0, lon = 0.0;
        try {
            lat = callback.getDouble(LocationEventCallback.PLACE_LAT);
            lon = callback.getDouble(LocationEventCallback.PLACE_LON);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new LatLng(lat, lon);
    }
}

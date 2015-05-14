package com.uf.togathor.model.timeline.event.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.uf.togathor.Togathor;
import com.uf.togathor.model.timeline.event.EventMessage;
import com.uf.togathor.model.timeline.event.GenericEventListener;
import com.uf.togathor.utils.appservices.LocationEventReceiver;
import com.uf.togathor.utils.appservices.TimeEventReceiver;

/**
 * Created by Alok on 4/3/2015.
 */
public class LocationEventListener implements GenericEventListener, ResultCallback<Status> {

    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 100; // 100m
    public static final int transitionResponse = Geofence.GEOFENCE_TRANSITION_ENTER |
            Geofence.GEOFENCE_TRANSITION_EXIT;

    private GenericGeoFence genericGeoFence;
    private String name;
    private double latitude, longitude;
    private Context context;
    private PendingIntent geofencePendingIntent;

    public LocationEventListener(String name, double latitude, double longitude, Context context)    {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.context = context;
    }

    @Override
    public void scheduleListener() {
        genericGeoFence = new GenericGeoFence(name, latitude, longitude, GEOFENCE_RADIUS_IN_METERS,
                GEOFENCE_EXPIRATION_IN_MILLISECONDS, transitionResponse);
        LocationServices.GeofencingApi.addGeofences(
                Togathor.getGoogleAPIService().getGoogleApiClient(), getGeofencingRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(this); // Result processed in onResult().
    }

    @Override
    public void cancelListener() {

    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent intent = new Intent(context, LocationEventReceiver.class);
        intent.putExtra(EventMessage.TYPE, EventMessage.LOCATION);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofence(genericGeoFence.toGeofence());

        // Return a GeofencingRequest.
        return builder.build();
    }

    @Override
    public void onResult(Status status) {
        new GeoFenceStore(context).setGeofence(name, genericGeoFence);
    }
}

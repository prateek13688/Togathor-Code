package com.uf.togathor.utils.appservices;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;
import com.uf.togathor.Togathor;
import com.uf.togathor.db.couchdb.Command;
import com.uf.togathor.db.couchdb.CouchDB;
import com.uf.togathor.db.couchdb.ResultListener;
import com.uf.togathor.db.couchdb.TogathorAsyncTask;
import com.uf.togathor.db.couchdb.TogathorException;
import com.uf.togathor.db.couchdb.TogathorForbiddenException;
import com.uf.togathor.management.UsersManagement;
import com.uf.togathor.utils.constants.Const;

import org.json.JSONException;

import java.io.IOException;

/**
 * Created by Alok on 1/24/2015.
 */
public class GoogleAPIService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static GoogleCloudMessaging gcm;
    private static String gcmRegID = "";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String TAG = "GoogleAPIService";
    private Context context;
    private GoogleApiClient googleApiClient;

    public GoogleAPIService(Context context)    {
        this.context = context;
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void registerGCM() {

        gcm = GoogleCloudMessaging.getInstance(context);
        gcmRegID = getRegistrationId();
        googleApiClient.connect();

        if (TextUtils.isEmpty(gcmRegID)) {
            registerInBackground();
        }
        else    {
            //TODO - If no GCM ID, re-run savepushtoken
            savePushTokenAsync(gcmRegID, Const.ONLINE, context);
            Log.d("GCMService",
                    "registerGCM - successfully registered with GCM server - regId: "
                            + gcmRegID);
        }
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId() {
        String existingGCMID = Togathor.getPreferences().getUserPushToken();
        if (existingGCMID.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        //TODO
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.

        return existingGCMID;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new RegisterGCMBackground(context).execute(null, null, null);
    }

    private class RegisterGCMBackground extends AsyncTask   {

        Context context;

        private RegisterGCMBackground(Context context)  {
            this.context = context;
        }

        @Override
        protected Object doInBackground(Object[] params) {

            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                gcmRegID = gcm.register(Const.PUSH_SENDER_ID);

                // Persist the regID - no need to register again.
                savePushTokenAsync(gcmRegID, Const.ONLINE, context);

                Log.d("GCMService",
                        "registerGCM - successfully registered with GCM server - regId: "
                                + gcmRegID);
            } catch (IOException ex) {
                Log.d(TAG, ex.getMessage());
                // If there is an error, don't just keep trying to register.
                // Require the user to click a button again, or perform
                // exponential back-off.
            }

            return null;
        }
    }

    public void savePushTokenAsync(String gcmRegID, String onlineStatus, Context context)  {
        new TogathorAsyncTask<Void, Void, Boolean>(new SavePushToken(gcmRegID, onlineStatus), new SavePushTokenListener(gcmRegID), context, false).execute();
    }

    private class SavePushToken implements Command<Boolean> {

        String pushToken;
        String onlineStatus;

        public SavePushToken (String pushToken, String onlineStatus) {
            this.pushToken = pushToken;
            this.onlineStatus = onlineStatus;
        }

        @Override
        public Boolean execute() throws JSONException, IOException,
                TogathorException, IllegalStateException, TogathorForbiddenException {

			/* set new androidToken and onlineStatus */
            UsersManagement.getLoginUser().setOnlineStatus(onlineStatus);
            Togathor.getPreferences().setUserEmail(UsersManagement.getLoginUser().getEmail());
            Togathor.getPreferences().setUserPushToken(pushToken);
            return CouchDB.updateUser(UsersManagement.getLoginUser());
        }
    }

    private class SavePushTokenListener implements ResultListener<Boolean> {

        String currentPushToken;

        public SavePushTokenListener (String currentPushToken) {
            this.currentPushToken = currentPushToken;
        }

        @Override
        public void onResultsSucceeded(Boolean result) {
            if (result) {
            } else {
                Togathor.getPreferences().setUserPushToken(currentPushToken);
            }
        }

        @Override
        public void onResultsFail() {
        }

    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected!");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}

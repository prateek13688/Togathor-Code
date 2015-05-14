package com.uf.togathor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.widget.Toast;

import com.crittercism.app.Crittercism;
import com.uf.togathor.db.couchdb.CouchDB;
import com.uf.togathor.db.couchdb.ResultListener;
import com.uf.togathor.db.couchdb.model.User;
import com.uf.togathor.management.UsersManagement;
import com.uf.togathor.utils.constants.Const;
import com.uf.togathor.utils.Preferences;

import java.util.List;

public class SplashScreenActivity extends Activity {

    private String mSavedEmail;
    private String mSavedPassword;
    public static SplashScreenActivity sInstance = null;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SplashScreenActivity.sInstance = this;
        setContentView(R.layout.activity_splash_screen);

		/* Initiate Crittercism */
        Crittercism.init(getApplicationContext(), Const.CRITTERCISM_APP_ID);

        new CouchDB();
        // new UsersManagement();

        if (Togathor.hasNetworkConnection()) {

            if (checkIfUserSignIn()) {
                mSavedEmail = Togathor.getPreferences().getUserEmail();
                mSavedPassword = Togathor.getPreferences().getUserPassword();

                mUser = new User();

                CouchDB.authAsync(mSavedEmail, mSavedPassword, new AuthListener(), SplashScreenActivity.this, false);
            } else {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        startActivity(new Intent(SplashScreenActivity.this,
                                SignInActivity.class));
                        finish();
                    }
                }, 10);
            }
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashScreenActivity.this,
                            HomeActivity.class);
                    intent.putExtra(Const.SIGN_IN, true);
                    SplashScreenActivity.this.startActivity(intent);
                    Toast.makeText(SplashScreenActivity.this,
                            getString(R.string.no_internet_connection),
                            Toast.LENGTH_LONG).show();
                }
            }, 10);
        }
    }

    private boolean checkIfUserSignIn() {
        boolean isSessionSaved = false;
        Preferences prefs = Togathor.getPreferences();
        if (prefs.getUserEmail() == null && prefs.getUserPassword() == null) {
            isSessionSaved = false;
        } else isSessionSaved = !(prefs.getUserEmail().equals("")
                && prefs.getUserPassword().equals(""));
        return isSessionSaved;
    }

    private void signIn(User u) {

        UsersManagement.setLoginUser(u);
        UsersManagement.setToUser(null);
        UsersManagement.setToGroup(null);

        Intent intent = new Intent(SplashScreenActivity.this,
                HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            signIn(mUser);
        } else {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class AuthListener implements ResultListener<String> {
        @Override
        public void onResultsSucceeded(String result) {
            boolean tokenOk = result.equals(Const.LOGIN_SUCCESS);
            mUser = UsersManagement.getLoginUser();
            if (tokenOk && mUser != null) {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        signIn(mUser);
                    }
                }, 10);
            } else {
            }
        }

        @Override
        public void onResultsFail() {
        }
    }
}
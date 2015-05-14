/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.uf.togathor.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.uf.togathor.R;
import com.uf.togathor.Togathor;
import com.uf.togathor.db.couchdb.CouchDB;
import com.uf.togathor.db.couchdb.ResultListener;
import com.uf.togathor.db.couchdb.model.Group;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Utils
 * <p/>
 * Contains various methods used through the application.
 */

@SuppressLint("SimpleDateFormat")
public class Utils {
    public static void copyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }

    private static Group createGroupHolder;

    public static Bitmap getBitmapFromUrl(String urlSource) {
        try {
            URL url = new URL(urlSource);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow((activity).getCurrentFocus()
                .getWindowToken(), 0);
    }

    /**
     * Returns length of jsonArray
     *
     * @param json
     * @return length if parameter is JSON Array, 0 if something else
     */
    public static int getJsonArrayLength(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            return jsonArray.length();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean isOsVersionHigherThenGingerbread() {
        return !(android.os.Build.VERSION.RELEASE.startsWith("1.")
                || android.os.Build.VERSION.RELEASE.startsWith("2.0")
                || android.os.Build.VERSION.RELEASE.startsWith("2.1")
                || android.os.Build.VERSION.RELEASE.startsWith("2.2")
                || android.os.Build.VERSION.RELEASE.startsWith("2.3"));
    }

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            Togathor.getInstance().getString(R.string.hookup_date_time_format));

    /**
     * Returns current time
     *
     * @return time in milliseconds
     * @throws Exception
     */
    public static long getCurrentDateTime() {
        long currentTime = System.currentTimeMillis();
        return currentTime;
    }

    /**
     * Returns time formatted as: JAN 24, 13:30
     *
     * @param timeInSeconds
     * @return date as a String
     */
    public static String getFormattedDateTime(long timeInSeconds) {
        Date dateTime = new Date(timeInSeconds * 1000);
        return DATE_FORMAT.format(dateTime);
    }

    public static String generateToken() {
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
                .toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 40; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }

        return sb.toString();
    }

    /**
     * @param fileId
     * @param imageView
     * @param progressBar
     * @param size
     * @param stubId
     */

    public static String checkPassword(Activity activity, String password) {
        if (password.length() < 6) {
            return activity.getString(R.string.password_error_number_of_characters);
        } else if (!isAlphaNumeric(password)) {
            return activity.getString(R.string.password_error_invalid_characters);
        }
        return activity.getString(R.string.password_ok);
    }

    public static String checkEmail(Activity activity, String email) {
        if (email.length() <= 0) {
            return activity.getString(R.string.email_length);
        } else if (!isEmailValid(email)) {
            return activity.getString(R.string.email_not_valid);
        }
        return activity.getString(R.string.email_ok);
    }

    public static String checkPasswordForGroup(Activity activity, String password) {

        if (password == null || password.length() == 0)
            return activity.getString(R.string.password_ok);


        if (password.length() < 6) {
            return activity.getString(R.string.password_error_number_of_characters);
        } else if (!isAlphaNumeric(password)) {
            return activity.getString(R.string.password_error_invalid_characters);
        }
        return activity.getString(R.string.password_ok);
    }

    public static String checkName(Activity activity, String name) {
        if (name != null && !name.equals("") && name.length() > 1) {
            return activity.getString(R.string.name_ok);
        } else {
            return activity.getString(R.string.name_error);
        }
    }

    private static boolean isAlphaNumeric(String s) {
        String pattern = "^[a-zA-Z0-9]*$";
        return s.matches(pattern);
    }

    /**
     * Used for checking valid email format.
     *
     * @param email String that will be checked
     * @return boolean true for valid false for invalid
     */
    private static boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static String getClassNameInStr(Object obj) {

        Class<?> enclosingClass = obj.getClass().getEnclosingClass();
        if (enclosingClass != null) {
            return enclosingClass.getName();
        } else {
            return obj.getClass().getName();
        }

    }

    public static boolean saveBitmapToFile(Bitmap bitmap, String path) {
        File file = new File(path);
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fOut);
            fOut.flush();
            fOut.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }

    public static Bitmap scaleBitmap(Bitmap originalBitmap, int toWidth, int toHeight) {

        int image_width = originalBitmap.getWidth();
        int image_height = originalBitmap.getHeight();

        float scaled_width, scaled_height;
        float scale = (float) toWidth / (float) image_width;

        scaled_width = image_width * scale;
        scaled_height = image_height * scale;

        return Bitmap.createScaledBitmap(originalBitmap, (int) scaled_width, (int) scaled_height, true);

    }

    //change the color of the icon
    public static void changeIconColor(Drawable icon){
        ColorFilter filter = new LightingColorFilter( Color.BLACK, Color.BLACK);
        icon.setColorFilter(filter);
    }

    public static String createGroup(Context context, Group group, boolean showProgress)   {
        try {
            createGroupHolder = group;
           return CouchDB.createGroupAsync(group, new GroupCreatedFinish(), context, showProgress);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class GroupCreatedFinish implements ResultListener<String> {

        @Override
        public void onResultsSucceeded(String groupId) {
            if (groupId != null) {
                Log.d("CreateGroup", "Successful");
                Log.d("CreateGroup", "Inserting into LocalDB");
                createGroupHolder.setId(groupId);
                Togathor.getContactsDataSource().insertGroup(createGroupHolder);
            } else {
                Log.d("CreateGroup", "Failed");
            }
        }

        @Override
        public void onResultsFail() {
            Log.d("CreateGroup", "Failed");
        }
    }

    public static boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) Togathor.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
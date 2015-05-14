package com.uf.togathor.modules.attendance;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.uf.togathor.R;
import com.uf.togathor.uitems.HookUpProgressDialog;
import com.uf.togathor.model.attendance.Course;
import com.uf.togathor.utils.appservices.CheckInServiceRetro;


public class AttendanceActivity extends ActionBarActivity {

    private final float larsenLat = 29.643119f;
    private final float larsenLong = -82.347155f;
    private static final int REQUEST_ENABLE_BT = 1;

    TextView courseName;
    TextView courseID;
    TextView courseInstructor;
    Course course;
    Toolbar toolbar;
    FloatingActionButton checkIn;
    private GoogleMap courseLocationMap;
    private MarkerOptions marker;
    HookUpProgressDialog progressDialog;

    private Intent serviceIntent;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                progressDialog.show();
                serviceIntent = new Intent(AttendanceActivity.this, CheckInServiceRetro.class);
                serviceIntent.putExtra("inapp", true);
                serviceIntent.putExtra("handler", new Messenger(new CheckInHandler(this, serviceIntent, progressDialog)));
                startService(serviceIntent);
            }
            else    {
                progressDialog.show();
                serviceIntent = new Intent(AttendanceActivity.this, CheckInServiceRetro.class);
                serviceIntent.putExtra("inapp", true);
                serviceIntent.putExtra("handler", new Messenger(new CheckInHandler(this, serviceIntent, progressDialog)));
                startService(serviceIntent);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_view);

        Intent intent = getIntent();

        course = new Course();
        course.setName(intent.getStringExtra("course_name"));
        course.setId(intent.getStringExtra("course_id"));
        course.setInstructor(intent.getStringExtra("course_instructor"));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        courseID = (TextView) findViewById(R.id.course_id);
        courseName = (TextView) findViewById(R.id.course_name);
        courseInstructor = (TextView) findViewById(R.id.course_instructor);
        checkIn = (FloatingActionButton) findViewById(R.id.check_in);
        progressDialog = new HookUpProgressDialog(this);

        if (courseLocationMap == null) {
            courseLocationMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            courseLocationMap.setMyLocationEnabled(true);
            courseLocationMap.getUiSettings().setMyLocationButtonEnabled(true);
            courseLocationMap.getUiSettings().setZoomGesturesEnabled(true);
            courseLocationMap.getUiSettings().setScrollGesturesEnabled(true);
            courseLocationMap.getUiSettings().setCompassEnabled(true);
            marker = new MarkerOptions().position(new LatLng(larsenLat, larsenLong)).title("Larsen Hall");
            courseLocationMap.addMarker(marker);
            courseLocationMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(larsenLat, larsenLong), 16));

            courseLocationMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                @Override
                public void onMyLocationChange(Location arg0) {
                    double lat = arg0.getLatitude();
                    double lon = arg0.getLongitude();
                    courseLocationMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 16));
                    courseLocationMap.setOnMyLocationChangeListener(null);
                }
            });

        }
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setTitle(course.getName());
        }
        courseName.setText(course.getName());
        courseID.setText(course.getId());
        courseInstructor.setText(course.getInstructor());

        checkIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIn();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_attendance_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.menu_action_attendance_list) {
            Intent intent = new Intent(AttendanceActivity.this, AttendanceListActivity.class);
            intent.putExtra("course", course.getId());
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkIn() {

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not supported", Toast.LENGTH_SHORT).show();
            finish();
        } else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP)    {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter btAdapter = bluetoothManager.getAdapter();
            if (!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                progressDialog.show();
                serviceIntent = new Intent(AttendanceActivity.this, CheckInServiceRetro.class);
                serviceIntent.putExtra("inapp", true);
                serviceIntent.putExtra("handler", new Messenger(new CheckInHandler(this, serviceIntent, progressDialog)));
                startService(serviceIntent);
            }
        }
        else {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter btAdapter = bluetoothManager.getAdapter();
            if (!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                progressDialog.show();
                serviceIntent = new Intent(AttendanceActivity.this, CheckInServiceRetro.class);
                serviceIntent.putExtra("inapp", true);
                serviceIntent.putExtra("handler", new Messenger(new CheckInHandler(this, serviceIntent, progressDialog)));
                startService(serviceIntent);
            }
        }
    }

    public static class CheckInHandler extends Handler {

        Activity activity;
        Intent serviceIntent;
        HookUpProgressDialog progressDialog;

        public CheckInHandler(Activity activity, Intent serviceIntent, HookUpProgressDialog progressDialog) {
            this.activity = activity;
            this.serviceIntent = serviceIntent;
            this.progressDialog = progressDialog;
        }

        @Override
        public void handleMessage(Message message) {
            int success = message.arg1;

            switch (success) {
                case 1:
                    Toast.makeText(activity, "Checked-in Succesfully", Toast.LENGTH_LONG).show();
                    break;

                case 0:
                    Toast.makeText(activity, "Are you really in class?", Toast.LENGTH_LONG).show();
                    break;
            }
            activity.stopService(serviceIntent);

            if(progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }
}
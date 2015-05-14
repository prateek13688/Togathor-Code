package com.uf.togathor.modules.attendance;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.uf.togathor.R;
import com.uf.togathor.adapters.AttendanceListAdapter;
import com.uf.togathor.db.couchdb.CouchDB;
import com.uf.togathor.db.couchdb.TogathorException;
import com.uf.togathor.db.couchdb.TogathorForbiddenException;
import com.uf.togathor.db.couchdb.model.Member;
import com.uf.togathor.management.SyncModule;
import com.uf.togathor.model.Message;
import com.uf.togathor.utils.constants.Const;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AttendanceListActivity extends ActionBarActivity {

    private List<String> studentsPresent;
    private List<Member> allStudents;
    private List<Message> messages;
    private String groupID;

    private ListView listViewOfStudents;
    private AttendanceListAdapter studentListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_list);

        studentsPresent = new ArrayList<>();
        messages = new ArrayList<>();
        listViewOfStudents = (ListView) findViewById(R.id.list_of_students);
        groupID = getIntent().getStringExtra("course");

        (new PrepareAttendanceSheet(this)).execute();

    }

    private class PrepareAttendanceSheet extends AsyncTask  {

        Activity activity;

        private PrepareAttendanceSheet(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected Object doInBackground(Object[] params) {

            try {
                messages = SyncModule.getAllMessagesFromServer();
                allStudents = CouchDB.findMembersByGroupId(groupID, 0, 200);
            } catch (IOException | JSONException | TogathorException | TogathorForbiddenException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {

            long currentTime = System.currentTimeMillis()/1000;
            long timePeriod = currentTime - 600;

            for(Message message : messages) {
                if(message.getMessageType().equals(Const.LOCATION) && (message.getCreated() > timePeriod)) {
                    studentsPresent.add(message.getFromUserId());
                }
            }

            studentListAdapter = new AttendanceListAdapter(activity, allStudents, studentsPresent);
            listViewOfStudents.setAdapter(studentListAdapter);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_attendance_list, menu);
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

        return super.onOptionsItemSelected(item);
    }
}

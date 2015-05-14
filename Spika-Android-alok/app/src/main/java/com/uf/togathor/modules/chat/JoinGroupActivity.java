package com.uf.togathor.modules.chat;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.uf.togathor.R;
import com.uf.togathor.adapters.GroupListAdapter;
import com.uf.togathor.db.couchdb.CouchDB;
import com.uf.togathor.db.couchdb.TogathorException;
import com.uf.togathor.db.couchdb.TogathorForbiddenException;
import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.uitems.HookUpProgressDialog;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class JoinGroupActivity extends ActionBarActivity {

    ListView groupListView;
    private GroupListAdapter mGroupListAdapter;
    List mGroups;

    EditText search;
    Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        groupListView = (ListView) findViewById(R.id.list_groups);
        search = (EditText) findViewById(R.id.search_text);
        searchButton = (Button) findViewById(R.id.search_button);

        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                    (new SearchGroupsAsync(JoinGroupActivity.this)).execute(v.getText().toString());
                    return true;
                }
                return false;
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new SearchGroupsAsync(JoinGroupActivity.this)).execute(search.getText().toString());
            }
        });

    }

    private class SearchGroupsAsync extends
            AsyncTask<String, Void, List<Group>> {

        Context context;

        protected SearchGroupsAsync(Context context) {
            this.context = context;
        }

        HookUpProgressDialog mProgressDialog = new HookUpProgressDialog(
                JoinGroupActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected List<Group> doInBackground(String... params) {

            try {
                return CouchDB.searchGroups(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (TogathorException e) {
                e.printStackTrace();
            } catch (TogathorForbiddenException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Group> result) {
            super.onPostExecute(result);
            if (result != null) {
                mGroups = new ArrayList<Group>(result);
            } else {
                mGroups = new ArrayList<Group>();
            }
//			mGroups = (ArrayList<Group>) result;

            groupListView.setVisibility(View.VISIBLE);

            // sorting groups by name
            Collections.sort(mGroups, new Comparator<Group>() {
                @Override
                public int compare(Group lhs, Group rhs) {
                    return lhs.getName().compareToIgnoreCase(rhs.getName());
                }
            });

            mGroupListAdapter = new GroupListAdapter(JoinGroupActivity.this, R.layout.groups_list_item_definition,
                    mGroups);
            groupListView.setAdapter(mGroupListAdapter);
            groupListView.setOnItemClickListener(mGroupListAdapter);
            mGroupListAdapter.notifyDataSetChanged();
            mProgressDialog.dismiss();
        }
    }


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_join_group, menu);
        return true;
    }*/

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
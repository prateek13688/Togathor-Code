package com.uf.togathor.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.uf.togathor.R;
import com.uf.togathor.adapters.GroupsListChatAdapter;
import com.uf.togathor.db.couchdb.CouchDB;
import com.uf.togathor.db.couchdb.TogathorException;
import com.uf.togathor.db.couchdb.TogathorForbiddenException;
import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.management.SyncModule;
import com.uf.togathor.uitems.HookUpProgressDialog;
import com.uf.togathor.management.UsersManagement;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GroupsViewFragment extends Fragment {

    private static final String TAG = "ChatFragment";

    private View rootView;

    // Layout Views
    private ListView mLvGroups;
    private List<Group> listOfGroups;
    private GroupsListChatAdapter mGroupListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_groups, container, false);

        mLvGroups = (ListView) rootView.findViewById(R.id.lvGroups);
        listOfGroups = SyncModule.getAllUserGroups();

        Collections.sort(listOfGroups, new Comparator<Group>() {
            @Override
            public int compare(Group lhs, Group rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });

        if (mGroupListAdapter == null) {
            mGroupListAdapter = new GroupsListChatAdapter(getActivity(), listOfGroups);
            mLvGroups.setAdapter(mGroupListAdapter);
            mLvGroups.setOnItemClickListener(mGroupListAdapter);
        } else {
            mGroupListAdapter.setItems(listOfGroups);
        }

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        listOfGroups.addAll(SyncModule.getAllUserGroups());
        mGroupListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        }
        return false;
    }
}
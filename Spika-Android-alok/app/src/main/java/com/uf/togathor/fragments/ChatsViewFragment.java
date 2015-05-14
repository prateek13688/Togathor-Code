/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.uf.togathor.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.dexafree.materialList.view.MaterialListView;
import com.uf.togathor.R;
import com.uf.togathor.adapters.UsersListChatAdapter;
import com.uf.togathor.db.couchdb.model.User;
import com.uf.togathor.management.SyncModule;
import com.uf.togathor.uitems.cards.CustomUserCard;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class ChatsViewFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "ChatFragment";

    private View rootView;
    private static MaterialListView userListView;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Layout Views
    private List<User> mUsers;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_chats, container, false);

        userListView = (MaterialListView) rootView.findViewById(R.id.user_list);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.user_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        mUsers = SyncModule.getAllUserContacts();

        // sorting users by name
        Collections.sort(mUsers, new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });

        for(User user : mUsers) {
            userListView.add(new CustomUserCard(R.layout.card_user_list_item, user));
        }

        return rootView;
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

    @Override
    public void onRefresh() {
        mUsers = SyncModule.getAllUserContacts();

        // sorting users by name
        Collections.sort(mUsers, new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });

        userListView.getAdapter().clear();

        for(User user : mUsers) {
            userListView.add(new CustomUserCard(R.layout.card_user_list_item, user));
        }

        swipeRefreshLayout.setRefreshing(false);
    }
}

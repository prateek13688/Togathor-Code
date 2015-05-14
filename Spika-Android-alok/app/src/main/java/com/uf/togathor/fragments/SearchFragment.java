package com.uf.togathor.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.uf.togathor.R;
import com.uf.togathor.Togathor;
import com.uf.togathor.adapters.UsersAdapter;
import com.uf.togathor.db.couchdb.CouchDB;
import com.uf.togathor.db.couchdb.ResultListener;
import com.uf.togathor.db.couchdb.model.User;
import com.uf.togathor.db.couchdb.model.UserSearch;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchFragment extends Fragment {

    View rootView;
    private ListView mLvUsers;
    private List<User> mUsers;
    private UsersAdapter mUserListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_explore,
                container, false);

        initialize();
        initUserSearch();

        return rootView;
    }

    private void initialize()   {
        mLvUsers = (ListView) rootView.findViewById(R.id.lvUsers);
    }

    private void initUserSearch() {

        final EditText etSearchName = (EditText) rootView.findViewById(R.id.etSearchName);
        etSearchName.setTypeface(Togathor.getTfMyriadPro());
        etSearchName.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    UserSearch userSearch = new UserSearch();
                    userSearch.setName(etSearchName.getText().toString());

                    etSearchName.setText("");

                    searchUsersAsync(userSearch);
                    return true;
                }
                return false;
            }
        });

    }

    private void searchUsersAsync (UserSearch userSearch) {
        CouchDB.searchUsersAsync(userSearch, new SearchUsersFinish(), getActivity(), true);
    }

    private class SearchUsersFinish implements ResultListener<List<User>> {

        @Override
        public void onResultsSucceeded(List<User> result) {
            if (result != null) {
                mUsers = result;

                //TODO
                // sorting users by name
                Collections.sort(mUsers, new Comparator<User>() {
                    @Override
                    public int compare(User lhs, User rhs) {
                        return lhs.getName().compareToIgnoreCase(rhs.getName());
                    }
                });

                if (mUserListAdapter == null) {
                    mUserListAdapter = new UsersAdapter(getActivity(),
                            mUsers);
                    mLvUsers.setAdapter(mUserListAdapter);
                    mLvUsers.setOnItemClickListener(mUserListAdapter);
                } else {
                    mUserListAdapter.setItems(mUsers);
                }
            }
        }

        @Override
        public void onResultsFail() {
        }
    }
}
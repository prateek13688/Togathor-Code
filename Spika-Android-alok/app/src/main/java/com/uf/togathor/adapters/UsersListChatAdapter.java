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

package com.uf.togathor.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uf.togathor.modules.chat.ChatUserActivity;
import com.uf.togathor.R;
import com.uf.togathor.db.couchdb.model.User;
import com.uf.togathor.management.UsersManagement;

import java.util.ArrayList;
import java.util.List;

/**
 * UsersAdapter
 * <p/>
 * Adapter class for users.
 */

public class UsersListChatAdapter extends BaseAdapter implements OnItemClickListener {

    private String TAG = "UsersChatAdapter";
    private List<User> mUsers = new ArrayList<>();
    private Activity mActivity;

    public UsersListChatAdapter(Activity activity, List<User> users) {
        mUsers = users;
        mActivity = activity;
    }

    public void setItems(List<User> users) {
        mUsers = users;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mUsers == null) {
            return 0;
        } else {
            return mUsers.size();
        }
    }

    @Override
    public User getItem(int position) {
        if (mUsers != null) {
            return mUsers.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder = null;
        try {

            if (v == null) {
                LayoutInflater li = (LayoutInflater) mActivity
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(R.layout.user_chat_item, parent, false);
                holder = new ViewHolder();
                holder.ivUserImage = (ImageView) v
                        .findViewById(R.id.ivUserImage);
                holder.tvUser = (TextView) v.findViewById(R.id.tvUser);
                holder.pbLoading = (ProgressBar) v
                        .findViewById(R.id.pbLoadingForImage);
                holder.pbLoading.setVisibility(View.VISIBLE);
                holder.cardView = (CardView) v.findViewById(R.id.card_view);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            User user = mUsers.get(position);
            holder.tvUser.setText(user.getName());

        } catch (Exception e) {
            Log.e(TAG, "error on inflating users");
        }

        return v;
    }

    class ViewHolder {
        public ImageView ivUserImage;
        public TextView tvUser;
        public ProgressBar pbLoading;
        public CardView cardView;
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        User user = (User) arg0.getItemAtPosition(arg2);
        Context context = arg0.getContext();

        Intent intent;

        UsersManagement.setToUser(user);
        UsersManagement.setToGroup(null);

        if (user != null) {
            intent = new Intent(context, ChatUserActivity.class);
            (context).startActivity(intent);
        }
    }
}

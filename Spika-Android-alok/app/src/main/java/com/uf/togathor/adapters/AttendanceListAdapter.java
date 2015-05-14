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
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uf.togathor.R;
import com.uf.togathor.db.couchdb.model.Member;

import java.util.ArrayList;
import java.util.List;

/**
 * UsersAdapter
 * 
 * Adapter class for users.
 */

public class AttendanceListAdapter extends BaseAdapter {

	private String TAG = "UsersAdapter";
	private List<Member> mUsers = new ArrayList<>();
	private Activity mActivity;
    private List<String> mUsersPresent = new ArrayList<>();


    public AttendanceListAdapter(Activity activity, List<Member> users, List<String> usersPresent) {
        mUsers = users;
        mUsersPresent = usersPresent;
        mActivity = activity;
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
	public Member getItem(int position) {
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
				v = li.inflate(R.layout.attendance_list_item, parent, false);
				holder = new ViewHolder();
				holder.studentName = (TextView) v.findViewById(R.id.tvUser);
                holder.studentStatus = (ImageView) v.findViewById(R.id.student_status);
                holder.cardView = (CardView) v.findViewById(R.id.card_view);
				v.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Member user = mUsers.get(position);
			holder.studentName.setText(user.getName());

            if(mUsersPresent.contains(user.getId()))
                holder.studentStatus.setBackgroundColor(Color.GREEN);
            else
                holder.studentStatus.setBackgroundColor(Color.RED);


		} catch (Exception e) {
			Log.e(TAG, "error on inflating users");
		}

		return v;
	}

	class ViewHolder {
		public TextView studentName;
        public ImageView studentStatus;
        public CardView cardView;
	}
}

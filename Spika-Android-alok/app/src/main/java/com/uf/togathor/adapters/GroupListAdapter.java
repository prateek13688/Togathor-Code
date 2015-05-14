package com.uf.togathor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.uf.togathor.R;
import com.uf.togathor.db.couchdb.CouchDB;
import com.uf.togathor.db.couchdb.ResultListener;
import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.management.SyncModule;
import com.uf.togathor.management.UsersManagement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alok on 1/22/2015.
 */
public class GroupListAdapter extends ArrayAdapter<Group> implements AdapterView.OnItemClickListener {

    private List<Group> mGroups = new ArrayList<>();
    private Context context;
    private int resource;

    public GroupListAdapter(Context context, int resource, List<Group> groups) {
        super(context, resource, groups);
        this.context = context;
        this.mGroups = groups;
        this.resource = resource;
    }

    @Override
    public int getCount() {
        return mGroups.size();
    }

    @Override
    public Group getItem(int position) {
        return mGroups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

        ViewHolder holder = null;

        try {

            if (v == null) {

                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(resource, null);
                holder = new ViewHolder();
                holder.tvGroup = (TextView) v.findViewById(R.id.tvGroup);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            Group group = mGroups.get(position);
            holder.tvGroup.setText(group.getName());

        } catch (Exception e) {
        }

        return v;
    }

    class ViewHolder {
        public TextView tvGroup;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SyncModule.addGroupContact(mGroups.get(position).getId(),
                UsersManagement.getLoginUser().getId(), context);
    }
}

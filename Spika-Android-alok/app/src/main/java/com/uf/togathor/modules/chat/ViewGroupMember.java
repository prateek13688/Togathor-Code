package com.uf.togathor.modules.chat;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uf.togathor.R;
import com.uf.togathor.adapters.MembersAdapter;
import com.uf.togathor.db.couchdb.model.Member;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ViewGroupMember extends ActionBarActivity {

    private ListView mList;
    private List<Member> memberList;
    private MembersAdapter mAdapter;
    private TextView groupName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_group_member);
        Intent intent = getIntent();
        String memList = intent.getStringExtra("memberList");
        String group = intent.getStringExtra("groupName");
        memberList = new Gson().fromJson(memList, new TypeToken<Member>(){}.getType());
        //if(memberList == null)
        //memberList = adddummyMembers();
        groupName = (TextView) findViewById(R.id.to_group);
        groupName.setText(group);
        mList = (ListView) findViewById(R.id.lvMembers);
        Collections.sort(memberList, new Comparator<Member>() {
            @Override
            public int compare(Member lhs, Member rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });
        if(mAdapter == null)
        {
            mAdapter = new MembersAdapter(ViewGroupMember.this, R.layout.view_members_list, new ArrayList<Member>(memberList));
            mList.setAdapter(mAdapter);
            mList.setOnItemClickListener(mAdapter);
        }
        else
            mAdapter.addAll(new ArrayList<Member>(memberList));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_group_member, menu);
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
    private List<Member> adddummyMembers()
    {
        List<Member> mList = new ArrayList<Member>();
        Member m1 = new Member("121", "Bryan", null, "online");
        Member m2 = new Member("122" , "Ryan", null, "away");
        mList.add(m1);
        mList.add(m2);
        return mList;
    }
}

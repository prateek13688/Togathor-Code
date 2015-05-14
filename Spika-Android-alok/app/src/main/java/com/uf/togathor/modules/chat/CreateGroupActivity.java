package com.uf.togathor.modules.chat;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.uf.togathor.R;
import com.uf.togathor.Togathor;
import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.management.SyncModule;
import com.uf.togathor.management.UsersManagement;
import com.uf.togathor.model.GroupCategory;
import com.uf.togathor.utils.Utils;

public class CreateGroupActivity extends ActionBarActivity {

    EditText name;
    EditText category;
    EditText description;
    EditText password;
    Button create;

    GroupCategory defaultGroupCat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        name = (EditText) findViewById(R.id.group_name);
        category = (EditText) findViewById(R.id.group_cat);
        description = (EditText) findViewById(R.id.group_desc);
        password = (EditText) findViewById(R.id.group_pass);
        create = (Button) findViewById(R.id.create_group);

        defaultGroupCat = new GroupCategory();
        defaultGroupCat.setId("10");
        defaultGroupCat.setTitle("Default Group");

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Group newGroup = new Group();

                newGroup.setName(name.getText().toString());
                newGroup.setCategoryName(defaultGroupCat.getTitle());
                newGroup.setCategoryId(defaultGroupCat.getId());
                newGroup.setDescription(description.getText().toString());
                newGroup.setPassword(password.getText().toString());
                Utils.createGroup(CreateGroupActivity.this, newGroup, true);
            }
        });

    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_group, menu);
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

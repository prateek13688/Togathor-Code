package com.uf.togathor;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;
import com.uf.togathor.adapters.LeftNavBarListAdapter;
import com.uf.togathor.fragments.ChatsViewFragment;
import com.uf.togathor.fragments.GroupsViewFragment;
import com.uf.togathor.fragments.SearchFragment;
import com.uf.togathor.management.LogoutReceiver;
import com.uf.togathor.management.UsersManagement;
import com.uf.togathor.modules.chat.CreateGroupActivity;
import com.uf.togathor.modules.chat.JoinGroupActivity;
import com.uf.togathor.modules.timeline.TimelineActivity;
import com.uf.togathor.uitems.CircularImageView;
import com.uf.togathor.uitems.HookUpDialog;
import com.uf.togathor.utils.ble.BLEDeviceListActivity;

import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;

/**
 * Created by Alok on 12/10/2014.
 */
public class HomeActivity extends ActionBarActivity implements MaterialTabListener {

    private final static int CONTACTS = 0;
    private final static int BLE = 1;
    private final static int TIMELINE = 2;
    private final static int IMAGE_RECOGNITION = 3;
    private final static int USER_SUPPORT = 4;
    private final static int LOGOUT = 5;
    private static final String TAG = "HomeActivity";

    private ViewPager viewPager;
    private MaterialTabHost materialTabHost;
    private ViewPagerAdapter viewPagerAdapter;

    private LinearLayout navBarLeft;
    private ImageView userCover;
    private TextView userName;
    private TextView userEmail;
    private CircularImageView userImage;
    private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private ListView mLeftDrawerList;
    private String[] mLeftNavBarTextItems = {"Contacts", "BLE", "Timeline", "Image Recognition", "User Support", "Logout"};
    private int[] mLeftNavBarIcons = {R.drawable.ic_group_black_24dp, R.drawable.ic_view_carousel_black_24dp,
            R.drawable.ic_info_black_24dp, R.drawable.ic_settings_black_24dp,
            R.drawable.ic_view_carousel_black_24dp, R.drawable.logout_icon1};

    HookUpDialog mLogoutDialog = null;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getResources().getBoolean(R.bool.isLollipop)) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Togathor.getGoogleAPIService().registerGCM();

        mTitle = getTitle();
        navBarLeft = (LinearLayout) findViewById(R.id.navBarLeft);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        userCover = (ImageView) findViewById(R.id.userCover);
        userName = (TextView) findViewById(R.id.userName);
        userEmail = (TextView) findViewById(R.id.userEmail);
        materialTabHost = (MaterialTabHost) findViewById(R.id.material_tab_host);
        viewPager = (ViewPager) findViewById(R.id.view_pager);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // when user do a swipe the selected tab change
                materialTabHost.setSelectedNavigationItem(position);
            }
        });

        // insert all tabs from pagerAdapter data
        for (int i = 0; i < viewPagerAdapter.getCount(); i++) {
            materialTabHost.addTab(
                    materialTabHost.newTab()
                            .setText(viewPagerAdapter.getPageTitle(i))
                            .setTabListener(this)
            );
        }

        //Set the userName & userEmail from login credentials. Checking to make sure it is not null otherwise take default.
        if (UsersManagement.getLoginUser() != null) {
            userName.setText(UsersManagement.getLoginUser().getName());
            userEmail.setText(UsersManagement.getLoginUser().getEmail());
            //userImage.setImageResource(UsersManagement.getLoginUser()); //Need to work on this latter for setting image photo.
        }


        mDrawerToggle = new CustomActionBarDrawerToggle(
                this, mDrawerLayout, toolbar
        );

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getResources().getBoolean(R.bool.isLollipop))
            toolbar.setElevation(10.0f);
        userCover.setImageResource(R.drawable.profile_back);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mLeftDrawerList = (ListView) findViewById(R.id.left_drawer);
        mLeftDrawerList.setAdapter(new LeftNavBarListAdapter(this,
                R.layout.left_drawer_list_item_definition,
                mLeftNavBarTextItems, mLeftNavBarIcons));
        mLeftDrawerList
                .setOnItemClickListener(new LeftDrawerItemClickListener());

        selectItem(0);
    }

    @Override
    public void onTabSelected(MaterialTab materialTab) {
        viewPager.setCurrentItem(materialTab.getPosition());
    }

    @Override
    public void onTabReselected(MaterialTab materialTab) {

    }

    @Override
    public void onTabUnselected(MaterialTab materialTab) {

    }

    // Implement Left Navigation Bar Interaction
    private class LeftDrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            selectItem(position);
            mDrawerLayout.closeDrawer(navBarLeft);
        }
    }

    private void selectItem(int position) {

        switch (position) {

            case CONTACTS:
                viewPager.setCurrentItem(0, true);
                return;

            case BLE:
                Intent intent = new Intent(HomeActivity.this, BLEDeviceListActivity.class);
                startActivity(intent);
                break;

            case TIMELINE:
                startActivity(new Intent(this, TimelineActivity.class));
                break;

            case IMAGE_RECOGNITION:
                startActivity(new Intent(this, ImageRecognitionActivity.class));
                break;

            case LOGOUT:
                logout();
                break;

            default:
                break;
        }
    }

    private class CustomActionBarDrawerToggle extends ActionBarDrawerToggle {

        public CustomActionBarDrawerToggle(Activity mActivity,
                                           DrawerLayout mDrawerLayout, Toolbar toolbar) {
            super(mActivity, mDrawerLayout, toolbar,
                    R.string.MAIN_MENU, R.string.MAIN_MENU);
        }

        @Override
        public void onDrawerClosed(View view) {
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after on`RestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_create_group:
                startActivity(new Intent(this, CreateGroupActivity.class));
                break;

            case R.id.menu_join_group:
                startActivity(new Intent(this, JoinGroupActivity.class));
                break;
            default:
                break;
        }

        return (mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item));
    }

    //logout function..
    public void logout() {

        mLogoutDialog = new HookUpDialog(this);
        mLogoutDialog.setMessage(getString(R.string.logout_message));
        mLogoutDialog.setOnButtonClickListener(HookUpDialog.BUTTON_OK,

                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        appLogout(false, false, false);
                    }

                });
        mLogoutDialog.setOnButtonClickListener(HookUpDialog.BUTTON_CANCEL,
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mLogoutDialog.dismiss();
                        //SideBarActivity.this.closeSideBar();
                    }

                });

        mLogoutDialog.show();

    }

    public void appLogout(boolean isUserUpdateConflict, boolean isServerError, boolean isInvalidToken) {
        //Activity fromActivity = SideBarActivity.getValidContext();

        //Intent goToSignIn = new Intent(fromActivity, SignInActivity.class);
        Intent goToSignIn = new Intent(this, SignInActivity.class);

        if (isServerError) {
            goToSignIn.putExtra("password_from_prefs", Togathor.getPreferences().getUserPassword());
            goToSignIn.putExtra("email_from_prefs", Togathor.getPreferences().getUserEmail());
        }
        if (isInvalidToken) {
            goToSignIn.putExtra("invalid_token", true);
        }

        GCMRegistrar.unregister(Togathor.getInstance().getApplicationContext());

        Togathor.getPreferences().setWatchingGroupId("");
        Togathor.getPreferences().setWatchingGroupRev("");

        //if (SideBarActivity.sInstance != null) {
        goToSignIn.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        //    SideBarActivity.sInstance.startActivity(goToSignIn);

        //sendBroadcastLogout(SideBarActivity.sInstance);
        sendBroadcastLogout(this);
        //} else if (SplashScreenActivity.sInstance != null) {
        if (SplashScreenActivity.sInstance != null) {
            SplashScreenActivity.sInstance.startActivity(goToSignIn);

            sendBroadcastLogout(SplashScreenActivity.sInstance);
        }
    }

    private static void sendBroadcastLogout(Context context) {
        /*
         * Send logout broadcast
		 *
		 */
        Intent intent = new Intent();
        intent.setAction(LogoutReceiver.LOGOUT);
        context.sendBroadcast(intent);
        intent = null;
        // End: Send logout broadcast
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        String [] titles = {"Users", "Groups", "Find Users"};

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ChatsViewFragment();
                case 1:
                    return new GroupsViewFragment();
                case 2:
                    return new SearchFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}

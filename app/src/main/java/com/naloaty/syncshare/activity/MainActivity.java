package com.naloaty.syncshare.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.SSActivity;
import com.naloaty.syncshare.service.CommunicationService;
import com.naloaty.syncshare.util.AppUtils;

import java.lang.reflect.Method;

public class MainActivity extends SSActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private MenuItem mSelectedDrawerItem;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolBar = findViewById(R.id.activity_main_toolbar);
        enableOptionMenuIcons(toolBar);

        setSupportActionBar(toolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setUpNavigationDrawer();

    }

    /*
     * Hack to display option menu icons
     * Copied from https://stackoverflow.com/questions/30076392/how-does-this-strange-condition-happens-when-show-menu-item-icon-in-toolbar-over/30337653#30337653
     */

    @SuppressLint("RestrictedApi")
    public void enableOptionMenuIcons(Toolbar toolbar){
        MenuBuilder menuBuilder = (MenuBuilder) toolbar.getMenu();
        menuBuilder.setOptionalIconsVisible(true);
    }

    private void setUpNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.activity_main_drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        //See onNavigationItemSelected();
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                doDrawerAction();
            }
        });

        mNavigationView = findViewById(R.id.activity_main_nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        //We want to do action after drawer is completely closed (for better user experience)
        //See doDrawerAction()

        //Used by doDrawerAction()
        mSelectedDrawerItem = item;

        if (mDrawerLayout != null)
            mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void doDrawerAction() {

        if (mSelectedDrawerItem == null)
            return;

        switch (mSelectedDrawerItem.getItemId()) {

            case R.id.menu_main_manage_devices:
                startActivity(new Intent(this, DeviceManageActivity.class));
                mSelectedDrawerItem = null;
                break;

            case R.id.menu_main_preferences:
                break;

            case R.id.menu_main_about:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        mDrawerToggle.onOptionsItemSelected(item);

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_toggle_sharing:
                toggleCommunicationService();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.toolbar_menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem serviceToggle = menu.findItem(R.id.action_toggle_sharing);

        /*boolean serviceRunning = getDefaultSharedPreferences()
                .getBoolean(CommunicationService.PREFERNCE_SERVICE_RUNNING, false);*/

        boolean serviceRunning = AppUtils.isServiceRunning(getApplication(), CommunicationService.class);

        if (serviceRunning) {
            Log.d(TAG, "Service running");
            serviceToggle.setTitle(R.string.menu_stopSharing);
            serviceToggle.setIcon(R.drawable.ic_pause_24dp);
        }
        else
        {
            Log.d(TAG, "Service NOT running");
            serviceToggle.setTitle(R.string.menu_startSharing);
            serviceToggle.setIcon(R.drawable.ic_play_arrow_24dp);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private void toggleCommunicationService() {

        boolean serviceRunning = AppUtils.isServiceRunning(getApplication(), CommunicationService.class);

        if (serviceRunning) {
            Log.d(TAG, "Stopping CommunicationService");

            Intent intent = new Intent(this, CommunicationService.class);
            intent.setAction(CommunicationService.ACTION_STOP_SHARING);

            startService(intent);
        }
        else
        {
            Log.d(TAG, "Starting CommunicationService");

            Intent intent = new Intent(this, CommunicationService.class);
            //TODO: kostyl
            intent.setAction("kostyl");

            startService(intent);
        }


    }

}

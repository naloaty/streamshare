package com.naloaty.syncshare.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
    private MenuItem mServiceToggle;

    private final IntentFilter mFilter = new IntentFilter();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(CommunicationService.SERVICE_STATE_CHANGED)) {
                setServiceState(intent.getBooleanExtra(CommunicationService.EXTRA_SERVICE_SATE, false));
            }
        }
    };

    private void setServiceState(boolean serviceStarted) {

        if (mServiceToggle == null)
            return;

        if (serviceStarted) {
            mServiceToggle.setTitle(R.string.menu_stopSharing);
            mServiceToggle.setIcon(R.drawable.ic_service_off_24dp);
        }
        else
        {
            mServiceToggle.setTitle(R.string.menu_startSharing);
            mServiceToggle.setIcon(R.drawable.ic_service_on_24dp);
        }

    }


    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolBar = findViewById(R.id.activity_main_toolbar);
        //enableOptionMenuIcons(toolBar);

        setSupportActionBar(toolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setUpNavigationDrawer();

        mFilter.addAction(CommunicationService.SERVICE_STATE_CHANGED);

    }

    /*
     * Hack to display option menu icons
     * TODO: not used anymore
     * Copied from https://stackoverflow.com/questions/30076392/how-does-this-strange-condition-happens-when-show-menu-item-icon-in-toolbar-over/30337653#30337653
     */

    /*@SuppressLint("RestrictedApi")
    public void enableOptionMenuIcons(Toolbar toolbar){
        MenuBuilder menuBuilder = (MenuBuilder) toolbar.getMenu();
        menuBuilder.setOptionalIconsVisible(true);
    }*/

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

            /*case R.id.menu_main_preferences:
                break;

            case R.id.menu_main_about:
                break;*/
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

            case R.id.action_open_device_info:
                startActivity(new Intent(this, AddDeviceActivity.class));
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
    protected void onResume() {
        setServiceState(AppUtils.isServiceRunning(this, CommunicationService.class));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.toolbar_menu_main, menu);

        mServiceToggle = menu.findItem(R.id.action_toggle_sharing);
        return true;
    }

    /*@Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem serviceToggle = menu.findItem(R.id.action_toggle_sharing);

        boolean serviceRunning = AppUtils.isServiceRunning(getApplication(), CommunicationService.class);

        if (serviceRunning) {
            Log.d(TAG, "Service running");
            serviceToggle.setTitle(R.string.menu_stopSharing);
            serviceToggle.setIcon(R.drawable.ic_service_off_24dp);
        }
        else
        {
            Log.d(TAG, "Service NOT running");
            serviceToggle.setTitle(R.string.menu_startSharing);
            serviceToggle.setIcon(R.drawable.ic_service_on_24dp);
        }
        return super.onPrepareOptionsMenu(menu);
    }*/

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
            startService(intent);
        }


    }

}

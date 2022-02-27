package com.naloaty.syncshare.activity;

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
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.navigation.NavigationView;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.SSActivity;
import com.naloaty.syncshare.service.CommunicationService;
import com.naloaty.syncshare.util.AppUtils;


/**
 * This activity represents the main screen, that displays the current devices on the network (trusted devices only).
 * NOTE: LocalAlbumsActivity is not used on tablets. In this case, LocalAlbumsFragment is located on MainActivity.
 *
 * Related fragments:
 * @see com.naloaty.syncshare.fragment.MainFragment
 * @see com.naloaty.syncshare.fragment.LocalAlbumsFragment
 */
public class MainActivity extends SSActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private final IntentFilter mFilter = new IntentFilter();
    private boolean mServiceRunning = false;

    /* UI elements */
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private MenuItem mSelectedDrawerItem;
    private MenuItem mServiceToggle;


    /**
     * Receives a broadcast about CommunicationService state changes
     * @see #setServiceState(boolean)
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (CommunicationService.SERVICE_STATE_CHANGED.equals(action)) {
                setServiceState(intent.getBooleanExtra(CommunicationService.EXTRA_SERVICE_SATE, false));
            }
        }
    };

    /**
     * Sets the state of UI depending on the state of CommunicationService
     * @param serviceRunning CommunicationService state (running or not)
     */
    private void setServiceState(boolean serviceRunning) {
        mServiceRunning = serviceRunning;

        if (mServiceToggle == null)
            return;

        if (serviceRunning) {
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
        setSupportActionBar(toolBar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        else
            Log.w(TAG, "Toolbar is not properly initialized");

        setUpNavigationDrawer();

        mFilter.addAction(CommunicationService.SERVICE_STATE_CHANGED);
    }


    /**
     * Initializes NavigationDrawer
     */
    private void setUpNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            //Called when a drawer has settled in a completely opened state.
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            //Called when a drawer has settled in a completely closed state.
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

        NavigationView mNavigationView = findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Postpones NavigationDrawer action.
     * Action is doing after NavigationDrawer is completely closed (for better user experience).
     * @see #doDrawerAction()
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //Used by doDrawerAction()
        mSelectedDrawerItem = item;

        if (mDrawerLayout != null)
            mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    /**
     * Performs a postponed NavigationDrawer action.
     */
    private void doDrawerAction() {
        if (mSelectedDrawerItem == null)
            return;

        switch (mSelectedDrawerItem.getItemId()) {

            case R.id.menu_main_manage_devices:
                startActivity(new Intent(this, DeviceManageActivity.class));

                break;

            case R.id.menu_main_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }

        mSelectedDrawerItem = null;
    }

    /**
     * Handles NavigationDrawer item selection.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        mDrawerToggle.onOptionsItemSelected(item);

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
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setServiceState(AppUtils.isServiceRunning(this, CommunicationService.class));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_main, menu);

        mServiceToggle = menu.findItem(R.id.action_toggle_sharing);

        if (mServiceRunning) {
            mServiceToggle.setTitle(R.string.menu_stopSharing);
            mServiceToggle.setIcon(R.drawable.ic_service_off_24dp);
        }
        else
        {
            mServiceToggle.setTitle(R.string.menu_startSharing);
            mServiceToggle.setIcon(R.drawable.ic_service_on_24dp);
        }

        return true;
    }

    /**
     * Toggles CommunicationService state (running or not)
     */
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

package com.naloaty.syncshare.activity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.SSActivity;
import com.naloaty.syncshare.fragment.ConnectionInfoFragment;
import com.naloaty.syncshare.fragment.PairFragment;
import com.naloaty.syncshare.fragment.PairOptionsFragment;

public class PairDeviceActivity extends SSActivity {

    private static final String TAG = "PairDeviceActivity";

    public static final String ACTION_CHANGE_FRAGMENT = "com.naloaty.intent.action.PAIR_DEVICE_CHANGE_FRAGMENT";
    public static final String EXTRA_TARGET_FRAGMENT = "targetFragment";

    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mToolBarLayout;
    private Toolbar mToolBar;

    private PairOptionsFragment mPairOptionsFragment;
    private ConnectionInfoFragment mConnectionInfoFragment ;

    private final IntentFilter mFilter = new IntentFilter();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (ACTION_CHANGE_FRAGMENT.equals(intent.getAction())
                && intent.hasExtra(EXTRA_TARGET_FRAGMENT)) {

                //PairFragment is enum
                PairFragment targetFragment = PairFragment.valueOf(intent.getStringExtra(EXTRA_TARGET_FRAGMENT));

                switch (targetFragment){

                    case EnterIP:
                        //show enter dialog
                        break;

                    default:
                        setFragment(targetFragment);
                }


            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_pair);

        //======== Init collapsing toolbar ==========
        mAppBarLayout = findViewById(R.id.pair_device_app_bar_layout);
        mToolBarLayout = findViewById(R.id.pair_device_toolbar_layout);
        mToolBar = findViewById(R.id.pair_device_toolbar);

        mToolBarLayout.setTitle(getString(R.string.title_pairDevice));
        mAppBarLayout.setExpanded(true, true);

        //Important to call this BEFORE setNavigationOnClickListener()
        setSupportActionBar(mToolBar);

        //To make "close" animation (this instead of using "parent activity")
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_24dp);
        getSupportActionBar().setHomeButtonEnabled(true);

        //======== Init fragments =========
        mPairOptionsFragment = new PairOptionsFragment();
        mConnectionInfoFragment = new ConnectionInfoFragment();

        setFragment(PairFragment.Options);

        //======= Init broadcast =========
        mFilter.addAction(ACTION_CHANGE_FRAGMENT);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mReceiver);
    }

    @Override
    public void onBackPressed() {

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.pair_device_fragment_placeholder);

        if (currentFragment instanceof PairOptionsFragment)
            super.onBackPressed();
        else
            setFragment(PairFragment.Options);
    }

    private void setFragment(PairFragment targetFragment) {

        Fragment candidate = null;

        switch (targetFragment) {
            case Options:
                candidate = mPairOptionsFragment;
                break;

            case ConnectionInfo:
                candidate = mConnectionInfoFragment;
                break;

            case ScanQR:
                //TODO

        }

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.pair_device_fragment_placeholder);

        if (currentFragment == null ||  currentFragment != candidate) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (currentFragment != null)
                transaction.remove(currentFragment);

            if (currentFragment != null && candidate instanceof PairOptionsFragment)
                transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
            else
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);

            transaction.add(R.id.pair_device_fragment_placeholder, candidate);
            transaction.commit();

            setToolbar(targetFragment);
        }
    }

    private void setToolbar(PairFragment fragment) {

        int titleResource;
        boolean isOptions = false;

        switch (fragment) {
            case Options:
                titleResource = R.string.title_pairDevice;
                isOptions = true;
                break;

            case ConnectionInfo:
                titleResource = R.string.title_connectionInfo;
                break;

            default:
                titleResource = R.string.title_pairDevice;
        }

        if (fragment.equals(PairFragment.Options))
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_24dp);
        else
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp);

        mToolBarLayout.setTitle(getString(titleResource));
        mAppBarLayout.setExpanded(isOptions, true);
    }
}

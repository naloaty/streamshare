package com.naloaty.syncshare.activity;


import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.SSActivity;
import com.naloaty.syncshare.fragment.PairOptionsFragment;

public class PairDeviceActivity extends SSActivity {

    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mToolBarLayout;
    private Toolbar mToolBar;

    private PairOptionsFragment mPairOptionsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_pair);

        mAppBarLayout = findViewById(R.id.pair_device_app_bar_layout);
        mToolBarLayout = findViewById(R.id.pair_device_toolbar_layout);
        mToolBar = findViewById(R.id.pair_device_toolbar);

        mToolBarLayout.setTitle(getString(R.string.text_pairDevice));
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

        FragmentFactory factory = getSupportFragmentManager().getFragmentFactory();

        mPairOptionsFragment = new PairOptionsFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.pair_device_fragment_placeholder, mPairOptionsFragment);
        transaction.commit();

    }

}

package com.naloaty.syncshare.activity;

import com.naloaty.syncshare.app.SSActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.naloaty.syncshare.R;

public class LocalDeviceActivity extends SSActivity {

    /* Collapsing toolbar */
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mToolBarLayout;
    private Toolbar mToolBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_device);

        /* Collapsing toolbar */
        mAppBarLayout = findViewById(R.id.local_device_app_bar_layout);
        mToolBarLayout = findViewById(R.id.local_device_toolbar_layout);
        mToolBar = findViewById(R.id.local_device_toolbar);

        mToolBarLayout.setTitle(getString(R.string.text_localDevice));
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
        getSupportActionBar().setHomeButtonEnabled(true);

    }
}

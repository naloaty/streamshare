package com.naloaty.syncshare.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.SSActivity;
import com.naloaty.syncshare.util.AppUtils;
import com.naloaty.syncshare.util.PermissionHelper;
import com.naloaty.syncshare.widget.DynamicViewPagerAdapter;

/**
 * This activity shows information about the app.
 * @see SSActivity
 */
public class AboutActivity extends SSActivity {

    private static final String TAG = "AboutActivity";

    /* UI elements */
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mToolBarLayout;
    private Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        mAppBarLayout = findViewById(R.id.app_bar_layout);
        mToolBarLayout = findViewById(R.id.toolbar_layout);
        mToolBar = findViewById(R.id.toolbar);

        if (mToolBarLayout != null)
            mToolBarLayout.setTitle(getString(R.string.menu_about));
        else
            mToolBar.setTitle(R.string.menu_about);

        if (mAppBarLayout != null)
            mAppBarLayout.setExpanded(true, true);

        //Important to call this BEFORE setNavigationOnClickListener()
        setSupportActionBar(mToolBar);

        //To make "close" animation (this instead of using "parent activity")
        mToolBar.setNavigationOnClickListener(v -> onBackPressed());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_24dp);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        else
            Log.w(TAG, "Toolbar is not properly initialized");
    }
}

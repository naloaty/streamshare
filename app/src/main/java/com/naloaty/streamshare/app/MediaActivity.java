package com.naloaty.streamshare.app;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.naloaty.streamshare.R;
import com.naloaty.streamshare.util.AppUtils;

/**
 * This class helps manage the visibility of the System UI (such as status bar and navigation bar)
 * @see com.naloaty.streamshare.activity.ImageViewActivity
 */
public abstract class MediaActivity extends SSActivity {

    private static final String TAG = "MediaActivity";

    private boolean fullScreenMode = false;

    /* UI elements */
    private RelativeLayout mRootLayout;
    private Toolbar mToolbar;
    private AppBarLayout mAppbarLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbar = findViewById(R.id.toolbar);
        mRootLayout = findViewById(R.id.root_layout);
        mAppbarLayout = findViewById(R.id.appbar_layout);

        mAppbarLayout.setOutlineProvider(null);
        mToolbar.setTitle("");

        setupSystemUI();

        //Important to call this BEFORE setNavigationOnClickListener()
        setSupportActionBar(mToolbar);

        //To make "close" animation (this instead of using "parent activity")
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        else
            Log.w(TAG, "Toolbar is not properly initialized");
    }

    /**
     * Sets toolbar title
     * @param title Required toolbar title
     */
    protected void setTitle(String title) {
        mToolbar.setTitle(title);
    }

    /**
     * Toggles the System UI visibility
     */
    protected void toggleSystemUI() {
        if (fullScreenMode)
            showSystemUI();
        else
            hideSystemUI();
    }

    /**
     * Hides toolbar, status bar and navigation bar
     */
    private void hideSystemUI() {
        runOnUiThread(new Runnable() {
            public void run() {
                mAppbarLayout.animate()
                        .translationY(-mAppbarLayout.getHeight())
                        .setInterpolator(new AccelerateInterpolator())
                        .setDuration(200)
                        .start();

                getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> Log.d(TAG, "ui changed: " + visibility));

                getWindow().getDecorView().setSystemUiVisibility(
                                  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);

                fullScreenMode = true;
            }
        });
    }


    /**
     * Shows toolbar, status bar and navigation bar
     */
    private void showSystemUI() {
        runOnUiThread(new Runnable() {
            public void run() {
                mAppbarLayout.animate()
                        .translationY(AppUtils.getStatusBarHeight(getResources()))
                        .setInterpolator(new DecelerateInterpolator())
                        .setDuration(240)
                        .start();

                getWindow().getDecorView().setSystemUiVisibility(
                                  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                fullScreenMode = false;
            }
        });
    }

    /**
     * Sets the System UI to default state (all visible)
     */
    private void setupSystemUI() {
        mAppbarLayout.animate()
                .translationY(AppUtils.getStatusBarHeight(getResources()))
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(240).start();

        getWindow().getDecorView().setSystemUiVisibility(
                          View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


    /**
     * Smoothly changes the background of activity from white to black and vice versa
     */
    private void changeBackGroundColor() {
        int colorTo;
        int colorFrom;

        if (fullScreenMode) {
            colorFrom = ContextCompat.getColor(this, R.color.windowBackground);
            colorTo = ContextCompat.getColor(this, R.color.colorBlack);
        }
        else
        {
            colorFrom = ContextCompat.getColor(this, R.color.colorBlack);
            colorTo = ContextCompat.getColor(this, R.color.windowBackground);
        }

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(240);

        colorAnimation.addUpdateListener(animator -> mRootLayout.setBackgroundColor((Integer) animator.getAnimatedValue()));

        colorAnimation.start();
    }
}

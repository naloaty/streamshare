package com.naloaty.syncshare.app;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.util.AppUtils;

public class MediaActivity extends SSActivity {

    private static final String TAG = "MediaActivity";

    /*
     * System UI state machine
     * Borrowed from LeafPic
     * https://github.com/HoraApps/LeafPic
     */

    private RelativeLayout mRootLayout;
    private Toolbar mToolbar;
    private AppBarLayout mAppbarLayout;
    private boolean fullScreenMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbar = findViewById(R.id.toolbar);
        mRootLayout = findViewById(R.id.root_layout);
        mAppbarLayout = findViewById(R.id.appbar_layout);

        setupSystemUI();

        mToolbar.setTitle("");

        //Important to call this BEFORE setNavigationOnClickListener()
        setSupportActionBar(mToolbar);

        //To make "close" animation (this instead of using "parent activity")
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    protected void setTitle(String title) {
        mToolbar.setTitle(title);
    }

    public void toggleSystemUI() {
        if (fullScreenMode)
            showSystemUI();
        else
            hideSystemUI();
    }

    /*
     * Runnable required because user
     * can close activity before animation end
     */
    private void hideSystemUI() {
        runOnUiThread(new Runnable() {
            public void run() {
                mAppbarLayout.animate()
                        .translationY(-mAppbarLayout.getHeight())
                        .setInterpolator(new AccelerateInterpolator())
                        .setDuration(200)
                        .start();

                getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        Log.d(TAG, "ui changed: " + visibility);
                    }
                });

                getWindow().getDecorView().setSystemUiVisibility(
                                  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);

                fullScreenMode = true;
                changeBackGroundColor();
            }
        });
    }


    /*
     * Runnable required because user
     * can close activity before animation end
     */
    private void showSystemUI() {
        runOnUiThread(new Runnable() {
            public void run() {
                mAppbarLayout.animate()
                        .translationY(0)
                        .setInterpolator(new DecelerateInterpolator())
                        .setDuration(240)
                        .start();

                getWindow().getDecorView().setSystemUiVisibility(
                                  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN);

                fullScreenMode = false;
                changeBackGroundColor();
            }
        });
    }

    private void setupSystemUI() {
        mAppbarLayout.animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(240).start();

        getWindow().getDecorView().setSystemUiVisibility(
                          View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        /*getWindow().getDecorView().setOnSystemUiVisibilityChangeListener
                (visibility -> {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) showSystemUI();
                    else hideSystemUI();
                });*/

    }


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

        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mRootLayout.setBackgroundColor((Integer) animator.getAnimatedValue());
            }
        });

        colorAnimation.start();
    }
}

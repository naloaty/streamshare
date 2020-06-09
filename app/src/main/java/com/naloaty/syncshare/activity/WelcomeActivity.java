package com.naloaty.syncshare.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.SSActivity;
import com.naloaty.syncshare.util.AppUtils;
import com.naloaty.syncshare.util.PermissionHelper;
import com.naloaty.syncshare.widget.DynamicViewPagerAdapter;

/**
 * This activity welcomes the user the first time the application is launched.
 */
public class WelcomeActivity extends SSActivity {

    /* UI elements */
    private ViewGroup mSplashView;
    private ViewGroup mPermissionsView;
    private ViewGroup mBatteryOptimizationView;

    /**
     * Do not initialize MainActivity until the Welcome Activity has been completed.
     * @see SSActivity#setSkipPermissionRequest(boolean)
     * @see SSActivity#setWelcomePageDisallowed(boolean)
     * @see SSActivity#setSkipStuffGeneration(boolean)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        setSkipPermissionRequest(true);
        setWelcomePageDisallowed(true);
        setSkipStuffGeneration(true);

        final FloatingActionButton nextButton = findViewById(R.id.activity_welcome_view_next);
        final AppCompatImageView previousButton = findViewById(R.id.activity_welcome_view_previous);
        final ProgressBar progressBar = findViewById(R.id.activity_welcome_progress_bar);
        final ViewPager viewPager = findViewById(R.id.activity_welcome_view_pager);
        final DynamicViewPagerAdapter pagerAdapter = new DynamicViewPagerAdapter();

        /*--------- layout_welcome_page_1 ------------ */
        mSplashView = (ViewGroup) getLayoutInflater().inflate(R.layout.layout_welcome_page_1, null, false);
        pagerAdapter.addView(mSplashView);

        /*--------- layout_welcome_page_2 ------------ */
        mPermissionsView = (ViewGroup) getLayoutInflater().inflate(R.layout.layout_welcome_page_2, null, false);
        pagerAdapter.addView(mPermissionsView);
        checkPermissionsState();

        mPermissionsView.findViewById(R.id.layout_welcome_page_2_request_btn)
                .setOnClickListener(v -> requestRequiredPermissions(false));

        /*--------- layout_welcome_page_3 ------------ */

        mBatteryOptimizationView = (ViewGroup) getLayoutInflater().inflate(R.layout.layout_welcome_page_3, null, false);
        pagerAdapter.addView(mBatteryOptimizationView);
        checkBatteryOptimizationState();

        mBatteryOptimizationView.findViewById(R.id.welcome_page_3_request_btn)
                .setOnClickListener(v -> PermissionHelper.requestDisableBatteryOptimization(WelcomeActivity.this));

        /*--------- layout_welcome_page_4 ------------ */
        View view = getLayoutInflater().inflate(R.layout.layout_welcome_page_4, null, false);
        pagerAdapter.addView(view);

        /*--------- Widgets setup ------------ */
        progressBar.setMax((pagerAdapter.getCount() - 1) * 100);

        previousButton.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() - 1 >= 0)
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
        });

        nextButton.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() + 1 < pagerAdapter.getCount())
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            else
            {

                //Do not show welcome screen anymore
                getDefaultSharedPreferences().edit()
                        .putBoolean(SSActivity.WELCOME_SHOWN, true)
                        .apply();

                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finish();
            }
        });

        /* UI effects */
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                progressBar.setProgress((position * 100) + (int) (positionOffset * 100));

                if (position == 0)
                {
                    progressBar.setAlpha(positionOffset);
                    previousButton.setAlpha(positionOffset);
                }
                else
                {
                    progressBar.setAlpha(1.0f);
                    previousButton.setAlpha(1.0f);
                }
            }

            @Override
            public void onPageSelected(int position)
            {
                nextButton.setImageResource(position + 1 >= pagerAdapter.getCount()
                        ? R.drawable.ic_check_24dp
                        : R.drawable.ic_navigate_next_24dp);
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
        });

        /*--------- All set ------------ */
        viewPager.setAdapter(pagerAdapter);
    }

    /**
     * Checks and displays the current status of required permissions
     * @see PermissionHelper
     */
    private void checkPermissionsState()
    {
        if (Build.VERSION.SDK_INT < 23)
            return;

        boolean permissionsGranted = PermissionHelper.checkRequiredPermissions(this);

        mPermissionsView.findViewById(R.id.layout_welcome_page_2_perm_ok_img)
                .setVisibility(permissionsGranted ? View.VISIBLE : View.GONE);

        mPermissionsView.findViewById(R.id.layout_welcome_page_2_request_btn)
                .setVisibility(permissionsGranted ? View.GONE : View.VISIBLE);
    }

    /**
     * Checks and displays the current status of battery optimization
     * @see PermissionHelper
     */
    private void checkBatteryOptimizationState()
    {
        /*if (Build.VERSION.SDK_INT < 23)
            return;*/

        boolean batteryOptimizationDisabled = PermissionHelper.checkBatteryOptimizationDisabled(this);

        mBatteryOptimizationView.findViewById(R.id.welcome_page_3_perm_ok_img)
                .setVisibility(batteryOptimizationDisabled ? View.VISIBLE : View.GONE);

        mBatteryOptimizationView.findViewById(R.id.welcome_page_3_request_btn)
                .setVisibility(batteryOptimizationDisabled ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppUtils.OPTIMIZATION_DISABLE)
            checkBatteryOptimizationState();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkPermissionsState();
    }
}

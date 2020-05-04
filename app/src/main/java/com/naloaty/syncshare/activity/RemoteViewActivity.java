package com.naloaty.syncshare.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.SSActivity;
import com.naloaty.syncshare.fragment.RemoteAlbumsFragment;
import com.naloaty.syncshare.fragment.RemoteMediaFragment;
import com.naloaty.syncshare.fragment.RemoteViewFragment;

public class RemoteViewActivity extends SSActivity {

    private static final String TAG = "RemoteViewActivity";

    public static final String ACTION_CHANGE_FRAGMENT = "com.naloaty.intent.action.PAIR_DEVICE_CHANGE_FRAGMENT";
    public static final String EXTRA_TARGET_FRAGMENT = "targetFragment";

    public static final String EXTRA_DEVICE_ID = "deviceId";
    public static final String EXTRA_DEVICE_NICKNAME = "deviceNickname";
    public static final String EXTRA_ALBUM_NAME = "albumName";
    public static final String EXTRA_ALBUM = "album";

    private RemoteAlbumsFragment mRemoteAlbumsFragment;
    private RemoteMediaFragment mRemoteMediaFragment;

    /* Collapsing toolbar */
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mToolBarLayout;
    private Toolbar mToolBar;

    private final IntentFilter mFilter = new IntentFilter();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (ACTION_CHANGE_FRAGMENT.equals(intent.getAction())) {

                RemoteViewFragment targetFragment = RemoteViewFragment.valueOf(intent.getStringExtra(EXTRA_TARGET_FRAGMENT));

                Bundle bundle;
                switch (targetFragment) {
                    case AlbumsView:
                        bundle = new Bundle();
                        bundle.putString(EXTRA_DEVICE_ID, getIntent().getStringExtra(EXTRA_DEVICE_ID));

                        mRemoteAlbumsFragment.setArguments(bundle);
                        setFragment(targetFragment, getIntent().getStringExtra(EXTRA_DEVICE_NICKNAME));
                        break;

                    case MediaGridView:
                        bundle = new Bundle();
                        bundle.putString(EXTRA_ALBUM, intent.getStringExtra(EXTRA_ALBUM));
                        bundle.putString(EXTRA_DEVICE_ID, getIntent().getStringExtra(EXTRA_DEVICE_ID));

                        mRemoteMediaFragment.setArguments(bundle);

                        setFragment(targetFragment, intent.getStringExtra(EXTRA_ALBUM_NAME));
                        break;
                }

            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_view);

        mFilter.addAction(ACTION_CHANGE_FRAGMENT);

        /* Collapsing toolbar */
        mAppBarLayout = findViewById(R.id.remote_view_app_bar_layout);
        mToolBarLayout = findViewById(R.id.remote_view_toolbar_layout);
        mToolBar = findViewById(R.id.remote_view_toolbar);
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

        String deviceNickname = getIntent().getStringExtra(EXTRA_DEVICE_NICKNAME);
        String deviceId = getIntent().getStringExtra(EXTRA_DEVICE_ID);

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_DEVICE_NICKNAME, deviceNickname);
        bundle.putString(EXTRA_DEVICE_ID, deviceId);

        mRemoteAlbumsFragment = new RemoteAlbumsFragment();
        mRemoteAlbumsFragment.setArguments(bundle);

        mRemoteMediaFragment = new RemoteMediaFragment();

        setFragment(RemoteViewFragment.AlbumsView, deviceNickname);
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    public void onBackPressed() {

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.remote_view_fragment_placeholder);

        if (currentFragment instanceof RemoteAlbumsFragment)
            super.onBackPressed();
        else if (currentFragment instanceof RemoteMediaFragment)
            setFragment(RemoteViewFragment.AlbumsView, getIntent().getStringExtra(EXTRA_DEVICE_NICKNAME));
    }

    private void setFragment(RemoteViewFragment targetFragment, String title) {

        Fragment candidate = null;

        switch (targetFragment) {
            case AlbumsView:
                candidate = mRemoteAlbumsFragment;
                break;

            case MediaGridView:
                candidate = mRemoteMediaFragment;
                break;
        }

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.remote_view_fragment_placeholder);

        if (currentFragment == null ||  currentFragment != candidate) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (currentFragment != null)
                transaction.remove(currentFragment);

            if (currentFragment != null && candidate instanceof RemoteAlbumsFragment)
                transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
            else
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);

            transaction.add(R.id.remote_view_fragment_placeholder, candidate);
            transaction.commit();

            setToolbar(targetFragment, title);
        }
    }


    private void setToolbar(RemoteViewFragment fragment, String title) {

        boolean isExpand = false;

        switch (fragment) {
            case AlbumsView:
                isExpand = true;
                break;

            case MediaGridView:
                isExpand = false;
                break;

            default:
                isExpand = false;
        }

        if (title != null)
            mToolBarLayout.setTitle(title);
        else
            mToolBarLayout.setTitle(getString(R.string.text_defaultValue));

        mAppBarLayout.setExpanded(isExpand, true);
    }

}

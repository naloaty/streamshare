package com.naloaty.streamshare.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.naloaty.streamshare.R;
import com.naloaty.streamshare.app.SSActivity;
import com.naloaty.streamshare.fragment.RemoteAlbumsFragment;
import com.naloaty.streamshare.fragment.RemoteMediaFragment;
import com.naloaty.streamshare.fragment.RemoteViewFragment;

/**
 * This activity represents a screen for viewing a list of albums and media files on a remote device.
 *
 * Related fragments:
 * @see RemoteAlbumsFragment
 * @see RemoteMediaFragment
 */
public class RemoteViewActivity extends SSActivity {

    private static final String TAG = "RemoteViewActivity";

    public static final String ACTION_CHANGE_FRAGMENT = "com.naloaty.intent.action.PAIR_DEVICE_CHANGE_FRAGMENT";
    public static final String EXTRA_TARGET_FRAGMENT  = "targetFragment";

    public static final String EXTRA_DEVICE_ID        = "deviceId";
    public static final String EXTRA_DEVICE_NICKNAME  = "deviceNickname";
    public static final String EXTRA_ALBUM_NAME       = "albumName";
    public static final String EXTRA_ALBUM            = "album";

    private IntentFilter mFilter = new IntentFilter();
    private RemoteAlbumsFragment mRemoteAlbumsFragment;
    private RemoteMediaFragment mRemoteMediaFragment;

    /* UI elements */
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mToolBarLayout;
    private Toolbar mToolBar;

    /**
     * Receives a broadcast about CommunicationService state changes
     * @see #setFragment(RemoteViewFragment, String)
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_CHANGE_FRAGMENT.equals(action)) {

                Bundle bundle = new Bundle();
                RemoteViewFragment targetFragment = RemoteViewFragment.valueOf(intent.getStringExtra(EXTRA_TARGET_FRAGMENT));

                switch (targetFragment) {
                    case AlbumsView:
                        bundle.putString(EXTRA_DEVICE_ID, getIntent().getStringExtra(EXTRA_DEVICE_ID));

                        mRemoteAlbumsFragment.setArguments(bundle);

                        setFragment(targetFragment, getIntent().getStringExtra(EXTRA_DEVICE_NICKNAME));
                        break;

                    case MediaGridView:
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

        mAppBarLayout = findViewById(R.id.remote_view_app_bar_layout);
        mToolBarLayout = findViewById(R.id.remote_view_toolbar_layout);
        mToolBar = findViewById(R.id.toolbar);
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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        else
            Log.w(TAG, "Toolbar is not properly initialized");

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
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.remote_view_fragment_placeholder);

        if (currentFragment instanceof RemoteAlbumsFragment)
            super.onBackPressed();
        else if (currentFragment instanceof RemoteMediaFragment)
            setFragment(RemoteViewFragment.AlbumsView, getIntent().getStringExtra(EXTRA_DEVICE_NICKNAME));
    }

    /**
     * Replaces current fragment with required one
     * @param targetFragment Required fragment
     * @see #setToolbar(RemoteViewFragment, String)
     */
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

    /**
     * Sets the title and state of toolbar depending on the required fragment
     * @param fragment Required fragment
     */
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

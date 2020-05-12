package com.naloaty.syncshare.activity;

import android.content.res.Configuration;
import android.icu.util.Measure;
import android.net.Uri;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.appbar.AppBarLayout;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.SSActivity;
import com.naloaty.syncshare.communication.SSOkHttpClient;
import com.naloaty.syncshare.util.AppUtils;
import com.naloaty.syncshare.util.DeviceUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import okhttp3.OkHttpClient;

public class VideoPlayerActivity extends SSActivity implements CustomPlayerView.VisibilityListener {

    private static final String TAG = "VideoPlayerActivity";

    public static final String EXTRA_VIDEO_SOURCE = "videoSource";
    private CustomPlayerView playerView;
    private SimpleExoPlayer simpleExoPlayer;

    private Toolbar mToolbar;
    private AppBarLayout mAppbarLayout;
    private boolean fullScreenMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        mToolbar = findViewById(R.id.toolbar);
        mAppbarLayout = findViewById(R.id.appbar_layout);

        mToolbar.setTitle("");

        //Important to call this BEFORE setNavigationOnClickListener()
        setSupportActionBar(mToolbar);

        //To make "close" animation (this instead of using "parent activity")
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        String videoURL;

        if (getIntent() != null && getIntent().hasExtra(EXTRA_VIDEO_SOURCE)) {
            videoURL = getIntent().getStringExtra(EXTRA_VIDEO_SOURCE);
        }
        else
        {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.title_noVideoSource)
                    .setMessage(R.string.text_noVideoSource)
                    .setPositiveButton(R.string.btn_close, (dialog, which) -> onBackPressed())
                    .show();

            return;
        }

        playerView = findViewById(R.id.player_view);
        playerView.setBackground(getDrawable(R.color.colorBlack));
        setupPlayer(Uri.parse(videoURL));
    }

    private void setupPlayer(Uri videoUri) {
        simpleExoPlayer = new SimpleExoPlayer.Builder(this).build();
        playerView.setVisibilityListener(this);
        playerView.setPlayer(simpleExoPlayer);

        OkHttpClient client = SSOkHttpClient.getOkHttpClient(this);

        // Produces DataSource instances through which media data is loaded.
        //DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "SyncShare"));

        DataSource.Factory dataSourceFactory = new OkHttpDataSourceFactory(client, Util.getUserAgent(this, "SyncShare"));

        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoUri);

        // Prepare the simpleExoPlayer with the source.
        simpleExoPlayer.prepare(videoSource);
        simpleExoPlayer.setPlayWhenReady(true);
    }


    @Override
    public void onVisibilityChange(int visibility) {
        if (visibility == View.GONE)
            hideControls();
        else if (visibility == View.VISIBLE)
            showControls();
    }

    private void hideControls() {
        runOnUiThread(() -> {
            getWindow().getDecorView().setSystemUiVisibility(
                              View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hideController nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hideController status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);

            mAppbarLayout.animate()
                    .translationY(-mAppbarLayout.getHeight())
                    .setInterpolator(new AccelerateInterpolator())
                    .setDuration(200)
                    .start();

            fullScreenMode = true;
            //changeBackGroundColor();
        });
    }

    private void showControls(){
        runOnUiThread(() -> {
            int rotation = (((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay()).getRotation();
            if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) { //Landscape
                getWindow().getDecorView().setSystemUiVisibility(
                                  View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                playerView.setPaddingRelative(0, 0, 0, 0);
            }
            else
            {
                getWindow().getDecorView().setSystemUiVisibility(
                                  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                playerView.setPaddingRelative(0, 0, 0, AppUtils.getNavigationBarHeight(this));
            }
            mAppbarLayout.animate()
                    .translationY(AppUtils.getStatusBarHeight(getResources()))
                    .setInterpolator(new DecelerateInterpolator())
                    .setDuration(240)
                    .start();

            fullScreenMode = false;
            //changeBackGroundColor();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        simpleExoPlayer.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        simpleExoPlayer.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        simpleExoPlayer.release();
    }
}

package com.naloaty.syncshare.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.appbar.AppBarLayout;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.SSActivity;
import com.naloaty.syncshare.communication.SSOkHttpClient;
import com.naloaty.syncshare.media.Media;
import com.naloaty.syncshare.util.AppUtils;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.OkHttpClient;

/**
 * This activity loads and displays the selected video file.
 * @see ImageViewActivity
 */
public class VideoPlayerActivity extends SSActivity implements CustomPlayerView.VisibilityListener {

    private boolean insecureMode = false;

    private static final String TAG = "VideoPlayerActivity";

    //TODO: add ability to change this in settings

    private static int BUFFER_SEGMENT_SIZE = 10000;
    private static int MIN_BUFFERSIZE_MS = 10 * 1000;
    private static int MAX_BUFFERSIZE_MS = 25 * 1000;

    /* Length of media that should be buffered after seeking */
    private static int BUFFER_FOR_PLAYBACK_MS = 2 * 1000;

    /* Length of media that should be buffered after buffer is depleted */
    private static int BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 5 * 1000;

    public static final String EXTRA_REMOTE_URL = "remoteUrl";
    public static final String EXTRA_VIDEO_INFO = "videoInfo";
    private SimpleExoPlayer simpleExoPlayer;

    private boolean fullScreenMode = false;

    /* UI elements */
    private Toolbar mToolbar;
    private AppBarLayout mAppbarLayout;
    private CustomPlayerView playerView;


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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        else
            Log.w(TAG, "Toolbar is not properly initialized");

        //Allows requests from any ip
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true );

        String remoteURL;
        Media videoInfo;

        if (getIntent() != null) {
            remoteURL = getIntent().getStringExtra(EXTRA_REMOTE_URL);
            videoInfo = (Media) getIntent().getSerializableExtra(EXTRA_VIDEO_INFO);
        }
        else
        {
            onSourceError();
            return;
        }

        if (videoInfo == null) {
            onSourceError();
            return;
        }

        playerView = findViewById(R.id.player_view);
        playerView.setBackground(getDrawable(R.color.colorBlack));
        setupPlayer(Uri.parse(remoteURL + videoInfo.getFilename()), videoInfo.getMimeType());
    }

    /**
     * When there is no video source, this method is used to display an error message.
     */
    private void onSourceError() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_noVideoSource)
                .setMessage(R.string.text_noVideoSource)
                .setPositiveButton(R.string.btn_close, (dialog, which) -> onBackPressed())
                .show();
    }

    private void setupPlayer(Uri videoUri, String mime) {
        DefaultAllocator defaultAllocator = new DefaultAllocator(true, BUFFER_SEGMENT_SIZE);
        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setAllocator(defaultAllocator)
                .setBufferDurationsMs(MIN_BUFFERSIZE_MS, MAX_BUFFERSIZE_MS, BUFFER_FOR_PLAYBACK_MS, BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS)
                .createDefaultLoadControl();

        //TODO: add more types of errors to handle
        ExoPlayer.EventListener listener = new ExoPlayer.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                switch (error.type) {
                    case ExoPlaybackException.TYPE_SOURCE:
                        Log.e(TAG, "TYPE_SOURCE: " + error.getSourceException().getMessage());

                        if (!insecureMode) {
                            new AlertDialog.Builder(VideoPlayerActivity.this)
                                    .setTitle(R.string.title_unsupportedVideo)
                                    .setMessage(R.string.text_unsupportedVideo)
                                    .setPositiveButton(R.string.btn_close, (dialog, which) -> onBackPressed())
                                    .show();
                        }
                        else
                        {
                            new AlertDialog.Builder(VideoPlayerActivity.this)
                                    .setTitle(R.string.title_unsupportedVideo)
                                    .setMessage(R.string.text_unsupportedVideoInsecure)
                                    .setNegativeButton(R.string.btn_no, (dialog, which) -> onBackPressed())
                                    .setPositiveButton(R.string.btn_yes, (dialog, which) -> {
                                        onBackPressed();
                                        Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(videoUri, mime);
                                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        startActivity(intent);
                                    }).show();
                        }
                        break;

                    default:
                        new AlertDialog.Builder(VideoPlayerActivity.this)
                                .setTitle(R.string.title_error)
                                .setMessage(R.string.text_videoPlayerError)
                                .setPositiveButton(R.string.btn_close, (dialog, which) -> onBackPressed())
                                .show();
                        break;
                }
            }
        };

        simpleExoPlayer = new SimpleExoPlayer.Builder(this)
                .setLoadControl(loadControl)
                .build();

        simpleExoPlayer.addListener(listener);
        playerView.setVisibilityListener(this);
        playerView.setPlayer(simpleExoPlayer);

        OkHttpClient client = SSOkHttpClient.getOkHttpClient(this);
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

    /**
     * Hides toolbar, status bar and navigation bar
     */
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
        });
    }

    /**
     * Shows toolbar, status bar and navigation bar
     */
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

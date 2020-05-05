package com.naloaty.syncshare.activity;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.SSActivity;

public class VideoPlayerActivity extends SSActivity {

    private static final String TAG = "VideoPlayerActivity";

    public static final String EXTRA_VIDEO_SOURCE = "videoSource";
    private PlayerView playerView;
    private SimpleExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

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
        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "SyncShare"));

        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoUri);

        // Prepare the player with the source.
        player.prepare(videoSource);

        player.setPlayWhenReady(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        player.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        player.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }
}

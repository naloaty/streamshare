package com.naloaty.syncshare.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

/**
 * Custom player view for ExoPlayer. Allows to handle changes in the visibility of controls.
 */
public final class CustomPlayerView
        extends PlayerView implements PlayerControlView.VisibilityListener {

    /**
     * Listener of controls visibility.
     */
    public interface VisibilityListener {
        void onVisibilityChange(int visibility);
    }

    private VisibilityListener mVisibilityListener;

    public CustomPlayerView(Context context) {
        this(context, null);
    }

    public CustomPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setControllerVisibilityListener(this);
    }

    public void setVisibilityListener(VisibilityListener mVisibilityListener) {
        this.mVisibilityListener = mVisibilityListener;
    }

    @Override
    public void onVisibilityChange(int visibility) {
        if (mVisibilityListener != null)
            mVisibilityListener.onVisibilityChange(visibility);
    }
}
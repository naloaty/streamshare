package com.naloaty.syncshare.widget;

import android.animation.ValueAnimator;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import retrofit2.internal.EverythingIsNonNull;

/**
 * This class allows to collapse linear layout like collapsing toolbar.
 */
public class LinearLayoutCollapser {

    private LinearLayout mLayout;
    private int mOriginalHeight;
    private boolean isShrink;
    private int mDuration;

    private ValueAnimator mHideAnimation = ValueAnimator.ofFloat(0f, 1f);
    private ValueAnimator mShowAnimation = ValueAnimator.ofFloat(1f, 0f);

    /**
     * Initializes hide and show animations
     * @param duration Duration of collapsing
     */
    private void initAnimations(int duration) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mLayout.getLayoutParams();

        mHideAnimation.setDuration(duration);
        mHideAnimation.addUpdateListener(animation -> {
            params.topMargin = -(int) (mOriginalHeight * (float)animation.getAnimatedValue());
            mLayout.setLayoutParams(params);
        });

        mShowAnimation.setDuration(duration);
        mShowAnimation.addUpdateListener(animation -> {
            params.topMargin = -(int) (mOriginalHeight * (float)animation.getAnimatedValue());
            mLayout.setLayoutParams(params);
        });
    }

    /**
     * @param layout Layout to be collapsed
     * @param duration Duration of collapsing
     */
    @EverythingIsNonNull
    public LinearLayoutCollapser(LinearLayout layout, int duration, boolean isExpanded) {
        mLayout = layout;
        mDuration = duration;

        initAnimations(duration);

        mLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mOriginalHeight = mLayout.getMeasuredHeight();

                if (!isExpanded) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mLayout.getLayoutParams();
                    params.topMargin = -mOriginalHeight;
                    mLayout.setLayoutParams(params);
                }
            }
        });

        isShrink = isExpanded;

        mHideAnimation.setDuration(mDuration);
        mShowAnimation.setDuration(mDuration);
    }

    /**
     * Toggles layout collapsing
     */
    public void toggleLayout() {
        //TODO: add support for half-position animation

        if (isShrink) {
            if (mShowAnimation.isRunning())
                mShowAnimation.cancel();

            mHideAnimation.start();
        }
        else
        {
            if (mHideAnimation.isRunning())
                mHideAnimation.cancel();

            mShowAnimation.start();
        }

        isShrink = !isShrink;
    }
}

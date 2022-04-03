package com.naloaty.streamshare.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Interpolator;

import retrofit2.internal.EverythingIsNonNull;

public class ViewRevealHelper {

    private View mView;
    private boolean isShrink;
    private int mDuration;

    @EverythingIsNonNull
    public ViewRevealHelper(View view, int duration, boolean isExpanded) {
        mView = view;
        isShrink = isExpanded;
        mDuration = duration;
    }

    private void revealView(int duration) {
        //TODO: add ability to customize reveal

        int cx = mView.getRight();
        int cy = 0;

        int finalRadius = Math.max(mView.getWidth(), mView.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(mView, cx, cy, 0, finalRadius);
        anim.setDuration(duration);

        mView.setVisibility(View.VISIBLE);
        anim.start();
    }

    private void hideView(int duration) {
        int initialRadius = mView.getWidth();

        int cx = mView.getRight();
        int cy = 0;

        Animator anim = ViewAnimationUtils.createCircularReveal(mView, cx, cy, initialRadius, 0);
        anim.setDuration(duration);
        anim.setInterpolator(getLinearOutSlowInInterpolator());

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                mView.setVisibility(View.INVISIBLE);
            }
        });

        anim.start();
    }

    public void toggleView() {

        if (isShrink)
            hideView(mDuration);
        else
            revealView(mDuration);

        isShrink = !isShrink;
    }

    public static Interpolator getLinearOutSlowInInterpolator() {
        //Decelerate Interpolator - For elements that enter the screen
        return new CubicBezierInterpolator(0, 0, 0.2, 1);
    }
}

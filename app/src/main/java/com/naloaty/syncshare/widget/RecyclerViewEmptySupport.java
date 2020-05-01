package com.naloaty.syncshare.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

/*
 * Based on https://stackoverflow.com/questions/28217436/how-to-show-an-empty-view-with-a-recyclerview
 */

public class RecyclerViewEmptySupport extends RecyclerView {

    private View emptyView;
    private UIState currentState;
    private boolean isEmptyViewEnabled;

    final private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };

    public RecyclerViewEmptySupport(Context context) {
        super(context);
    }

    public RecyclerViewEmptySupport(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewEmptySupport(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private enum UIState {
        EmptyViewVisible,
        EmptyViewGone,
        EmptyViewDisabled,
        NotDefined
    }

    private UIState getRequiredState() {
        if (emptyView == null || getAdapter() == null)
            return UIState.NotDefined;

        if (!isEmptyViewEnabled)
            return UIState.EmptyViewDisabled;

        boolean hasItems = getAdapter().getItemCount() != 0;

        if (hasItems)
            return UIState.EmptyViewGone;
        else
            return UIState.EmptyViewVisible;
    }

    private void setUIState(UIState state) {
        if (state == currentState)
            return;

        switch (state) {
            case EmptyViewGone:
                toggleEmptyView(false);
                break;

            case EmptyViewVisible:
                toggleEmptyView(true);
                break;

            case EmptyViewDisabled:
                toggleEmptyView(false);
                break;

            case NotDefined:
                //Do nothing
                break;
        }

        //emptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
        //setVisibility(emptyViewVisible ? GONE : VISIBLE);

        currentState = state;
    }

    public void setEmptyViewEnabled(boolean isEnabled) {
        this.isEmptyViewEnabled = isEnabled;
        setUIState(getRequiredState());
    }

    private void checkIfEmpty() {
        setUIState(getRequiredState());
    }

    private void toggleEmptyView(boolean isVisible) {
        int currentState = emptyView.getVisibility();

        if (isVisible) {
            if (currentState == VISIBLE)
                return;

            this.animate()
                    .alpha(0)
                    .setDuration(100)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);

                            setVisibility(GONE);

                            emptyView.setAlpha(0);
                            emptyView.setVisibility(VISIBLE);
                            emptyView.animate()
                                    .alpha(1)
                                    .setDuration(100)
                                    .setListener(null);
                        }
                    });
        }
        else
        {
            if (currentState == GONE)
                return;

            emptyView.animate()
                    .alpha(0)
                    .setDuration(100)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);

                            emptyView.setVisibility(GONE);

                            if (!isEmptyViewEnabled)
                                return;

                            setAlpha(0);
                            setVisibility(VISIBLE);
                            animate()
                                    .alpha(1)
                                    .setDuration(100)
                                    .setListener(null);

                        }
                    });
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }

        checkIfEmpty();
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        checkIfEmpty();
    }
}
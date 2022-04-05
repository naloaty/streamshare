package com.naloaty.streamshare.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.streamshare.R;
import com.naloaty.streamshare.adapter.MyDevicesAdapter;
import com.naloaty.streamshare.adapter.OnRVClickListener;
import com.naloaty.streamshare.database.entity.SSDevice;
import com.naloaty.streamshare.database.viewmodel.SSDeviceViewModel;
import com.naloaty.streamshare.dialog.MyDeviceDetailsDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment displays a list of trusted devices.
 * @see com.naloaty.streamshare.activity.DeviceManageActivity
 */
public class MyDevicesFragment extends Fragment {

    private static final String TAG = "MyDevicesFragment";

    private List<SSDevice> mList = new ArrayList<>();
    private MyDevicesAdapter mRVAdapter;
    private SSDeviceViewModel mDeviceViewModel;

    /* UI elements */
    private FrameLayout mRootLayout;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDeviceViewModel = new ViewModelProvider(this).get(SSDeviceViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_my_devices_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.my_devices_recycler_view);
        mRootLayout = view.findViewById(R.id.root_frame_layout);

        initMessage(view.findViewById(R.id.message_placeholder));
        setupRecyclerView();
        setupView();
    }

    /**
     * Fixes the minHeight property of a fragment layout when it is inside ScrollView
     */
    private void setupView() {
        ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!isAdded())
                    return;

                int height = mRootLayout.getMeasuredHeight();
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mRootLayout.getLayoutParams();

                mRootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                if(height < (int)getResources().getDimension(R.dimen.manage_devices_min_height)) {
                    params.height = (int) getResources().getDimension(R.dimen.manage_devices_min_height);
                    mRootLayout.setLayoutParams(params);

                    Log.d(TAG, "Layout adjusted");
                }

            }
        };

        ViewTreeObserver viewTreeObserver = mRootLayout.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener);
    }

    /**
     * Initializes a list of trusted devices
     * @see SSDeviceViewModel#getAllDevices()
     */
    private void setupRecyclerView() {
        OnRVClickListener clickListener = itemIndex -> {
            SSDevice ssDevice = mList.get(itemIndex);

            MyDeviceDetailsDialog details = new MyDeviceDetailsDialog(requireContext());
            details.setSSDevice(ssDevice);
            details.show();
        };

        mRVAdapter = new MyDevicesAdapter(clickListener);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mRVAdapter);

        mDeviceViewModel.getAllDevices().observe(getViewLifecycleOwner(), ssDevices ->
        {
            mList = ssDevices;
            mRVAdapter.setDevicesList(ssDevices);
            updateUIState();
        });
    }

    /*
     * Following section represents the UI state machine
     * TODO: Wrap ViewMessage into widget
     */

    /* View Message */
    private ImageView mMessageIcon;
    private TextView mMessageText;
    private AppCompatButton mMessageActionBtn;
    private LinearLayout mMessageBtnLayout;
    private ViewGroup mMessageHolder;

    private UIState currentUIState;

    private void initMessage(ViewGroup messageHolder) {
        mMessageIcon = messageHolder.findViewById(R.id.message_icon);
        mMessageText = messageHolder.findViewById(R.id.message_text);
        mMessageActionBtn = messageHolder.findViewById(R.id.message_action_button);
        mMessageBtnLayout = messageHolder.findViewById(R.id.message_btn_layout);
        mMessageHolder = messageHolder;
    }

    private enum UIState {
        DevicesShown,
        NoDevicesAdded,
    }

    /**
     * Sets the optimal state of the UI
     */
    private void updateUIState() {
        setUIState(getRequiredState());
    }

    /**
     * Returns the optimal state of the UI.
     * @return Optimal UI state
     */
    private UIState getRequiredState() {
        boolean hasItems = mList.size() > 0;

        if (hasItems)
            return UIState.DevicesShown;
        else
            return UIState.NoDevicesAdded;
    }

    /**
     * Sets the state of the UI
     * @param state Required UI state
     */
    private void setUIState(UIState state) {
        if (currentUIState == state)
            return;

        switch (state) {
            case DevicesShown:
                toggleMessage(false);
                break;

            case NoDevicesAdded:
                replaceMessage(R.drawable.ic_devices_24dp, R.string.text_noDevicesAdded);
                toggleMessage(true);
                break;
        }

        currentUIState = state;
    }

    /**
     * Replaces one message with another (action button disabled)
     * @param iconResource Message icon resource
     * @param textResource Message text resource
     */
    private void replaceMessage(int iconResource, int textResource) {
        replaceMessage(iconResource, textResource, 0, null);
    }

    /**
     * Replaces one message with another
     * @param iconResource Message icon resource
     * @param textResource Message text resource
     * @param btnResource Action button text resource
     * @param listener Action button click listener
     */
    private void replaceMessage(int iconResource, int textResource, int btnResource, @Nullable View.OnClickListener listener) {

        int currState = mMessageHolder.getVisibility();

        if (currState == View.VISIBLE) {
            mMessageHolder
                    .animate()
                    .setDuration(300)
                    .alpha(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            setMessage(iconResource, textResource, btnResource, listener);
                        }
                    });
        }
        else
        {
            setMessage(iconResource, textResource, btnResource, listener);
        }
    }

    /**
     * Sets the message
     * @param iconResource Message icon resource
     * @param textResource Message text resource
     * @param btnResource Action button text resource
     * @param listener Action button click listener
     */
    private void setMessage(int iconResource, int textResource, int btnResource, View.OnClickListener listener) {
        mMessageIcon.setImageResource(iconResource);
        mMessageText.setText(textResource);

        if (listener == null) {
            mMessageBtnLayout.setVisibility(View.GONE);
        }
        else
        {
            mMessageBtnLayout.setVisibility(View.VISIBLE);
            mMessageActionBtn.setText(btnResource);
            mMessageActionBtn.setOnClickListener(listener);
        }

        int currSate = mMessageHolder.getVisibility();

        if (mMessageHolder.getAlpha() < 1 && currSate == View.VISIBLE) {
            mMessageHolder
                    .animate()
                    .setDuration(300)
                    .alpha(1)
                    .setListener(null);
        }
    }

    /**
     * Toggles message visibility
     * @param isVisible Required message visibility
     */
    private void toggleMessage(boolean isVisible) {
        int currentState = mMessageHolder.getVisibility();

        if (isVisible) {
            if (currentState == View.VISIBLE)
                return;

            mRecyclerView.setVisibility(View.GONE);
            mMessageHolder.setAlpha(0);
            mMessageHolder.setVisibility(View.VISIBLE);
            mMessageHolder.animate()
                    .alpha(1)
                    .setDuration(150)
                    .setListener(null);
        }
        else
        {
            if (currentState == View.GONE)
                return;

            mMessageHolder.setVisibility(View.VISIBLE);
            mMessageHolder.setAlpha(1);
            mMessageHolder.animate()
                    .alpha(0)
                    .setDuration(150)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mMessageHolder.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }
}

package com.naloaty.syncshare.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.adapter.MyDevicesAdapter;
import com.naloaty.syncshare.adapter.OnRVClickListener;
import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.database.device.SSDeviceViewModel;
import com.naloaty.syncshare.dialog.MyDeviceDetailsDialog;
import com.naloaty.syncshare.util.DeviceUtils;

import java.util.ArrayList;
import java.util.List;

public class MyDevicesFragment extends Fragment {

    private List<SSDevice> mList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private MyDevicesAdapter mRVAdapter;
    private SSDeviceViewModel ssDeviceViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ssDeviceViewModel = new ViewModelProvider(this).get(SSDeviceViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.layout_my_devices_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.my_devices_recycler_view);
        initMessage(view.findViewById(R.id.message_placeholder));
        setupRecyclerView();
    }

    private void setupRecyclerView() {

        OnRVClickListener clickListener = new OnRVClickListener() {
            @Override
            public void onClick(int itemIndex) {
                SSDevice ssDevice = mList.get(itemIndex);

                MyDeviceDetailsDialog details = new MyDeviceDetailsDialog(getContext());
                details.setSSDevice(ssDevice);
                details.show();
            }
        };

        RecyclerView.LayoutManager layoutManager;

        if (DeviceUtils.isPortrait(getResources()))
            layoutManager = new LinearLayoutManager(getContext());
        else
            layoutManager = new GridLayoutManager(getContext(), 2);

        mRVAdapter = new MyDevicesAdapter(clickListener);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRVAdapter);

        ssDeviceViewModel.getAllDevices().observe(getViewLifecycleOwner(), new Observer<List<SSDevice>>() {
            @Override
            public void onChanged(List<SSDevice> ssDevices) {
                mList = ssDevices;
                mRVAdapter.setDevicesList(ssDevices);
                updateUIState();
            }
        });
    }

    /*
     * UI State Machine
     */

    /* Message view */
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

    private void updateUIState() {
        setUIState(getRequiredState());
    }

    private UIState getRequiredState() {
        boolean hasItems = mList.size() > 0;

        if (hasItems)
            return UIState.DevicesShown;
        else
            return UIState.NoDevicesAdded;
    }

    private void setUIState(UIState state)
    {
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

    private void replaceMessage(int iconResource, int textResource) {
        replaceMessage(iconResource, textResource, 0, null);
    }

    private void replaceMessage(int iconResource, int textResource, int btnResource, View.OnClickListener listener) {

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

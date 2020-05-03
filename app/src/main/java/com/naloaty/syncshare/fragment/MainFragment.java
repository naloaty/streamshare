package com.naloaty.syncshare.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.activity.DeviceManageActivity;
import com.naloaty.syncshare.activity.LocalDeviceActivity;
import com.naloaty.syncshare.activity.RemoteViewActivity;
import com.naloaty.syncshare.adapter.OnRVClickListener;
import com.naloaty.syncshare.adapter.OnlineDevicesAdapter;
import com.naloaty.syncshare.database.device.NetworkDevice;
import com.naloaty.syncshare.database.device.NetworkDeviceViewModel;
import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.database.device.SSDeviceViewModel;
import com.naloaty.syncshare.service.CommunicationService;
import com.naloaty.syncshare.util.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private ArrayList<SSDevice> mList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private OnlineDevicesAdapter mRVAdapter;
    private NetworkDeviceViewModel mNetworkDeviceViewModel;
    private SSDeviceViewModel mSSDeviceViewModel;

    /* Local device */
    private TextView mAlbums;
    private TextView mPhotos;
    private TextView mVideos;
    private LinearLayout mLocalDeviceLayout;

    private final IntentFilter mFilter = new IntentFilter();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(CommunicationService.SERVICE_STATE_CHANGED)) {
                setServiceState(intent.getBooleanExtra(CommunicationService.EXTRA_SERVICE_SATE, false));
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNetworkDeviceViewModel = new ViewModelProvider(this).get(NetworkDeviceViewModel.class);
        mSSDeviceViewModel = new ViewModelProvider(this).get(SSDeviceViewModel.class);

        mFilter.addAction(CommunicationService.SERVICE_STATE_CHANGED);
    }



    @Override
    public void onResume() {
        setServiceState(AppUtils.isServiceRunning(getContext(), CommunicationService.class));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, mFilter);
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.layout_main_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* Local device */
        mAlbums = view.findViewById(R.id.local_device_albums);
        mPhotos = view.findViewById(R.id.local_device_photos);
        mVideos = view.findViewById(R.id.local_device_videos);
        mLocalDeviceLayout = view.findViewById(R.id.local_device_layout);

        initMessage(view.findViewById(R.id.message_placeholder));

        mLocalDeviceLayout.setOnClickListener((v -> startActivity(new Intent(getContext(), LocalDeviceActivity.class))));

        mRecyclerView = view.findViewById(R.id.main_fragment_devices_online);
        setupRecyclerView();
    }



    private void setupRecyclerView() {

        OnRVClickListener clickListener = new OnRVClickListener() {
            @Override
            public void onClick(int itemIndex) {
                Intent intent = new Intent(getContext(), RemoteViewActivity.class);
                intent.putExtra(RemoteViewActivity.EXTRA_DEVICE_ID, mList.get(itemIndex).getDeviceId());
                intent.putExtra(RemoteViewActivity.EXTRA_DEVICE_NICKNAME, mList.get(itemIndex).getNickname());
                startActivity(intent);
            }
        };

        /* RecyclerView */
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRVAdapter = new OnlineDevicesAdapter(clickListener);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRVAdapter);

        mNetworkDeviceViewModel.getAllDevices().observe(getViewLifecycleOwner(), new Observer<List<NetworkDevice>>() {
            @Override
            public void onChanged(List<NetworkDevice> networkDevices) {

                mList = new ArrayList<>();

                for (NetworkDevice networkDevice : networkDevices) {
                    String deviceId = networkDevice.getDeviceId();
                    SSDevice foundedDevice = mSSDeviceViewModel.findDeviceDep(deviceId);

                    if (foundedDevice == null)
                        continue;

                    mList.add(foundedDevice);
                }

                mRVAdapter.setDevicesList(mList);
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
    private boolean isServiceRunning = false;

    private void initMessage(ViewGroup messageHolder) {
        mMessageIcon = messageHolder.findViewById(R.id.message_icon);
        mMessageText = messageHolder.findViewById(R.id.message_text);
        mMessageActionBtn = messageHolder.findViewById(R.id.message_action_button);
        mMessageBtnLayout = messageHolder.findViewById(R.id.message_btn_layout);
        mMessageHolder = messageHolder;
    }

    private enum UIState {
        OnlineShown,
        NoOnlineDevices,
        NoDevicesAdded,
        ServiceNotStarted
    }

    private void updateUIState() {
        setUIState(getRequiredState());
    }

    private UIState getRequiredState() {

        if (isServiceRunning) {
            boolean hasDevices = mSSDeviceViewModel.getDeviceCount() > 0;

            if (hasDevices) {
                boolean hasOnlineDevices = mList.size() > 0;

                if (hasOnlineDevices)
                    return UIState.OnlineShown;
                else
                    return UIState.NoOnlineDevices;
            }
            else
                return UIState.NoDevicesAdded;
        }
        else
            return UIState.ServiceNotStarted;

    }

    private void setServiceState(boolean serviceStarted) {
        isServiceRunning = serviceStarted;
        updateUIState();
    }

    private void setUIState(UIState state)
    {
        if (currentUIState == state)
            return;

        switch (state) {
            case OnlineShown:
                toggleMessage(false);
                break;

            case NoOnlineDevices:
                replaceMessage(R.drawable.ic_wifi_24dp, R.string.text_noOnlineDevices);
                toggleMessage(true);
                break;

            case ServiceNotStarted:
                View.OnClickListener startService = (v -> {
                    Intent intent = new Intent(getContext(), CommunicationService.class);
                    getActivity().startService(intent);
                });

                replaceMessage(R.drawable.ic_service_off_24dp, R.string.text_serviceNotStarted, R.string.btn_start, startService);

                toggleMessage(true);
                break;

            case NoDevicesAdded:
                View.OnClickListener manageDevices = (v -> {
                    startActivity(new Intent(getContext(), DeviceManageActivity.class));
                });

                replaceMessage(R.drawable.ic_devices_24dp, R.string.text_noDevicesAdded, R.string.btn_manageDevices, manageDevices);
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

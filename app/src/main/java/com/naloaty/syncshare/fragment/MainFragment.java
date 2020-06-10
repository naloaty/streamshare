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
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.naloaty.syncshare.util.DeviceUtils;

import java.util.ArrayList;

/**
 * This fragment displays a list of current devices on the network (trusted devices only).
 * @see MainFragment
 */
public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    private final IntentFilter mFilter = new IntentFilter();
    private ArrayList<SSDevice> mList = new ArrayList<>();
    private OnlineDevicesAdapter mRVAdapter;
    private NetworkDeviceViewModel mNetworkDeviceViewModel;
    private SSDeviceViewModel mDeviceViewModel;

    /* UI elements */
    private RecyclerView mRecyclerView;
    private LinearLayout mLocalDeviceLayout;

    /**
     * Receives a broadcast about CommunicationService state changes
     * @see MainFragment#setServiceState(boolean)
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (CommunicationService.SERVICE_STATE_CHANGED.equals(action)) {
                setServiceState(intent.getBooleanExtra(CommunicationService.EXTRA_SERVICE_SATE, false));
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNetworkDeviceViewModel = new ViewModelProvider(this).get(NetworkDeviceViewModel.class);
        mDeviceViewModel = new ViewModelProvider(this).get(SSDeviceViewModel.class);

        mFilter.addAction(CommunicationService.SERVICE_STATE_CHANGED);
    }

    @Override
    public void onResume() {
        super.onResume();

        setServiceState(AppUtils.isServiceRunning(requireContext(), CommunicationService.class));
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mReceiver, mFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mReceiver);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLocalDeviceLayout = view.findViewById(R.id.local_device_layout);
        mLocalDeviceLayout.setOnClickListener((v -> startActivity(new Intent(getContext(), LocalDeviceActivity.class))));
        mRecyclerView = view.findViewById(R.id.main_fragment_devices_online);

        initMessage(view.findViewById(R.id.message_placeholder));
        setupRecyclerView();
    }

    /**
     * Sets the layout state depending on device type (phone or tablet)
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Fragment localDevice = requireActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_localDevice);

        if (localDevice != null && DeviceUtils.isLandscape(getResources())){
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mLocalDeviceLayout.setVisibility(View.GONE);
        }
        else
        {
            mLocalDeviceLayout.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Initializes a list of online devices
     * @see NetworkDeviceViewModel#getAllDevices() 
     * @see SSDeviceViewModel#getAllDevices() 
     * @see SSDeviceViewModel#findDeviceDep(String) 
     */
    private void setupRecyclerView() {
        OnRVClickListener clickListener = itemIndex -> {
            Intent intent = new Intent(getContext(), RemoteViewActivity.class);
            intent.putExtra(RemoteViewActivity.EXTRA_DEVICE_ID, mList.get(itemIndex).getDeviceId());
            intent.putExtra(RemoteViewActivity.EXTRA_DEVICE_NICKNAME, mList.get(itemIndex).getNickname());
            startActivity(intent);
        };

        RecyclerView.LayoutManager layoutManager;

        if (DeviceUtils.isPortrait(getResources()))
            layoutManager = new LinearLayoutManager(getContext());
        else
            layoutManager = new GridLayoutManager(getContext(), 2);

        mRVAdapter = new OnlineDevicesAdapter(clickListener);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRVAdapter);

        mNetworkDeviceViewModel.getAllDevices().observe(getViewLifecycleOwner(), networkDevices -> {
            mList = new ArrayList<>();

            for (NetworkDevice networkDevice : networkDevices) {
                String deviceId = networkDevice.getDeviceId();
                SSDevice foundedDevice = mDeviceViewModel.findDeviceDep(deviceId);

                if (foundedDevice == null)
                    continue;

                mList.add(foundedDevice);
            }

            mRVAdapter.setDevicesList(mList);
            updateUIState();
        });
    }

    /*
     * Following section represents the UI state machine
     * TODO: Wrap ViewMessage into widget
     * TODO: use CircleImageDrawable instead of progressbar
     */

    /* View Message */
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
        if (isServiceRunning) {
            boolean hasDevices = mDeviceViewModel.getDeviceCount() > 0;

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

    /**
     * Sets the state of the UI depending on the state of CommunicationService
     * @param serviceRunning CommunicationService state (running or not)
     */
    private void setServiceState(boolean serviceRunning) {
        isServiceRunning = serviceRunning;
        updateUIState();
    }

    /**
     * Sets the state of the UI
     * @param state Required UI state
     */
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
                    requireActivity().startService(intent);
                });

                replaceMessage(R.drawable.ic_service_off_24dp, R.string.text_serviceNotStarted, R.string.btn_start, startService);

                toggleMessage(true);
                break;

            case NoDevicesAdded:
                View.OnClickListener manageDevices = (v -> startActivity(new Intent(getContext(), DeviceManageActivity.class)));

                replaceMessage(R.drawable.ic_devices_24dp, R.string.text_noDevicesAdded, R.string.btn_manageDevices, manageDevices);
                toggleMessage(true);
                break;
        }

        currentUIState = state;
    }

    /**
     * Replaces one message with another (progressbar instead of icon)
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

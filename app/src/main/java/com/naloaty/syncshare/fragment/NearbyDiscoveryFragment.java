package com.naloaty.syncshare.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.adapter.DiscoveredDevicesAdapter;
import com.naloaty.syncshare.adapter.OnRVClickListener;
import com.naloaty.syncshare.database.device.NetworkDevice;
import com.naloaty.syncshare.database.device.NetworkDeviceViewModel;
import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.service.CommunicationService;
import com.naloaty.syncshare.util.AddDeviceHelper;
import com.naloaty.syncshare.util.AppUtils;
import com.naloaty.syncshare.util.DNSSDHelper;

import java.util.ArrayList;
import java.util.List;

public class NearbyDiscoveryFragment extends Fragment {

    private static final String TAG = "NearbyDiscoveryFragment";
    
    private List<NetworkDevice> mList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private DiscoveredDevicesAdapter mRVAdapter;
    private NetworkDeviceViewModel networkDeviceViewModel;

    private DNSSDHelper mDNSSDHelper;

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

        mDNSSDHelper = new DNSSDHelper(getContext());

        this.networkDeviceViewModel = new ViewModelProvider(this).get(NetworkDeviceViewModel.class);
        mFilter.addAction(CommunicationService.SERVICE_STATE_CHANGED);
    }


    @Override
    public void onResume()
    {
        super.onResume();
        setServiceState(AppUtils.isServiceRunning(getContext(), CommunicationService.class));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, mFilter);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.layout_nearby_discovery_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initMessage(view.findViewById(R.id.message_placeholder));

        mRecyclerView = view.findViewById(R.id.nearby_discovery_recycler_view);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        final AddDeviceHelper.AddDeviceCallback callback = new AddDeviceHelper.AddDeviceCallback() {
            @Override
            public void onSuccessfullyAdded() {
                DialogInterface.OnClickListener btnClose = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().onBackPressed();
                    }
                };

                new AlertDialog.Builder(getContext())
                        .setMessage(R.string.text_onSuccessfullyAdded)
                        .setPositiveButton(R.string.btn_close, btnClose)
                        .setTitle(R.string.title_success)
                        .show();
            }

            @Override
            public void onException(int errorCode) {
                Log.w(TAG, "Device added with exception. Error code is " + errorCode);

                DialogInterface.OnClickListener btnClose = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().onBackPressed();
                    }
                };

                int helpResource = R.string.text_defaultValue;
                int titleResource = R.string.text_defaultValue;

                switch (errorCode) {
                    case AddDeviceHelper.ERROR_ALREADY_ADDED:
                        Toast.makeText(getContext(), R.string.toast_alreadyAdded, Toast.LENGTH_LONG).show();
                        return;

                    case AddDeviceHelper.ERROR_UNTRUSTED_DEVICE:
                        break;

                    case AddDeviceHelper.ERROR_DEVICE_OFFLINE:
                        helpResource = R.string.text_offlineDevice;
                        break;

                    case AddDeviceHelper.ERROR_BAD_RESPONSE:
                    case AddDeviceHelper.ERROR_HANDSHAKE_EXCEPTION:
                        helpResource = R.string.text_handShakeException;
                        titleResource = R.string.title_step2;
                        break;

                    case AddDeviceHelper.ERROR_REQUEST_FAILED:
                    case AddDeviceHelper.ERROR_SENDING_FAILED:
                        helpResource = R.string.text_oExchangeFailed;
                        break;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setMessage(helpResource)
                        .setPositiveButton(R.string.btn_close, btnClose);

                if (titleResource != R.string.text_defaultValue)
                    builder.setTitle(titleResource);

                builder.show();
            }
        };

        final OnRVClickListener clickListener = new OnRVClickListener() {
            @Override
            public void onClick(int itemIndex) {
                NetworkDevice device = mList.get(itemIndex);

                SSDevice ssDevice = AddDeviceHelper.getEmptyDevice();
                ssDevice.setDeviceId(device.getDeviceId());
                ssDevice.setNickname(device.getDeviceName());
                ssDevice.setAppVersion(device.getAppVersion());

                AddDeviceHelper helper = new AddDeviceHelper(getContext(), ssDevice, callback);
                helper.processDevice();
            }
        };

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRVAdapter = new DiscoveredDevicesAdapter(clickListener);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRVAdapter);

        networkDeviceViewModel.getAllDevices().observe(getViewLifecycleOwner(), new Observer<List<NetworkDevice>>() {

            @Override
            public void onChanged(List<NetworkDevice> networkDevices) {
                mList = networkDevices;
                mRVAdapter.setDevicesList(networkDevices);
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
        NoDevicesFound,
        ServiceNotRunning;
    }

    private void updateUIState() {
        setUIState(getRequiredState());
    }

    private void setServiceState(boolean serviceStarted) {
        isServiceRunning = serviceStarted;
        updateUIState();
    }

    private UIState getRequiredState() {
        if (isServiceRunning) {
            boolean hasItems = mList.size() > 0;

            if (hasItems)
                return UIState.OnlineShown;
            else
                return UIState.NoDevicesFound;
        }
        else
            return UIState.ServiceNotRunning;
    }

    private void setUIState(UIState state)
    {
        if (currentUIState == state)
            return;

        switch (state) {
            case OnlineShown:
                toggleMessage(false);
                break;

            case NoDevicesFound:
                replaceMessage(R.drawable.ic_devices_24dp, R.string.text_discoveringDevices);
                toggleMessage(true);
                break;

            case ServiceNotRunning:
                View.OnClickListener listener = (v -> {
                    Intent intent = new Intent(getContext(), CommunicationService.class);
                    getActivity().startService(intent);
                });

                replaceMessage(R.drawable.ic_service_off_24dp, R.string.text_serviceNotStarted, R.string.btn_start, listener);
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

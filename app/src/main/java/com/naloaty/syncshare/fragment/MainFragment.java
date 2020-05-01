package com.naloaty.syncshare.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import com.naloaty.syncshare.activity.AddDeviceActivity;
import com.naloaty.syncshare.activity.DeviceManageActivity;
import com.naloaty.syncshare.activity.LocalDeviceActivity;
import com.naloaty.syncshare.adapter.CategoryAdapter;
import com.naloaty.syncshare.adapter.base.BodyItem;
import com.naloaty.syncshare.adapter.base.Category;
import com.naloaty.syncshare.adapter.custom.ActionMessage;
import com.naloaty.syncshare.adapter.custom.DefaultHeader;
import com.naloaty.syncshare.adapter.custom.DiscoveredDevice;
import com.naloaty.syncshare.adapter.custom.OnlineDevice;
import com.naloaty.syncshare.config.AppConfig;
import com.naloaty.syncshare.database.NetworkDevice;
import com.naloaty.syncshare.database.NetworkDeviceViewModel;
import com.naloaty.syncshare.database.SSDevice;
import com.naloaty.syncshare.database.SSDeviceViewModel;
import com.naloaty.syncshare.service.CommunicationService;
import com.naloaty.syncshare.util.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private ArrayList<Category> mList;
    private RecyclerView mRecyclerView;
    private CategoryAdapter mCategoryAdapter;
    private NetworkDeviceViewModel mNetworkDeviceViewModel;
    private SSDeviceViewModel mSSDeviceViewModel;

    /* Message view */
    private ImageView mMessageIcon;
    private TextView mMessageText;
    private AppCompatButton mMessageActionBtn;
    private LinearLayout mMessageBtnLayout;
    private ViewGroup mMessagePlaceholder;

    /* Local device */
    private TextView mAlbums;
    private TextView mPhotos;
    private TextView mVideos;
    private LinearLayout mLocalDeviceLayout;

    private UIState currentUIState;

    private enum UIState {
        OnlineShown,
        NoOnlineDevices,
        ServiceNotRunning,
        NoDevicesAdded;
    }

    private boolean isServiceRunning = false;

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

        /* View Message*/
        mMessageIcon = view.findViewById(R.id.message_icon);
        mMessageText = view.findViewById(R.id.message_text);
        mMessageActionBtn = view.findViewById(R.id.message_action_button);
        mMessageBtnLayout = view.findViewById(R.id.message_btn_layout);
        mMessagePlaceholder = view.findViewById(R.id.message_placeholder);

        /* Local device */
        mAlbums = view.findViewById(R.id.local_device_albums);
        mPhotos = view.findViewById(R.id.local_device_photos);
        mVideos = view.findViewById(R.id.local_device_videos);
        mLocalDeviceLayout = view.findViewById(R.id.local_device_layout);

        mLocalDeviceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), LocalDeviceActivity.class));
            }
        });

        /* RecyclerView */
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mCategoryAdapter = new CategoryAdapter();

        mRecyclerView = view.findViewById(R.id.main_fragment_devices_online);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mCategoryAdapter);

        setupRecyclerView();
    }

    private UIState getRequiredState() {
        if (isServiceRunning) {

            boolean hasDevices = mSSDeviceViewModel.getDeviceCount() > 0;

            if (!hasDevices)
                return UIState.NoDevicesAdded;

            boolean hasOnline = mNetworkDeviceViewModel.getDeviceCount() > 0;

            if (hasOnline)
            {
                List<NetworkDevice> list = mNetworkDeviceViewModel.getAllDevicesList();
                if (list == null)
                    return UIState.NoOnlineDevices;

                for (NetworkDevice netDevice: list) {
                    SSDevice device = mSSDeviceViewModel.findDevice(netDevice.getDeviceId());

                    if (device != null)
                        return UIState.OnlineShown;
                }

                return UIState.NoOnlineDevices;
            }
            else
                return UIState.NoOnlineDevices;
        }
        else
        {
            return UIState.ServiceNotRunning;
        }
    }

    private void setUIState(UIState uiState) {
        if (currentUIState == uiState)
            return;

        switch (uiState) {
            case OnlineShown:
                toggleMessage(false);
                break;

            case NoOnlineDevices:
                setMessageAnim(R.drawable.ic_wifi_24dp, R.string.text_noOnlineDevices, 0, null);
                toggleMessage(true);
                break;

            case ServiceNotRunning:
                View.OnClickListener startService = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), CommunicationService.class);
                        //TODO: kostyl
                        intent.setAction("kostyl");
                        getActivity().startService(intent);
                    }
                };

                setMessageAnim(R.drawable.ic_service_off_24dp, R.string.text_serviceNotStarted, R.string.btn_start, startService);

                toggleMessage(true);
                break;

            case NoDevicesAdded:
                View.OnClickListener manageDevices = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getContext(), DeviceManageActivity.class));
                    }
                };

                setMessageAnim(R.drawable.ic_devices_24dp, R.string.text_noDevicesAdded, R.string.btn_manageDevices, manageDevices);
                toggleMessage(true);
                break;
        }

        currentUIState = uiState;
    }

    private void setMessageAnim(int iconResource, int textResource, int btnResource, View.OnClickListener listener) {

        int currState = mMessagePlaceholder.getVisibility();

        if (currState == View.VISIBLE) {
            mMessagePlaceholder
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

        int currSate = mMessagePlaceholder.getVisibility();

        if (mMessagePlaceholder.getAlpha() < 1 && currSate == View.VISIBLE) {
            mMessagePlaceholder
                    .animate()
                    .setDuration(300)
                    .alpha(1)
                    .setListener(null);
        }
    }

    private void setServiceState(boolean serviceStarted) {

        isServiceRunning = serviceStarted;
        setUIState(getRequiredState());
    }

    private void toggleMessage(boolean isVisible) {
        //mMessagePlaceholder.setVisibility(isVisible ? View.VISIBLE : View.GONE);

        int curentState = mMessagePlaceholder.getVisibility();

        if (isVisible) {
            if (curentState == View.VISIBLE)
                return;

            mRecyclerView.setVisibility(View.GONE);
            mMessagePlaceholder.setAlpha(0);
            mMessagePlaceholder.setVisibility(View.VISIBLE);
            mMessagePlaceholder.animate()
                    .alpha(1)
                    .setDuration(150)
                    .setListener(null);
        }
        else
        {
            if (curentState == View.GONE)
                return;

            mMessagePlaceholder.setVisibility(View.VISIBLE);
            mMessagePlaceholder.setAlpha(1);
            mMessagePlaceholder.animate()
                    .alpha(0)
                    .setDuration(150)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mMessagePlaceholder.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mCategoryAdapter = new CategoryAdapter();

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mCategoryAdapter);

        final BodyItem.OnItemClickListener localDeviceListener = new BodyItem.OnItemClickListener() {
            @Override
            public void onItemClick(BodyItem item) {

            }
        };

        mNetworkDeviceViewModel.getAllDevices().observe(getViewLifecycleOwner(), new Observer<List<NetworkDevice>>() {
            @Override
            public void onChanged(List<NetworkDevice> networkDevices) {

                mList = new ArrayList<>();

                /* Online devices */
                Category onlineDevices = new Category(null);

                for (NetworkDevice networkDevice : networkDevices) {
                    String deviceId = networkDevice.getDeviceId();
                    SSDevice foundedDevice = mSSDeviceViewModel.findDevice(deviceId);

                    if (foundedDevice == null)
                        continue;

                    int iconResource;
                    String[] appVersion = foundedDevice.getAppVersion().split("::");
                    switch (appVersion[1]) {

                        case AppConfig.PLATFORM_MOBILE:
                            iconResource = R.drawable.ic_phone_android_24dp;
                            break;

                        case AppConfig.PLATFORM_DESKTOP:
                            iconResource = R.drawable.ic_desktop_windows_24dp;
                            break;

                        default:
                            iconResource = R.drawable.ic_warning_24dp;
                            break;
                    }

                    OnlineDevice onlineDevice = new OnlineDevice(deviceId, foundedDevice.getNickname(), "somedata", iconResource);
                    onlineDevices.addItem(onlineDevice);
                }

                if (onlineDevices.getItemsCount() > 0)
                    mList.add(onlineDevices);


                setUIState(getRequiredState());
                mCategoryAdapter.setItems(mList);
            }
        });
    }
}

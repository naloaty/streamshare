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
import com.naloaty.syncshare.activity.AddDeviceActivity;
import com.naloaty.syncshare.activity.DeviceManageActivity;
import com.naloaty.syncshare.adapter.CategoryAdapter;
import com.naloaty.syncshare.adapter.base.BodyItem;
import com.naloaty.syncshare.adapter.base.Category;
import com.naloaty.syncshare.adapter.custom.DiscoveredDevice;
import com.naloaty.syncshare.config.AppConfig;
import com.naloaty.syncshare.database.NetworkDevice;
import com.naloaty.syncshare.database.NetworkDeviceViewModel;
import com.naloaty.syncshare.database.SSDevice;
import com.naloaty.syncshare.dialog.EnterDeviceIdDialog;
import com.naloaty.syncshare.dialog.SingleTextInputDialog;
import com.naloaty.syncshare.service.CommunicationService;
import com.naloaty.syncshare.util.AddDeviceHelper;
import com.naloaty.syncshare.util.AppUtils;
import com.naloaty.syncshare.widget.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.List;

public class NearbyDiscoveryFragment extends Fragment {

    private static final String TAG = "NearbyDiscoveryFragment";
    
    private ArrayList<Category> mList;
    private RecyclerView mRecyclerView;
    private CategoryAdapter mCategoryAdapter;
    private NetworkDeviceViewModel networkDeviceViewModel;

    /* Message view */
    private ImageView mMessageIcon;
    private TextView mMessageText;
    private AppCompatButton mMessageActionBtn;
    private LinearLayout mMessageBtnLayout;
    private ViewGroup mMessagePlaceholder;

    private UIState currentUIState;
    private boolean isServiceRunning = false;

    private enum UIState {
        OnlineShown,
        NoDevicesFound,
        ServiceNotRunning;
    }

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

       // this.mDNSSDHelper = AppUtils.getDNSSDHelper(getActivity().getApplicationContext());
        this.networkDeviceViewModel = new ViewModelProvider(this).get(NetworkDeviceViewModel.class);

        mFilter.addAction(CommunicationService.SERVICE_STATE_CHANGED);
    }

    private void setServiceState(boolean serviceStarted) {
        isServiceRunning = serviceStarted;
        setUIState(getRequiredState());
    }

    private UIState getRequiredState() {
        if (isServiceRunning) {
            boolean hasDevices = networkDeviceViewModel.getDeviceCount() > 0;

            if (hasDevices)
                return UIState.OnlineShown;
            else
                return UIState.NoDevicesFound;
        }
        else
            return UIState.ServiceNotRunning;
    }

    private void setUIState(UIState uiState) {
        if (currentUIState == uiState)
            return;

        switch (uiState) {
            case OnlineShown:
                toggleMessage(false);
                break;

            case NoDevicesFound:
                setMessageAnim(R.drawable.ic_devices_24dp, R.string.text_discoveringDevices, 0, null);
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

        /* View Message*/
        mMessageIcon = view.findViewById(R.id.message_icon);
        mMessageText = view.findViewById(R.id.message_text);
        mMessageActionBtn = view.findViewById(R.id.message_action_button);
        mMessageBtnLayout = view.findViewById(R.id.message_btn_layout);
        mMessagePlaceholder = view.findViewById(R.id.message_placeholder);

        mRecyclerView = view.findViewById(R.id.nearby_discovery_recycler_view);

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mCategoryAdapter = new CategoryAdapter();

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mCategoryAdapter);

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

        BodyItem.OnItemClickListener clickListener = new BodyItem.OnItemClickListener() {
            @Override
            public void onItemClick(BodyItem item) {
                DiscoveredDevice device = (DiscoveredDevice)item;

                SSDevice ssDevice = AddDeviceHelper.getEmptyDevice();
                ssDevice.setDeviceId(device.getDeviceId());
                ssDevice.setNickname(device.getDeviceName());
                ssDevice.setAppVersion(device.getAppVersion());

                AddDeviceHelper helper = new AddDeviceHelper(getContext(), ssDevice, callback);
                helper.processDevice();
            }
        };

        networkDeviceViewModel.getAllDevices().observe(getViewLifecycleOwner(), new Observer<List<NetworkDevice>>() {
            @Override
            public void onChanged(List<NetworkDevice> networkDevices) {

                mList = new ArrayList<>();

                Category category = new Category(null);

                for(NetworkDevice networkDevice: networkDevices) {
                    int iconResource = 0;
                    String platform = networkDevice.getAppVersion().split("::")[1];
                    switch (platform) {

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

                    DiscoveredDevice device = new DiscoveredDevice(
                            networkDevice.getDeviceName(),
                            networkDevice.getAppVersion(),
                            networkDevice.getDeviceId(),
                            iconResource);

                    device.setOnItemClickListener(clickListener);
                    category.addItem(device);
                }

                if (category.getItemsCount() > 0)
                    mList.add(category);

                setUIState(getRequiredState());
                mCategoryAdapter.setItems(mList);
            }
        });
    }
}

package com.naloaty.streamshare.fragment;

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
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.streamshare.R;
import com.naloaty.streamshare.adapter.DiscoveredDevicesAdapter;
import com.naloaty.streamshare.adapter.OnRVClickListener;
import com.naloaty.streamshare.database.entity.NetworkDevice;
import com.naloaty.streamshare.database.viewmodel.NetworkDeviceViewModel;
import com.naloaty.streamshare.database.entity.SSDevice;
import com.naloaty.streamshare.service.CommunicationService;
import com.naloaty.streamshare.util.AddDeviceHelper;
import com.naloaty.streamshare.util.AppUtils;
import com.naloaty.streamshare.util.DeviceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment displays current devices on the network.
 * @see com.naloaty.streamshare.activity.AddDeviceActivity
 * @see AddOptionsFragment
 */
public class NearbyDiscoveryFragment extends Fragment {

    private static final String TAG = "NearbyDiscoveryFragment";

    private final IntentFilter mFilter = new IntentFilter();
    private List<NetworkDevice> mList = new ArrayList<>();
    private DiscoveredDevicesAdapter mRVAdapter;
    private NetworkDeviceViewModel mNetworkDeviceViewModel;

    /* UI elements */
    private LinearLayout mRootLayout;
    private RecyclerView mRecyclerView;

    /**
     * This callback is called by {@link AddDeviceHelper}
     * @see #setupRecyclerView()
     */
    private final AddDeviceHelper.AddDeviceCallback addDeviceCallback = new AddDeviceHelper.AddDeviceCallback() {
        @Override
        public void onSuccessfullyAdded() {
            DialogInterface.OnClickListener btnClose = (dialog, which) -> requireActivity().onBackPressed();

            new AlertDialog.Builder(requireContext())
                    .setMessage(R.string.text_onSuccessfullyAdded)
                    .setPositiveButton(R.string.btn_close, btnClose)
                    .setTitle(R.string.title_success)
                    .show();
        }

        @Override
        public void onException(int errorCode) {
            Log.w(TAG, String.format("Device added with exception (code %s)", errorCode));

            DialogInterface.OnClickListener btnClose = (dialog, which) -> requireActivity().onBackPressed();

            int helpResource = R.string.text_defaultValue;
            int titleResource = R.string.text_defaultValue;

            switch (errorCode) {
                case AddDeviceHelper.ERROR_ALREADY_ADDED:
                    Toast.makeText(requireContext(), R.string.toast_alreadyAdded, Toast.LENGTH_LONG).show();
                    return;

                case AddDeviceHelper.ERROR_UNTRUSTED_DEVICE:
                    return;

                case AddDeviceHelper.ERROR_DEVICE_OFFLINE:
                    helpResource = R.string.text_offlineDevice;
                    break;

                case AddDeviceHelper.ERROR_BAD_RESPONSE:
                case AddDeviceHelper.ERROR_SSL_PROTOCOL_EXCEPTION:
                    helpResource = R.string.text_handShakeException;
                    titleResource = R.string.title_step2;
                    break;

                case AddDeviceHelper.ERROR_REQUEST_FAILED:
                case AddDeviceHelper.ERROR_SENDING_FAILED:
                    helpResource = R.string.text_oExchangeFailed;
                    break;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                    .setMessage(helpResource)
                    .setPositiveButton(R.string.btn_close, btnClose);

            if (titleResource != R.string.text_defaultValue)
                builder.setTitle(titleResource);

            builder.show();
        }
    };

    /**
     * Receives a broadcast about CommunicationService state changes
     * @see #setServiceState(boolean)
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
        return inflater.inflate(R.layout.layout_nearby_discovery_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.nearby_discovery_recycler_view);
        mRootLayout = view.findViewById(R.id.root_linear_layout);

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

                if(height < (int)getResources().getDimension(R.dimen.nearby_discovery_min_height)) {
                    params.height = (int) getResources().getDimension(R.dimen.nearby_discovery_min_height);
                    mRootLayout.setLayoutParams(params);

                    Log.d(TAG, "Layout adjusted");
                }

            }
        };

        ViewTreeObserver viewTreeObserver = mRootLayout.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener);
    }

    /**
     * Initializes a list of current devices on the network.
     * @see AddDeviceHelper
     * @see NetworkDeviceViewModel#getAllDevices() 
     */
    private void setupRecyclerView() {
        final OnRVClickListener clickListener = itemIndex -> {
            NetworkDevice device = mList.get(itemIndex);

            SSDevice ssDevice = AddDeviceHelper.getEmptyDevice();
            ssDevice.setDeviceId(device.getDeviceId());
            ssDevice.setNickname(device.getDeviceName());
            ssDevice.setAppVersion(device.getAppVersion());

            AddDeviceHelper helper = new AddDeviceHelper(getContext(), ssDevice, addDeviceCallback);
            helper.processDevice();
        };

        RecyclerView.LayoutManager layoutManager;

        if (DeviceUtils.isPortrait(getResources()))
            layoutManager = new LinearLayoutManager(getContext());
        else
            layoutManager = new GridLayoutManager(getContext(), 2);

        mRVAdapter = new DiscoveredDevicesAdapter(clickListener);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRVAdapter);

        mNetworkDeviceViewModel.getAllDevices().observe(getViewLifecycleOwner(), networkDevices -> {
            mList = networkDevices;
            mRVAdapter.setDevicesList(networkDevices);
            updateUIState();
        });
    }

    /*
     * Following section represents the UI state machine
     * TODO: Wrap ViewMessage into widget
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
        ServiceNotRunning
    }

    /**
     * Sets the optimal state of the UI
     */
    private void updateUIState() {
        setUIState(getRequiredState());
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
     * Returns the optimal state of the UI.
     * @return Optimal UI state
     */
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

            case NoDevicesFound:
                replaceMessage(R.drawable.ic_devices_24dp, R.string.text_discoveringDevices);
                toggleMessage(true);
                break;

            case ServiceNotRunning:
                View.OnClickListener listener = (v -> {
                    Intent intent = new Intent(getContext(), CommunicationService.class);
                    requireActivity().startService(intent);
                });

                replaceMessage(R.drawable.ic_service_off_24dp, R.string.text_serviceNotStarted, R.string.btn_start, listener);
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

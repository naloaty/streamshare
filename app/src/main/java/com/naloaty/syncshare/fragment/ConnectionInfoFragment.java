package com.naloaty.syncshare.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.activity.PairDeviceActivity;
import com.naloaty.syncshare.util.ConnectionStateMonitor;

public class ConnectionInfoFragment extends Fragment{

    private static final String TAG = "ConnectionInfoFragment";

    private IntentFilter mIntentFilter = new IntentFilter();

    private ImageView mQRCode;
    private TextView mHelpText;
    private AppCompatButton mActionButton;
    private TextView mNetworkName;
    private RelativeLayout mNetworkNameLayout;
    private TextView mDeviceIpAddress;
    private RelativeLayout mDeviceipAddressLayout;

    private ConnectionStateMonitor mMonitor;

    private UIState currentUIState;

    private final IntentFilter mFilter = new IntentFilter();

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "Received Broadcast");

            if (ConnectionStateMonitor.NETWORK_MONITOR_STATE_CHANGED.equals(intent.getAction())
                    && intent.hasExtra(ConnectionStateMonitor.EXTRA_WIFI_CONNECTED)) {

                boolean wifiConnected = intent.getBooleanExtra(ConnectionStateMonitor.EXTRA_WIFI_CONNECTED, false);

                if (wifiConnected) {
                    mNetworkName.setText(intent.getStringExtra(ConnectionStateMonitor.EXTRA_NETWORK_NAME));
                    mDeviceIpAddress.setText(intent.getStringExtra(ConnectionStateMonitor.EXTRA_IP_ADDRESS));
                    setUIState(UIState.QRShown);
                }
                else
                    setUIState(UIState.WifiUnavilable);

            }
        }
    };

    private enum UIState {
        QRShown,
        WifiUnavilable,
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.layout_connection_info_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mQRCode = view.findViewById(R.id.connection_info_qr_code);
        mHelpText = view.findViewById(R.id.connection_info_help_text);
        mActionButton = view.findViewById(R.id.connection_info_action_btn);
        mNetworkName = view.findViewById(R.id.connection_info_network_name);
        mNetworkNameLayout = view.findViewById(R.id.connection_info_network_name_layout);
        mDeviceIpAddress = view.findViewById(R.id.connection_info_device_ip);
        mDeviceipAddressLayout = view.findViewById(R.id.connection_info_device_ip_layout);

        mActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMonitor = new ConnectionStateMonitor(getContext());

        //======= Init broadcast =========
        mFilter.addAction(ConnectionStateMonitor.NETWORK_MONITOR_STATE_CHANGED);
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, mFilter);
        mMonitor.startMonitor();

    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        mMonitor.stopMonitor();
    }

    private void setUIState(UIState state) {

        int helpTextResource;
        int actionButtonResource;
        int networkInfoVisibility;

        if (currentUIState != null && currentUIState.equals(state))
            return;

        switch (state) {
            case QRShown:
                helpTextResource = R.string.text_connectionInfoHelp;
                actionButtonResource = R.string.btn_openWifiSettings;
                networkInfoVisibility = View.VISIBLE;
                break;

            case WifiUnavilable:
                helpTextResource = R.string.text_connectToWifiNetwork;
                actionButtonResource = R.string.btn_openWifiSettings;
                networkInfoVisibility = View.GONE;
                mQRCode.setImageResource(R.drawable.ic_qr_code_24dp);
                break;

            default:
                helpTextResource = R.string.text_defaultValues;
                actionButtonResource = R.string.text_defaultValues;
                networkInfoVisibility = View.VISIBLE;
                mQRCode.setImageResource(R.drawable.ic_qr_code_24dp);
        }

        mHelpText.setText(helpTextResource);
        mActionButton.setText(actionButtonResource);
        mNetworkNameLayout.setVisibility(networkInfoVisibility);
        mDeviceipAddressLayout.setVisibility(networkInfoVisibility);

        currentUIState = state;
    }

    private void changeFragment(PairFragment targetFragment) {

        getContext().sendBroadcast(new Intent(PairDeviceActivity.ACTION_CHANGE_FRAGMENT)
                .putExtra(PairDeviceActivity.EXTRA_TARGET_FRAGMENT, targetFragment.toString()));
    }
}

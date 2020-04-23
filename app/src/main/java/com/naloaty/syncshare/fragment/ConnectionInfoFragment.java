package com.naloaty.syncshare.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.GlideApp;
import com.naloaty.syncshare.util.NetworkStateMonitor;
import com.naloaty.syncshare.util.PermissionHelper;

import org.json.JSONObject;

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

    private NetworkStateMonitor mMonitor;

    private UIState currentUIState;

    private final IntentFilter mFilter = new IntentFilter();

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (NetworkStateMonitor.NETWORK_MONITOR_STATE_CHANGED.equals(intent.getAction())) {
                setUIState(getRequiredState());
            }
        }
    };


    private enum UIState {
        QRShown,
        WifiUnavailable,
        LocationServiceDisabled,
        LocationPermissionDenied
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
                switch (currentUIState) {
                    case QRShown:
                    case WifiUnavailable:
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        break;

                    case LocationServiceDisabled:
                        PermissionHelper.requestLocationService(getActivity());
                        break;

                    case LocationPermissionDenied:
                        PermissionHelper.requestLocationPermission(getActivity(), PermissionHelper.REQUEST_LOCATION_PERMISSION);
                        break;
                }
            }
        });

        mMonitor = new NetworkStateMonitor(getContext());
        mFilter.addAction(NetworkStateMonitor.NETWORK_MONITOR_STATE_CHANGED);

        setUIState(getRequiredState());
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionHelper.REQUEST_LOCATION_PERMISSION) {
            setUIState(getRequiredState());
        }
    }

    private UIState getRequiredState() {
        int networkType = mMonitor.getCurrentNetworkType();
        boolean locationGranted = PermissionHelper.checkLocationPermission(getContext());
        boolean locationServiceEnabled = PermissionHelper.checkLocationService(getContext());

        if (locationGranted)
            if (locationServiceEnabled)
                if (networkType == NetworkStateMonitor.NETWORK_TYPE_WIFI)
                    return UIState.QRShown;
                else
                    return UIState.WifiUnavailable;

            else
                return UIState.LocationServiceDisabled;

        else
            return UIState.LocationPermissionDenied;
    }

    private void setQRCode(String wifiSSID, String ipAddress) {
        JSONObject json = new JSONObject();

        try{
            json.put(NetworkStateMonitor.JSON_WIFI_SSID, wifiSSID);
            json.put(NetworkStateMonitor.JSON_WIFI_IP_ADDRESS, ipAddress);

            setQRCode(json);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setQRCode(JSONObject networkInfo) {

        if (networkInfo == null) {
            mQRCode.setImageResource(R.drawable.ic_scan_qr_code_24dp);
            return;
        }

        try {
            MultiFormatWriter formatWriter = new MultiFormatWriter();

            BitMatrix bitMatrix = formatWriter.encode(networkInfo.toString(), BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder encoder = new BarcodeEncoder();

            Bitmap bitmap = encoder.createBitmap(bitMatrix);

            GlideApp.with(getContext())
                    .load(bitmap)
                    .into(mQRCode);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setNetworkInfo(JSONObject networkInfo) {

        if (networkInfo == null){
            mNetworkName.setText(R.string.text_defaultValue);
            mDeviceIpAddress.setText(R.string.text_defaultValue);
            return;
        }

        try {
            if (networkInfo.has(NetworkStateMonitor.JSON_WIFI_SSID))
                mNetworkName.setText(networkInfo.getString(NetworkStateMonitor.JSON_WIFI_SSID));
            else
                mNetworkName.setText(R.string.text_defaultValue);

            if (networkInfo.has(NetworkStateMonitor.JSON_WIFI_IP_ADDRESS))
                mDeviceIpAddress.setText(networkInfo.getString(NetworkStateMonitor.JSON_WIFI_IP_ADDRESS));
            else
                mDeviceIpAddress.setText(R.string.text_defaultValue);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUIState(UIState state) {

        int helpTextResource;
        int actionButtonResource;
        int networkInfoVisibility;

        //TODO: fix this in other way (create method getCurrentState() based on views state)
        /*if (currentUIState != null && currentUIState.equals(state))
            return;*/

        switch (state) {
            case QRShown:
                helpTextResource = R.string.text_connectionInfoHelp;
                actionButtonResource = R.string.btn_openWifiSettings;
                networkInfoVisibility = View.VISIBLE;

                JSONObject wifiInfo = mMonitor.getCurrentWifiInfo();
                setNetworkInfo(wifiInfo);

                if (wifiInfo.has(NetworkStateMonitor.JSON_WIFI_SSID))
                    wifiInfo.remove(NetworkStateMonitor.JSON_WIFI_SSID);

                setQRCode(wifiInfo);
                break;

            case WifiUnavailable:
                helpTextResource = R.string.text_wifiNetworkUnavailable;
                actionButtonResource = R.string.btn_openWifiSettings;
                networkInfoVisibility = View.GONE;
                break;

            case LocationServiceDisabled:
                helpTextResource = R.string.text_locationServiceDisabled;
                actionButtonResource = R.string.btn_enableLocationService;
                networkInfoVisibility = View.GONE;
                break;

            case LocationPermissionDenied:
                helpTextResource = R.string.text_locationPermissionDenied;
                actionButtonResource = R.string.btn_ask;
                networkInfoVisibility = View.GONE;
                break;

            default:
                helpTextResource = R.string.text_defaultValue;
                actionButtonResource = R.string.text_defaultValue;
                networkInfoVisibility = View.GONE;
        }

        if (!state.equals(UIState.QRShown))
            mQRCode.setImageResource(R.drawable.ic_qr_code_24dp);

        mHelpText.setText(helpTextResource);
        mActionButton.setText(actionButtonResource);
        mNetworkNameLayout.setVisibility(networkInfoVisibility);
        mDeviceipAddressLayout.setVisibility(networkInfoVisibility);

        currentUIState = state;
    }

}

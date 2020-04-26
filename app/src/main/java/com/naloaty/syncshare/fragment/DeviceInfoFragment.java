package com.naloaty.syncshare.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.GlideApp;
import com.naloaty.syncshare.config.AppConfig;
import com.naloaty.syncshare.util.AppUtils;
import com.naloaty.syncshare.util.EncryptionUtils;

import org.json.JSONObject;

public class DeviceInfoFragment extends Fragment{

    private static final String TAG = "DeviceInfoFragment";

    public static final String
                    QR_CODE_DEVICE_NICKNAME = "deviceName",
                    QR_CODE_DEVICE_ID = "deviceId",
                    QR_CODE_APP_VERSION = "appVersion";


    //private IntentFilter mIntentFilter = new IntentFilter();

    private ImageView mQRCode;
    private TextView mHelpText;
    private TextView mDeviceName;
    private TextView mAppVersion;
    private TextView mDeviceId;
    private LinearLayout mTitleLayout;
    private RelativeLayout mDeviceNameLayout;
    private RelativeLayout mAppVersionLayout;

    //private NetworkStateMonitor mMonitor;

    private UIState currentUIState;

    //private final IntentFilter mFilter = new IntentFilter();

   /* private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (NetworkStateMonitor.NETWORK_MONITOR_STATE_CHANGED.equals(intent.getAction())) {
                setUIState(getRequiredState());
            }
        }
    };*/


    private enum UIState {
        QRShown,
        NoDeviceId
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.layout_device_info_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mQRCode = view.findViewById(R.id.device_info_qr_code);
        mHelpText = view.findViewById(R.id.device_info_help_text);
        mDeviceName = view.findViewById(R.id.device_info_name);
        mAppVersion = view.findViewById(R.id.device_info_app_version);
        mDeviceId = view.findViewById(R.id.device_info_device_id);
        mTitleLayout = view.findViewById(R.id.device_info_title_layout);
        mDeviceNameLayout = view.findViewById(R.id.device_info_device_name_layout);
        mAppVersionLayout = view.findViewById(R.id.device_app_version_layout);


        /*mActionButton.setOnClickListener(new View.OnClickListener()
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
        });*/

        /*mMonitor = new NetworkStateMonitor(getContext());
        mFilter.addAction(NetworkStateMonitor.NETWORK_MONITOR_STATE_CHANGED);*/

        setUIState(getRequiredState());
    }

   /* @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, mFilter);
        //mMonitor.startMonitor();

    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        //mMonitor.stopMonitor();
    }*/

   /* @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionHelper.REQUEST_LOCATION_PERMISSION) {
            setUIState(getRequiredState());
        }
    }*/

    private UIState getRequiredState() {
        /*int networkType = mMonitor.getCurrentNetworkType();
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
            return UIState.LocationPermissionDenied;*/

        if (EncryptionUtils.checkStuff(getContext()))
            return UIState.QRShown;
        else
            return UIState.NoDeviceId;
    }

    private void setQRCode() {
        JSONObject json = new JSONObject();

        try{
            json.put(QR_CODE_DEVICE_NICKNAME, AppUtils.getLocalDeviceName());
            json.put(QR_CODE_APP_VERSION, AppConfig.APP_VERSION);
            json.put(QR_CODE_DEVICE_ID, AppUtils.getDeviceId(getContext()));

            setQRCode(json);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDeviceInfo() {
        mDeviceName.setText(AppUtils.getLocalDeviceName());
        mAppVersion.setText(AppConfig.APP_VERSION);
        mDeviceId.setText(AppUtils.getDeviceId(getContext()));
    }

    private void setQRCode(JSONObject deviceInfo) {

        if (deviceInfo == null) {
            mQRCode.setImageResource(R.drawable.ic_scan_qr_code_24dp);
            return;
        }

        try {
            MultiFormatWriter formatWriter = new MultiFormatWriter();

            BitMatrix bitMatrix = formatWriter.encode(deviceInfo.toString(), BarcodeFormat.QR_CODE, 400, 400);
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

    private void setUIState(UIState state) {

        int helpTextResource;
        int deviceInfoVisibility;

        //TODO: fix this in other way (create method getCurrentState() based on views state)
        /*if (currentUIState != null && currentUIState.equals(state))
            return;*/

        switch (state) {
            case QRShown:
                helpTextResource = R.string.text_deviceInfoHelp;
                deviceInfoVisibility = View.VISIBLE;

                setDeviceInfo();
                setQRCode();
                break;

            case NoDeviceId:
                helpTextResource = R.string.text_noDeviceIdError;
                deviceInfoVisibility = View.GONE;
                mDeviceId.setText(R.string.text_defaultValue);
                break;


            default:
                helpTextResource = R.string.text_defaultValue;
                deviceInfoVisibility = View.GONE;
                mDeviceId.setText(R.string.text_defaultValue);
        }

        if (!state.equals(UIState.QRShown))
            mQRCode.setImageResource(R.drawable.ic_qr_code_24dp);

        mHelpText.setText(helpTextResource);

        mTitleLayout.setVisibility(deviceInfoVisibility);
        mDeviceNameLayout.setVisibility(deviceInfoVisibility);
        mAppVersionLayout.setVisibility(deviceInfoVisibility);

        currentUIState = state;
    }

}

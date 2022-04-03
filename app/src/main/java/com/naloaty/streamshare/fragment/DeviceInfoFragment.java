package com.naloaty.streamshare.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.naloaty.streamshare.R;
import com.naloaty.streamshare.config.AppConfig;
import com.naloaty.streamshare.security.SecurityUtils;
import com.naloaty.streamshare.util.AppUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This fragment displays general information about the current device, including device ID and name.
 * All this information is encoded into a QR code.
 * @see com.naloaty.streamshare.activity.AddDeviceActivity
 */
public class DeviceInfoFragment extends Fragment{

    public static final String
                    QR_CODE_DEVICE_NICKNAME = "deviceName",
                    QR_CODE_DEVICE_ID       = "deviceId",
                    QR_CODE_APP_VERSION     = "appVersion";

    /* UI elements */
    private ImageView mQRCode;
    private TextView mHelpText;
    private TextView mDeviceName;
    private TextView mAppVersion;
    private TextView mDeviceId;
    private RelativeLayout mDeviceNameLayout;
    private RelativeLayout mAppVersionLayout;

    private UIState currentUIState;

    private enum UIState {
        QRShown,
        NoDeviceId
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_device_info_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mQRCode = view.findViewById(R.id.device_info_qr_code);
        mHelpText = view.findViewById(R.id.device_info_help_text);
        mDeviceName = view.findViewById(R.id.device_info_name);
        mAppVersion = view.findViewById(R.id.device_info_app_version);
        mDeviceId = view.findViewById(R.id.device_info_device_id);
        mDeviceNameLayout = view.findViewById(R.id.device_info_device_name_layout);
        mAppVersionLayout = view.findViewById(R.id.device_app_version_layout);

        setUIState(getRequiredState());
    }

    /**
     * Returns the optimal state of the UI.
     * @return Optimal UI state
     * @see SecurityUtils
     */
    private UIState getRequiredState() {
        //Checks if SSL certificate is presented
        if (SecurityUtils.checkSecurityStuff(requireContext().getFilesDir(), false))
            return UIState.QRShown;
        else
            return UIState.NoDeviceId;
    }

    /**
     * Sets the state of the UI
     * @param state Required UI state
     * @see #setDeviceInfo()
     * @see #setQRCode()
     */
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

        mDeviceNameLayout.setVisibility(deviceInfoVisibility);
        mAppVersionLayout.setVisibility(deviceInfoVisibility);

        currentUIState = state;
    }

    /**
     * Displays general information about the current device.
     */
    private void setDeviceInfo() {
        mDeviceName.setText(AppUtils.getLocalDeviceName());
        mAppVersion.setText(AppConfig.APP_VERSION);
        mDeviceId.setText(AppUtils.getDeviceId(requireContext()));
    }

    /**
     * Encodes general information about the current device into a QR code.
     * @see #setQRCode(JSONObject)
     */
    private void setQRCode() {
        /* Catches exceptions that may be caused by incorrect qr code content */
        try
        {
            JSONObject json = new JSONObject();
            json.put(QR_CODE_DEVICE_NICKNAME, AppUtils.getLocalDeviceName());
            json.put(QR_CODE_APP_VERSION, AppConfig.APP_VERSION);
            json.put(QR_CODE_DEVICE_ID, AppUtils.getDeviceId(requireContext()));

            setQRCode(json);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Encodes JSONObject into a QR code and displays it.
     * @param contents Contents
     */
    private void setQRCode(JSONObject contents) {
        if (contents == null) {
            mQRCode.setImageResource(R.drawable.ic_scan_qr_code_24dp);
            return;
        }

        /* Catches an exception that may be caused by a qr code write error */
        try {
            MultiFormatWriter formatWriter = new MultiFormatWriter();

            BitMatrix bitMatrix = formatWriter.encode(contents.toString(), BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(bitMatrix);

            Glide.with(requireContext())
                    .load(bitmap)
                    .into(mQRCode);
        }
        catch (WriterException e) {
            e.printStackTrace();
        }
    }

}

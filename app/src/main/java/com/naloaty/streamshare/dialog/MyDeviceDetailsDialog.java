package com.naloaty.streamshare.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.naloaty.streamshare.R;
import com.naloaty.streamshare.config.AppConfig;
import com.naloaty.streamshare.database.device.SSDevice;
import com.naloaty.streamshare.database.device.SSDeviceRepository;

/**
 * This class represents a dialog that displays general information about a trusted device.
 * @see com.naloaty.streamshare.fragment.MyDevicesFragment
 */
public class MyDeviceDetailsDialog extends ManualDismissDialog {

    private Context mContext;
    private AlertDialog mDialog;
    private SSDevice mSSDevice;

    /* UI elements */
    private ViewGroup mView;
    private ImageView mDeviceIcon;
    private TextView mDeviceNickname;
    private TextView mDeviceId;
    private TextView mDeviceName;
    private TextView mAppVersion;
    private Switch mAllowAccess;

    public MyDeviceDetailsDialog(@NonNull Context context) {
        super(context);

        mView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.layout_dialog_my_device_details, null);
        mDeviceIcon = mView.findViewById(R.id.my_device_details_device_icon);
        mDeviceNickname = mView.findViewById(R.id.my_device_details_device_nickname);
        mDeviceId = mView.findViewById(R.id.my_device_details_device_id);
        mDeviceName = mView.findViewById(R.id.my_device_details_device_name);
        mAppVersion = mView.findViewById(R.id.my_device_details_app_version);
        mAllowAccess = mView.findViewById(R.id.my_device_details_allow_access);

        setPositiveButton(R.string.btn_close, null);

        setNegativeButtonS(R.string.btn_remove, dialog -> {
            removeDevice();
            return false;
        });

        mAllowAccess.setOnCheckedChangeListener((buttonView, isChecked) -> setAccessAllowed(isChecked));

        setView(mView);
        mContext = context;
    }

    /**
     * Writes the status of the device (access is allowed or not) to the database.
     * @param accessAllowed True if this device has access to local albums.
     */
    private void setAccessAllowed(boolean accessAllowed) {
        mSSDevice.setAccessAllowed(accessAllowed);
        SSDeviceRepository repository = new SSDeviceRepository(mContext);
        repository.update(mSSDevice);
    }

    /**
     * Removes a trusted device from the database.
     * Also shows a dialog that asks the user to confirm the removal of the device.
     */
    private void removeDevice() {
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();

        new AlertDialog.Builder(mContext)
                .setMessage(R.string.text_confirmRemoval)
                .setNegativeButton(R.string.btn_cancel, (dialog, which) -> {
                    if (mDialog != null && !mDialog.isShowing())
                        mDialog.show();
                })
                .setPositiveButton(R.string.btn_remove, (dialog, which) -> {
                    if (mSSDevice != null) {
                        SSDeviceRepository repository = new SSDeviceRepository(mContext);
                        repository.delete(mSSDevice);
                    }
                })
                .show();
    }

    /**
     * Sets the trusted device whose general information will be displayed.
     * @param device Trusted device. Instance of {@link SSDevice}.
     */
    public void setSSDevice(SSDevice device) {
        if (device == null)
            return;

        mSSDevice = device;

        int iconResource;
        String[] appVersion = device.getAppVersion().split("::");
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

        mDeviceNickname.setText(device.getNickname());

        mDeviceIcon.setImageResource(iconResource);
        mDeviceId.setText(device.getDeviceId());

        String namePlaceholder = mContext.getString(R.string.text_deviceNamePlaceholder);
        mDeviceName.setText(String.format(namePlaceholder, device.getBrand(), device.getModel()));

        mAppVersion.setText(appVersion[0]);
        mAllowAccess.setChecked(device.isAccessAllowed());
    }

    /**
     * Shows the dialog.
     * NOTE: Control over dialog dismissing is in hands of ManualDismissDialog.OnClickListener();
     * @return Showed dialog.
     * @see ManualDismissDialog
     */
    @Override
    public AlertDialog show() {
        mDialog = super.show();
        return mDialog;
    }
}

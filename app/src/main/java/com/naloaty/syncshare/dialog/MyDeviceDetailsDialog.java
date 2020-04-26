package com.naloaty.syncshare.dialog;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.database.SSDevice;
import com.naloaty.syncshare.database.SSDeviceRepository;

public class MyDeviceDetailsDialog extends ManualDismissDialog {

    private ViewGroup mView;

    private ImageView mDeviceIcon;
    private TextView mDeviceNickname;
    private TextView mDeviceId;
    private TextView mDeviceName;
    private TextView mAppVersion;
    private Switch mAllowAccess;

    private Context mContext;
    private AlertDialog mDialog;
    private SSDevice mSSDevice;

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

        setNegativeButtonS(R.string.btn_remove, new OnClickListener() {
            @Override
            public boolean onClick(AlertDialog dialog) {
                removeDevice();
                return false;
            }
        });

        setView(mView);
        mContext = context;
    }

    public void removeDevice() {

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (mDialog != null && mDialog.isShowing())
                    mDialog.dismiss();

                if (mSSDevice != null) {
                    SSDeviceRepository repository = new SSDeviceRepository(mContext);
                    repository.delete(mSSDevice);
                }
            }
        };

        new AlertDialog.Builder(mContext)
                .setMessage(R.string.text_confirmRemoval)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_remove, listener)
                .show();
    }

    public void setSSDevice(SSDevice device) {
        if (device == null)
            return;

        mSSDevice = device;

        int iconResource = 0;
        switch (device.getAppPlatform()) {

            case SSDevice.PLATFORM_MOBILE:
                iconResource = R.drawable.ic_phone_android_24dp;
                break;

            case SSDevice.PLATFORM_DESKTOP:
                iconResource = R.drawable.ic_desktop_windows_24dp;
                break;

            case SSDevice.PLATFORM_UNKNOWN:
                iconResource = R.drawable.ic_warning_24dp;
                break;
        }

        if (!device.isVerified()) {
            iconResource = R.drawable.ic_warning_24dp;
            mDeviceNickname.setText(R.string.text_notVerifiedHelp);
            mDeviceNickname.setTextColor(ContextCompat.getColor(mContext, R.color.colorNotVerified));
            mAllowAccess.setEnabled(false);
        }
        else
            mDeviceNickname.setText(device.getNickname());

        mDeviceIcon.setImageResource(iconResource);

        mDeviceId.setText(device.getDeviceId());

        String namePlaceholder = mContext.getString(R.string.text_deviceNamePlaceholder);
        mDeviceName.setText(String.format(namePlaceholder, device.getBrand(), device.getModel()));

        mAppVersion.setText(device.getAppVersion());
        mAllowAccess.setChecked(device.isAccessAllowed());
    }

    @Override
    public AlertDialog show() {
        mDialog = super.show();

        return mDialog;
    }
}

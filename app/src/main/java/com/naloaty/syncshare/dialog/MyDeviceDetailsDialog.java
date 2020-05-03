package com.naloaty.syncshare.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.config.AppConfig;
import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.database.device.SSDeviceRepository;

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

        mAllowAccess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setAccessAllowed(isChecked);
            }
        });

        setView(mView);
        mContext = context;
    }

    private void setAccessAllowed(boolean accessAllowed) {
        mSSDevice.setAccessAllowed(accessAllowed);

        SSDeviceRepository repository = new SSDeviceRepository(mContext);
        repository.update(mSSDevice);
    }

    private void removeDevice() {

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

        int iconResource;
        String[] appVersion = device.getAppVersion().split("::");;
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

    @Override
    public AlertDialog show() {
        mDialog = super.show();

        return mDialog;
    }
}

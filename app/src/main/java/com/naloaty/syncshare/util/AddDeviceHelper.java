package com.naloaty.syncshare.util;

import android.content.Context;
import android.widget.Toast;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.database.SSDevice;
import com.naloaty.syncshare.database.SSDeviceRepository;

public class AddDeviceHelper {

    public static boolean proccessDevice(Context context, String deviceNickname, String appVersion, String deviceId) {

        SSDeviceRepository repository = new SSDeviceRepository(context);
        SSDevice device = repository.findDevice(deviceId);

        if (device != null) {
            Toast.makeText(context, R.string.toast_alreadyAdded, Toast.LENGTH_LONG).show();
            return false;
        }

        SSDevice newDevice = new SSDevice(deviceId, appVersion);
        newDevice.setNickname(deviceNickname);
        newDevice.setModel("-");
        newDevice.setBrand("-");
        newDevice.setLastUsageTime(System.currentTimeMillis());
        newDevice.setVerified(false);
        newDevice.setAccessAllowed(false);
        newDevice.setAppPlatform(SSDevice.PLATFORM_UNKNOWN);

        repository.insert(newDevice);

        return true;
    }

    public static boolean proccessDevice(Context context, String deviceId) {
        return proccessDevice(context, "-", "-", deviceId);
    }
}

package com.naloaty.syncshare.util;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;


import com.naloaty.syncshare.config.Keyword;
import com.naloaty.syncshare.database.NetworkDevice;
import com.naloaty.syncshare.database.NetworkDeviceRepository;
import com.naloaty.syncshare.database.SSDevice;
import com.naloaty.syncshare.database.SSDeviceRepository;

import org.json.JSONException;
import org.json.JSONObject;

public class NetworkDeviceManager {

    private static final String TAG = NetworkDeviceManager.class.getSimpleName();

    public static final int JOB_DETECTIVE_ID = 43123;

    public static void manageDevice(Context context, NetworkDevice networkDevice) {
        String myId = AppUtils.getDeviceId(context);

        if (networkDevice.getDeviceId().equals(myId))
            return;

        processDevice(context, networkDevice);
    }

    public static void processDevice(Context context, NetworkDevice networkDevice)
    {
        NetworkDeviceRepository repository = new NetworkDeviceRepository(context);
        NetworkDevice entry = repository.findDevice(networkDevice.getIpAddress(), networkDevice.getDeviceId(), networkDevice.getServiceName());

        if (entry != null)
            repository.delete(entry);

        repository.insert(networkDevice);

        Log.d(TAG, "Device with ip " + networkDevice.getIpAddress() + " and service name "
                + networkDevice.getServiceName() + " added to database");

    }

    public static SSDevice loadDeviceFromJson(JSONObject json) throws JSONException {
        JSONObject deviceInfo = json.getJSONObject(Keyword.DEVICE_INFO);

        SSDevice device = new SSDevice(deviceInfo.getString(Keyword.DEVICE_INFO_SERIAL), deviceInfo.getString(Keyword.APP_INFO_VERSION));

        device.setBrand(deviceInfo.getString(Keyword.DEVICE_INFO_BRAND));
        device.setModel(deviceInfo.getString(Keyword.DEVICE_INFO_MODEL));
        device.setNickname(deviceInfo.getString(Keyword.DEVICE_INFO_USER));
        device.setLastUsageTime(System.currentTimeMillis());

        return device;
    }

    public static void manageLostDevice(Context context, String serviceName) {

        NetworkDeviceRepository repository = new NetworkDeviceRepository(context);
        try {
            NetworkDevice connection = repository.findDevice(null, null, serviceName);

            if (connection != null){
                repository.delete(connection);
                Log.d(TAG, "Connection with service name " + serviceName + " removed from database");
            }
            else
                Log.d(TAG, "Connection with service name " + serviceName + " not found in database");

        }
        catch (Exception e) {
            Log.d(TAG, "Cannot manage lost device with service name " + serviceName);
        }
    }
}

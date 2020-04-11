package com.naloaty.syncshare.util;

import android.content.Context;
import android.util.Log;


import com.naloaty.syncshare.config.Keyword;
import com.naloaty.syncshare.database.DeviceConnection;
import com.naloaty.syncshare.database.DeviceConnectionRepository;
import com.naloaty.syncshare.other.NetworkDevice;

import org.json.JSONException;
import org.json.JSONObject;

public class NetworkDeviceManager {

    private static final String TAG = NetworkDeviceManager.class.getSimpleName();

    /*
     * [NsdHelper]                               [NetworkDeviceManager]
     * onServiceResolved(); --------------------> manageDevice(db, ip);
     */


    public static void manageDevice(Context context, DeviceConnection deviceConnection) {
        manageDevice(context, deviceConnection, false);
    }

    public static CommunicationBridge.Client manageDevice(final Context context, final DeviceConnection deviceConnection, boolean useCurrentThread) {

        CommunicationBridge.Client.ConnectionHandler connectionHandler =
                new CommunicationBridge.Client.ConnectionHandler() {
                    @Override
                    public void onConnect(CommunicationBridge.Client client) {
                        try {
                            NetworkDevice device = client.handleDevice(deviceConnection.getIpAddress());
                            client.setDevice(device);

                            if (device.deviceId != null) {

                                NetworkDevice localDevice = AppUtils.getLocalDevice(context);

                                if (localDevice.deviceId.contentEquals(device.deviceId))
                                    deviceConnection.setLocalDevice(true);
                                else
                                    device.lastUsageTime = System.currentTimeMillis();

                                processConnection(context, device, deviceConnection);
                            }
                        } catch (Exception e) {
                            Log.i(TAG, "Could not connect to device " + deviceConnection.getIpAddress() + ": " + e.getMessage());
                        }
                    }
                };

       Log.i(TAG, "Connecting to " + deviceConnection.getIpAddress());
       return CommunicationBridge.connect(context, useCurrentThread, connectionHandler);
    }

    public static void processConnection(Context context, NetworkDevice device, DeviceConnection deviceConnection)
    {

        deviceConnection.setLastCheckedDate(System.currentTimeMillis());
        deviceConnection.setDeviceId(device.deviceId);

        DeviceConnectionRepository repository = AppUtils.getDeviceConnectionRepository(context);
        DeviceConnection entry = repository.findConnection(deviceConnection.getIpAddress(), deviceConnection.getDeviceId(), deviceConnection.getServiceName());

        if (entry != null)
            repository.delete(entry);

        repository.insert(deviceConnection);

        Log.i(TAG, "Connection with ip " + deviceConnection.getIpAddress() + " and service name "
                + deviceConnection.getServiceName() + " added to database");

    }

    public static NetworkDevice loadDeviceFromJson(JSONObject json) throws JSONException {
        JSONObject deviceInfo = json.getJSONObject(Keyword.DEVICE_INFO);

        NetworkDevice device = new NetworkDevice(deviceInfo.getString(Keyword.DEVICE_INFO_SERIAL));

        device.brand = deviceInfo.getString(Keyword.DEVICE_INFO_BRAND);
        device.model = deviceInfo.getString(Keyword.DEVICE_INFO_MODEL);
        device.nickname = deviceInfo.getString(Keyword.DEVICE_INFO_USER);
        device.lastUsageTime = System.currentTimeMillis();

        return device;
    }

    public static void manageLostDevice(Context context, String serviceName) {

        DeviceConnectionRepository repository = AppUtils.getDeviceConnectionRepository(context);
        try {
            DeviceConnection connection = repository.findConnection(null, null, serviceName);

            if (connection != null){
                repository.delete(connection);
                Log.i(TAG, "Connection with service name " + serviceName + " removed from database");
            }
            else
                Log.i(TAG, "Connection with service name " + serviceName + " not found in database");

        }
        catch (Exception e) {
            Log.v(TAG, "Cannot manage lost device with service name " + serviceName);
        }
    }
}

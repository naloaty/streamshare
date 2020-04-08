package com.naloaty.syncshare.util;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import androidx.lifecycle.ViewModelProvider;

import com.naloaty.syncshare.config.Keyword;
import com.naloaty.syncshare.database.DeviceConnection;
import com.naloaty.syncshare.database.DeviceConnectionRepository;
import com.naloaty.syncshare.database.DeviceConnectionViewModel;
import com.naloaty.syncshare.other.NetworkDevice;

import org.json.JSONException;
import org.json.JSONObject;

public class NetworkDeviceManager {

    private static final String TAG = NetworkDeviceManager.class.getSimpleName();

    /*
     * [NsdHelper]                               [NetworkDeviceManager]
     * onServiceResolved(); --------------------> manageDevice(db, ip);
     */


    public static void manageDevice(Context context, String ipAddress, String serviceName) {
        manageDevice(context, ipAddress, serviceName, false);
    }

    public static CommunicationBridge.Client manageDevice(final Context context, final String ipAddress, final String serviceName, boolean useCurrentThread) {

        CommunicationBridge.Client.ConnectionHandler connectionHandler =
                new CommunicationBridge.Client.ConnectionHandler() {
                    @Override
                    public void onConnect(CommunicationBridge.Client client) {
                        try {
                            NetworkDevice device = client.handleDevice(ipAddress);
                            client.setDevice(device);

                            if (device.deviceId != null) {
                                NetworkDevice localDevice = AppUtils.getLocalDevice(context);

                                //Save information about connection to database (current connections)
                                processConnection(context, device, ipAddress, serviceName);

                                if (!localDevice.deviceId.equals(device.deviceId)) {
                                    device.lastUsageTime = System.currentTimeMillis();

                                    //Save information about device to database
                                    // database.publish(device);
                                }
                            }
                        } catch (Exception e) {
                            Log.i(TAG, "Could not connect to device " + ipAddress + " because: " + e.getMessage());
                        }
                    }
                };

       Log.i(TAG, "Connecting to " + ipAddress);
       return CommunicationBridge.connect(context, useCurrentThread, connectionHandler);
    }

    public static void processConnection(Context context, NetworkDevice device, String ipAddress, String serviceName)
    {
        DeviceConnection connection = new DeviceConnection(ipAddress);

        connection.setLastCheckedDate(System.currentTimeMillis());
        connection.setDeviceId(device.deviceId);
        connection.setServiceName(serviceName);

        DeviceConnectionRepository repository = AppUtils.getDeviceConnectionRepository(context);
        repository.insert(connection);

        Log.i(TAG, "Connection with ip " + ipAddress + " added to database. Service name: " + serviceName);

        /*database.remove(new SQLQuery.Select(AccessDatabase.TABLE_DEVICECONNECTION)
                .setWhere(AccessDatabase.FIELD_DEVICECONNECTION_DEVICEID + "=? AND "
                                + AccessDatabase.FIELD_DEVICECONNECTION_ADAPTERNAME + " =? AND "
                                + AccessDatabase.FIELD_DEVICECONNECTION_IPADDRESS + " != ?",
                        connection.deviceId, connection.adapterName, connection.ipAddress));

        database.publish(connection);*/
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
            DeviceConnection connection = repository.getConnectionByService(serviceName);

            if (connection != null){
                repository.delete(connection);
                Log.i(TAG, "Connection with service name " + serviceName + " removed to database");
            }
            else
                Log.i(TAG, "Connection with service name " + serviceName + " not found in database");

        }
        catch (Exception e) {
            Log.v(TAG, "Cannot manage lost device with service name " + serviceName);
        }
    }
}

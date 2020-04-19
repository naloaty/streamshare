package com.naloaty.syncshare.util;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;


import com.naloaty.syncshare.config.Keyword;
import com.naloaty.syncshare.database.DeviceConnection;
import com.naloaty.syncshare.database.DeviceConnectionRepository;
import com.naloaty.syncshare.other.NetworkDevice;
import com.naloaty.syncshare.service.DetectiveJobService;

import org.json.JSONException;
import org.json.JSONObject;

public class NetworkDeviceManager {

    private static final String TAG = NetworkDeviceManager.class.getSimpleName();

    public static final int JOB_DETECTIVE_ID = 43123;

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
                            processConnection(context, null, deviceConnection);
                        }
                    }
                };

       Log.i(TAG, "Connecting to " + deviceConnection.getIpAddress());
       return CommunicationBridge.connect(context, useCurrentThread, connectionHandler);
    }

    public static void scheduleDetective(final Context context) {

        ComponentName componentName = new ComponentName(context, DetectiveJobService.class);
        /*
         * See useful methods here
         * https://developer.android.com/reference/android/app/job/JobInfo.Builder.html
         */
        JobInfo jobInfo = new JobInfo.Builder(JOB_DETECTIVE_ID, componentName)
                .setMinimumLatency(10 * 1000) //10 sec
                .build();

        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(jobInfo);

        if (resultCode == JobScheduler.RESULT_SUCCESS)
            Log.d(TAG, "Detective scheduled with success");
        else
            Log.d(TAG, "Detective scheduling fail");

    }

    public static boolean isJobServiceOn(Context context, int jobId) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE) ;

        boolean hasBeenScheduled = false ;

        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == jobId) {
                hasBeenScheduled = true ;
                break ;
            }
        }

        return hasBeenScheduled ;
    }


    public static void processConnection(Context context, NetworkDevice device, DeviceConnection deviceConnection)
    {

        deviceConnection.setLastCheckedDate(System.currentTimeMillis());

        if (device == null){
            deviceConnection.setDeviceId("-");

            if (!isJobServiceOn(context, JOB_DETECTIVE_ID))
                scheduleDetective(context);
        }
        else
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

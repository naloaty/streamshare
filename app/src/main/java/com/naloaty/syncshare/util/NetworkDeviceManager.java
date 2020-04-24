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


    public static void manageDevice(Context context, NetworkDevice networkDevice) {
        manageDevice(context, networkDevice, false);
    }

    public static CommunicationBridge.Client manageDevice(final Context context, final NetworkDevice networkDevice, boolean useCurrentThread) {

        CommunicationBridge.Client.ConnectionHandler connectionHandler =
                new CommunicationBridge.Client.ConnectionHandler() {
                    @Override
                    public void onConnect(CommunicationBridge.Client client) {
                        try {
                            SSDevice device = client.handleDevice(networkDevice.getIpAddress());
                            client.setDevice(device);

                            if (device.getDeviceId() != null) {

                                SSDevice localDevice = AppUtils.getLocalDevice(context);

                                if (localDevice.getDeviceId().contentEquals(device.getDeviceId()))
                                    networkDevice.setLocalDevice(true);
                                else
                                {
                                    networkDevice.setDeviceId(device.getDeviceId());
                                    networkDevice.setDeviceName(device.getNickname());
                                    networkDevice.setLastCheckedDate(System.currentTimeMillis());
                                }


                                processConnection(context, networkDevice);
                            }
                        } catch (Exception e) {
                            Log.i(TAG, "Could not connect to device " + networkDevice.getIpAddress() + ": " + e.getMessage());
                            processConnection(context, networkDevice);
                        }
                    }
                };

       Log.i(TAG, "Connecting to " + networkDevice.getIpAddress());
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


    public static void processConnection(Context context, NetworkDevice networkDevice)
    {

        if (networkDevice.getDeviceId().equals("-"))
            if (!isJobServiceOn(context, JOB_DETECTIVE_ID))
                scheduleDetective(context);

        NetworkDeviceRepository repository = new NetworkDeviceRepository(context);
        NetworkDevice entry = repository.findConnection(networkDevice.getIpAddress(), networkDevice.getDeviceId(), networkDevice.getServiceName());

        if (entry != null)
            repository.delete(entry);

        repository.insert(networkDevice);

        Log.i(TAG, "Connection with ip " + networkDevice.getIpAddress() + " and service name "
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
            NetworkDevice connection = repository.findConnection(null, null, serviceName);

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

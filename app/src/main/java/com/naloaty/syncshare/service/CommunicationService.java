package com.naloaty.syncshare.service;


import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.naloaty.syncshare.app.SSService;
import com.naloaty.syncshare.database.device.NetworkDeviceRepository;
import com.naloaty.syncshare.util.AppUtils;
import com.naloaty.syncshare.util.CommunicationNotification;
import com.naloaty.syncshare.util.DNSSDHelper;

public class CommunicationService extends SSService {

    private static final String TAG = "CommunicationService";

    public static final String ACTION_STOP_SHARING = "com.naloaty.intent.action.STOP_SHARING";
    public static final String EXTRA_SERVICE_SATE = "serviceState";
    public static final String SERVICE_STATE_CHANGED = "com.naloaty.intent.state.SERVICE_STATE_CHANGED";

    private CommunicationNotification mNotification;
    private DNSSDHelper mDNSSDHelper;
    private MediaServer mMediaServer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Registering DNSSD");

        NetworkDeviceRepository repository = new NetworkDeviceRepository(this);
        repository.deleteAllConnections();

        mDNSSDHelper = AppUtils.getDNSSDHelper(getApplicationContext());
        mDNSSDHelper.register();
        mDNSSDHelper.startBrowse();

        mNotification = new CommunicationNotification(getNotificationUtils());

        if (!AppUtils.checkRunningConditions(this)){
            Log.i(TAG, "Aborting CommunicationService");
            stopSelf();
        }

        Log.d(TAG, "Starting MediaServer");
        mMediaServer = new MediaServer(this);

        try { mMediaServer.start(); }
        catch (Exception e) {
            Log.d(TAG, "Cannot start MediaServer: ");
            e.printStackTrace();
        }

        mNotification.getServiceNotification().build();

        Intent intent = new Intent(SERVICE_STATE_CHANGED);
        intent.putExtra(EXTRA_SERVICE_SATE, true);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.i(TAG, "Communication service started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null)
            Log.d(TAG, "onStartCommand() with action = " + intent.getAction());

        if (intent != null && intent.getAction() != null && AppUtils.checkRunningConditions(this)) {

            if (intent.getAction().contentEquals(ACTION_STOP_SHARING)) {
                Log.d(TAG, "User stopped service");
                stopSelf();
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "Unregistering DNSSD");
        mDNSSDHelper.unregister();
        mDNSSDHelper.stopBrowse();

        try
        {
            mMediaServer.stop();
        }
        catch (Exception e) {
            Log.d(TAG, "Cannot stop MediaServer: ");
            e.printStackTrace();
        }

        mNotification.cancelNotification();

        NetworkDeviceRepository repository = new NetworkDeviceRepository(this);
        repository.deleteAllConnections();

        Intent intent = new Intent(SERVICE_STATE_CHANGED);
        intent.putExtra(EXTRA_SERVICE_SATE, false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        Log.d(TAG, "Destroy :(");
    }



}

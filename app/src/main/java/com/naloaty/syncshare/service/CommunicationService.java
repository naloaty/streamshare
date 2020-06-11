package com.naloaty.syncshare.service;


import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.naloaty.syncshare.app.SSService;
import com.naloaty.syncshare.config.AppConfig;
import com.naloaty.syncshare.database.device.NetworkDeviceRepository;
import com.naloaty.syncshare.util.CommunicationNotification;
import com.naloaty.syncshare.util.DNSSDHelper;
import com.naloaty.syncshare.util.PermissionHelper;

/**
 * This class represents the main StreamShare service.
 * The main tasks of this class:
 *      - Holding a {@link MediaServer} in running state;
 *      - Providing nearby device discovery regardless of activity lifecycle. See {@link com.github.druk.dnssd.DNSSD}.
 */
public class CommunicationService extends SSService {

    private static final String TAG = "CommunicationService";

    public static final String ACTION_STOP_SHARING   = "com.naloaty.intent.action.STOP_SHARING";
    public static final String SERVICE_STATE_CHANGED = "com.naloaty.intent.state.SERVICE_STATE_CHANGED";
    public static final String EXTRA_SERVICE_SATE    = "serviceState";

    private State mServiceState = State.Stopped;

    private CommunicationNotification mNotification;
    private DNSSDHelper mDNSSDHelper;
    private MediaServer mMediaServer;

    private enum State {
        Running,
        Stopped
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Starting StreamShare service");

        if (!PermissionHelper.checkRequiredPermissions(this)) {
            Log.i(TAG, "No required running conditions met. Shutting down the service");

            stopSelf();
            return;
        }

        if (!runMediaServer()) {
            stopSelf();
            return;
        }

        setupDiscoveryService();

        mNotification = new CommunicationNotification(getNotificationUtils());
        mNotification.showServiceNotification();
        setServiceState(State.Running);

        Log.i(TAG, "StreamShare service is started successfully");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        String action = intent.getAction();

        if (ACTION_STOP_SHARING.equals(action)) {
            Log.i(TAG, "Shutting down the service by user");
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopMediaServer();
        stopDiscoveryService();
        clearNearbyDevices();

        mNotification.cancelNotification();
        setServiceState(State.Stopped);

        Log.d(TAG, "StreamShare service is stopped");
    }

    /**
     * Starts the {@link MediaServer}.
     * @return True if the media server is started successfully.
     */
    private boolean runMediaServer() {
        try {
            mMediaServer = new MediaServer(this, AppConfig.MEDIA_SERVER_PORT);
            mMediaServer.start();

            Log.i(TAG, "Media server is started");
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();

            Log.i(TAG, "Cannot start media server");
            return false;
        }
    }

    /**
     * Stops the {@link MediaServer}.
     */
    private void stopMediaServer() {
        mMediaServer.stop();
        Log.i(TAG, "Media server is stopped");
    }

    /**
     * Setups nearby discovery service.
     */
    private void setupDiscoveryService() {
        mDNSSDHelper = new DNSSDHelper(getApplicationContext());
        mDNSSDHelper.register();
        mDNSSDHelper.startBrowse();

        Log.i(TAG, "Discovery service is registered");
    }

    /**
     * Stops nearby discovery service.
     */
    private void stopDiscoveryService() {
        mDNSSDHelper.unregister();
        mDNSSDHelper.stopBrowse();

        Log.i(TAG, "Discovery service is unregistered");
    }

    /**
     * Clears a table of network devices in the database.
     * @see com.naloaty.syncshare.database.device.NetworkDevice
     */
    private void clearNearbyDevices() {
        NetworkDeviceRepository repository = new NetworkDeviceRepository(this);
        repository.deleteAllDevices();

        Log.i(TAG, "Nearby devices table is cleared");
    }

    /**
     * Toggles the service to the required state.
     * @see #broadcastServiceState()
     */
    private void setServiceState(State serviceState) {
        if (mServiceState == serviceState)
            return;

        mServiceState = serviceState;
        broadcastServiceState();
    }

    /**
     * Broadcasts the state of the service
     */
    private void broadcastServiceState() {
        Intent intent = new Intent(SERVICE_STATE_CHANGED);

        switch (mServiceState) {
            case Running:
                intent.putExtra(EXTRA_SERVICE_SATE, true);
                break;

            case Stopped:
                intent.putExtra(EXTRA_SERVICE_SATE, false);
                break;
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}

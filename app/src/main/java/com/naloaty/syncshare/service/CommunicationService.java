package com.naloaty.syncshare.service;


import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.genonbeta.CoolSocket.CoolSocket;
import com.naloaty.syncshare.app.SSService;
import com.naloaty.syncshare.config.AppConfig;
import com.naloaty.syncshare.config.Keyword;
import com.naloaty.syncshare.database.NetworkDeviceRepository;
import com.naloaty.syncshare.util.AppUtils;
import com.naloaty.syncshare.util.CommunicationNotification;
import com.naloaty.syncshare.util.DNSSDHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class CommunicationService extends SSService {

    private static final String TAG = "CommunicationService";
    public static final String
            ACTION_STOP_SHARING = "stopSharing",
            ACTION_STOP_DISCOVERING = "stopDiscovering",
            EXTRA_STATUS_RUNNING = "statusRunning";
            //PREFERNCE_SERVICE_RUNNING = "communicationServiceRunning";


    private CommunicationServer mCommunicationServer;
    private CommunicationNotification mNotification;
    //private NsdHelper mNsdHelper;
    private DNSSDHelper mDNSSDHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mCommunicationServer = new CommunicationServer(AppConfig.SERVER_PORT);
        //mNsdHelper = new NsdHelper(getApplicationContext());
        //mNsdHelper.registerService();
        mDNSSDHelper = AppUtils.getDNSSDHelper(getApplicationContext());
        mDNSSDHelper.register();

        mNotification = new CommunicationNotification(getNotificationUtils());

        if (!AppUtils.checkRunningConditions(this) || !mCommunicationServer.start()){
            Log.i(TAG, "Aborting CommunicationService");
            stopSelf();
        }

        /*startForeground(CommunicationNotification.SERVICE_NOTIFICATION_ID,
                mNotification.getServiceNotification().build());*/

        mNotification.getServiceNotification().build();

        /*AppUtils.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(PREFERNCE_SERVICE_RUNNING, true)
                .apply();*/

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
            else if (intent.getAction().contentEquals(ACTION_STOP_DISCOVERING)) {

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable()
                {
                    @Override
                    public void run() {
                        {
                            Log.d(TAG, "onStartCommand(): deleting connections");
                            NetworkDeviceRepository repository = new NetworkDeviceRepository(getApplicationContext());
                            repository.deleteAllConnections();
                        }
                    }
                }, 3000);
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "Stopping and unregistering NSD");
        mCommunicationServer.stop();
        //mNsdHelper.unregisterService();
        mDNSSDHelper.unregister();

        /*Log.d(TAG, "Deleting all connections");
        NetworkDeviceRepository repository = AppUtils.getDeviceConnectionRepository(getApplicationContext());
        repository.deleteAllDevices();*/

        /*AppUtils.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(PREFERNCE_SERVICE_RUNNING, false)
                .apply();*/

        mNotification.cancelNotification();
        Log.d(TAG, "Destroy :(");
    }

    public class CommunicationServer extends CoolSocket {

        public CommunicationServer(int port) {
            super(port);
        }

        @Override
        protected void onConnected(ActiveConnection activeConnection) {
            //Limit clients amount
            if (getConnectionCountByAddress(activeConnection.getAddress()) > 3)
                return;

            //Can produce JSONException, TimeoutException, IOException
            try {
                ActiveConnection.Response clientRequest = activeConnection.receive();
                JSONObject responseJSON = analyzeResponse(clientRequest);
                JSONObject replyJSON = new JSONObject();

                AppUtils.applyDeviceToJSON(CommunicationService.this, replyJSON);
                String remoteDeviceSerial = null;

                //Handshake exchange between devices
                if (responseJSON.has(Keyword.HANDSHAKE_REQUIRED) && responseJSON.getBoolean(Keyword.HANDSHAKE_REQUIRED)) {
                    pushReply(activeConnection, replyJSON, true);

                    /*
                     * "handshakeOnly" means that devices will only exchange information
                     * about each other and nothing else
                     */

                    if (!responseJSON.has(Keyword.HANDSHAKE_ONLY) || !responseJSON.getBoolean(Keyword.HANDSHAKE_ONLY)) {
                        if (responseJSON.has(Keyword.DEVICE_INFO_SERIAL))
                            remoteDeviceSerial = responseJSON.getString(Keyword.DEVICE_INFO_SERIAL);

                        clientRequest = activeConnection.receive();
                        responseJSON = analyzeResponse(clientRequest);
                    } else {
                        Log.i(TAG, "Disconnecting client due handshake only");
                        activeConnection.getSocket().close();
                        return;
                    }
                }

                //Client wants to connect
                if (remoteDeviceSerial != null) {
                    //TODO: NetworkDevice should be restored from database
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }

        public JSONObject analyzeResponse(ActiveConnection.Response response) throws JSONException
        {
            return response.totalLength > 0 ? new JSONObject(response.response) : new JSONObject();
        }

        public void pushReply(ActiveConnection activeConnection, JSONObject reply, boolean result) throws JSONException, TimeoutException, IOException
        {
            activeConnection.reply(reply
                    .put(Keyword.RESULT, result)
                    .toString());
        }
    }

}

package com.naloaty.syncshare.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;

import com.genonbeta.CoolSocket.CoolSocket;
import com.naloaty.syncshare.app.Service;
import com.naloaty.syncshare.config.AppConfig;
import com.naloaty.syncshare.config.Keyword;
import com.naloaty.syncshare.other.NetworkDevice;
import com.naloaty.syncshare.util.AppUtils;
import com.naloaty.syncshare.util.CommunicationBridge;
import com.naloaty.syncshare.util.NsdHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class CommunicationService extends Service {

    private static final String TAG = CommunicationService.class.getSimpleName();

    private CommunicationServer mCommunicationServer;
    private NsdHelper mNsdHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mCommunicationServer = new CommunicationServer(AppConfig.SERVER_PORT);
        mNsdHelper = new NsdHelper(getApplicationContext());
        mNsdHelper.registerService();

        if (!AppUtils.checkRunningConditions(this) || !mCommunicationServer.start()){
            Log.i(TAG, "Aborting CommunicationService");
            stopSelf();
        }


        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }

        Log.i(TAG, "Communication service started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "Communication service -> destroy");
        mCommunicationServer.stop();
        mNsdHelper.unregisterService();
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

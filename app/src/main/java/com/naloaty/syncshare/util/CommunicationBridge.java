package com.naloaty.syncshare.util;

import android.content.Context;
import android.util.Log;

import com.genonbeta.CoolSocket.CoolSocket;
import com.naloaty.syncshare.config.AppConfig;
import com.naloaty.syncshare.config.Keyword;
import com.naloaty.syncshare.database.NetworkDevice;
import com.naloaty.syncshare.database.SSDatabase;
import com.naloaty.syncshare.database.SSDevice;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeoutException;

public abstract class CommunicationBridge implements CoolSocket.Client.ConnectionHandler {

    public static Client connect(Context context, boolean useCurrentThread, final Client.ConnectionHandler handler)
    {
        final Client clientInstance = new Client(context);

        if (useCurrentThread)
            handler.onConnect(clientInstance);
        else
            new Thread()
            {
                @Override
                public void run()
                {
                    super.run();
                    handler.onConnect(clientInstance);
                }
            }.start();

        return clientInstance;
    }

    public static class Client extends CoolSocket.Client {

        private static final String TAG = Client.class.getSimpleName();
        /*
         * [CommunicationBridge.Client.ConnectionHandler]
         *   onConnect(CommunicationBridge.Client client);
         *     ^ |
         *     | |
         *     | V
         * [CommunicationBridge.Client]
         *  handleDevice(ip);
         *     ^ | ---------------> connectWithHandshake(ip, true);
         *     | |                           | --------------------> connect(ip);
         *     | |                           | <------------------- activeConnection
         *     | |                           V
         *     | | <-------------------- handShake(activeConnection, true) (requesting device info)
         *     | V
         *  handleDevice(activeConnection); (receiving response in json)
         *     ^ |
         *     | |            [NetworkDeviceManager]
         *     | ------------> loadFromJson(json);
         *     --------------- NetworkDevice
         */
        private SSDevice mDevice;
        private Context mContext;

        public Client(Context context) {
            this.mContext = context;
        }

        public Context getContext() {
            return mContext;
        }

        public SSDevice getDevice()
        {
            return mDevice;
        }

        public void setDevice(SSDevice device) {
            this.mDevice = device;
        }

        public SSDevice handleDevice(String ipAddress)
                throws CommunicationException, IOException, TimeoutException
        {
            return handleDevice(InetAddress.getByName(ipAddress));
        }

        public SSDevice handleDevice(InetAddress address)
                throws CommunicationException, IOException, TimeoutException
        {
            /*
             * handshakeOnly means that devices will only exchange information
             * about each other and disconnect
             */
            return handleDevice(connectWithHandshake(address, true), true);
        }

        public SSDevice handleDevice(CoolSocket.ActiveConnection activeConnection, boolean handshakeOnly)
                throws CommunicationException, IOException, TimeoutException
        {
            try {
                CoolSocket.ActiveConnection.Response response = activeConnection.receive();
                JSONObject responseJSON = new JSONObject(response.response);

                if (handshakeOnly){
                    Log.i(TAG, "Disconnecting from server due handshake only");
                    activeConnection.getSocket().close();
                }

                return NetworkDeviceManager.loadDeviceFromJson(responseJSON);

            } catch (JSONException e) {
                throw new CommunicationException("Cannot read the device from JSON");
            }
        }

        public CoolSocket.ActiveConnection connectWithHandshake(InetAddress address, boolean handshakeOnly)
                throws IOException, CommunicationException, TimeoutException {

            return handshake(connect(address), handshakeOnly);
        }

        public CoolSocket.ActiveConnection handshake(CoolSocket.ActiveConnection activeConnection, boolean handshakeOnly)
                throws IOException, TimeoutException, CommunicationException{
            try {
                activeConnection.reply(new JSONObject()
                        .put(Keyword.HANDSHAKE_REQUIRED, true)
                        .put(Keyword.HANDSHAKE_ONLY, handshakeOnly)
                        .put(Keyword.DEVICE_INFO_SERIAL, AppUtils.getDeviceId(mContext)).toString());
            } catch (JSONException e) {
                throw new CommunicationException("Failed to open connection between devices");
            }

            return activeConnection;
        }

        public CoolSocket.ActiveConnection connect(InetAddress address) throws IOException {
            if (!address.isReachable(1000))
                throw new IOException("Ping test before connection to the address has failed");

            /*
             * This class extends from CoolSocket.Client and uses it's method
             * CoolSocket.Client.connect(SocketAddress socketAddress, int operationTimeout) throws IOException
             */
            return super.connect(new InetSocketAddress(address, AppConfig.SERVER_PORT), AppConfig.SOCKET_TIMEOUT);
        }

        public interface ConnectionHandler
        {
            void onConnect(Client client);
        }
    }

    public static class CommunicationException extends Exception
    {
        public CommunicationException(String desc)
        {
            super(desc);
        }
    }
}

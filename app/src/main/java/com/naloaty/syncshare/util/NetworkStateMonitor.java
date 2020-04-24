package com.naloaty.syncshare.util;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONObject;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

/*
 * Based on https://stackoverflow.com/questions/36421930/connectivitymanager-connectivity-action-deprecated
 */

public class NetworkStateMonitor extends ConnectivityManager.NetworkCallback {

    private static final String TAG = "NetworkStateMonitor";

    public static final String
                    NETWORK_MONITOR_STATE_CHANGED = "com.naloaty.intent.NETWORK_MONITOR_STATE_CHANGED",
                    EXTRA_NETWORK_TYPE = "networkType",
                    JSON_WIFI_SSID = "wifiSSID",
                    JSON_WIFI_BSSID = "BSSID",
                    JSON_WIFI_IP_ADDRESS = "ipAddress";

    public static final int
                    NETWORK_TYPE_WIFI = 0,
                    NETWORK_TYPE_CELLULAR = 1,
                    NETWORK_TYPE_NOT_CONNECTED = 2;


    final NetworkRequest mNetworkRequest;
    final Context mContext;

    private enum NetworkType {
        WIFI,
        CELLULAR
    }

    public NetworkStateMonitor(Context context) {

        mContext = context;
        mNetworkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();
    }

    public void startMonitor() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(mNetworkRequest, this);

        Log.d(TAG, "Monitor started");
    }

    public void stopMonitor() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.unregisterNetworkCallback(this);

        Log.d(TAG, "Monitor stopped");
    }

    @Override
    public void onAvailable(Network network) {
        super.onAvailable(network);
        sendBroadcast();
    }

    @Override
    public void onLost(@NonNull Network network) {
        super.onLost(network);
        sendBroadcast();
    }

    private void sendBroadcast() {
        Intent intent = new Intent(new Intent(NETWORK_MONITOR_STATE_CHANGED));

        int networkType = getCurrentNetworkType();
        intent.putExtra(EXTRA_NETWORK_TYPE, networkType);

        Log.d(TAG, "Network type: " + networkType);

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public JSONObject getCurrentWifiInfo() {
        return getCurrentWfifInfo(mContext);
    }

    public static JSONObject getCurrentWfifInfo(Context context) {
        int networkType = getCurrentNetworkType(context);

        if (networkType == NETWORK_TYPE_WIFI) {
            WifiManager mainWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo currentWifi = mainWifi.getConnectionInfo();

            if (currentWifi == null)
                return null;

            JSONObject json = new JSONObject();

            try {
                json.put(JSON_WIFI_SSID, currentWifi.getSSID().replaceAll("\"", ""));
                json.put(JSON_WIFI_IP_ADDRESS, convertIpAddress(currentWifi.getIpAddress()));
                json.put(JSON_WIFI_BSSID, currentWifi.getBSSID());
                return json;
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }

        return null;
    }

    /*
     * Copied from https://stackoverflow.com/questions/16730711/get-my-wifi-ip-address-android
     */
    private static String convertIpAddress(int ipAddress) {

        // Convert little-endian to big-endian if needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e(TAG, "Unable to get host address.");
            ipAddressString = "<unknown ip>";
        }

        return ipAddressString;
    }

    /*
     * Based on https://stackoverflow.com/questions/59704596/how-to-use-connectivity-manager-networkcallback-to-get-network-type
     */
    public int getCurrentNetworkType() {
        return getCurrentNetworkType(mContext);
    }

    public static int getCurrentNetworkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return NETWORK_TYPE_WIFI;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return NETWORK_TYPE_CELLULAR;
                    }
                }
            }
        } else {
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        return NETWORK_TYPE_WIFI;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        return NETWORK_TYPE_CELLULAR;
                    }
                }
            }
        }

        return NETWORK_TYPE_NOT_CONNECTED;
    }

}

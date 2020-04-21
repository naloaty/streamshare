package com.naloaty.syncshare.util;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.naloaty.syncshare.activity.PairDeviceActivity;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

/*
 * Based on https://stackoverflow.com/questions/36421930/connectivitymanager-connectivity-action-deprecated
 */

public class ConnectionStateMonitor extends ConnectivityManager.NetworkCallback {

    private static final String TAG = "ConnectionStateMonitor";

    public static final String
                    NETWORK_MONITOR_STATE_CHANGED = "com.naloaty.intent.NETWORK_MONITOR_STATE_CHANGED",
                    EXTRA_WIFI_CONNECTED = "wifiConnected",
                    EXTRA_NETWORK_NAME = "networkName",
                    EXTRA_IP_ADDRESS = "ipAddress";

    final NetworkRequest mNetworkRequest;
    final Context mContext;

    public ConnectionStateMonitor(Context context) {

        mContext = context;
        mNetworkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
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

        Log.d(TAG, "Sending broadcast: wifi enabled");
        WifiManager mainWifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo currentWifi = mainWifi.getConnectionInfo();

        String ipAddress = convertIpAddress(currentWifi.getIpAddress());

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(NETWORK_MONITOR_STATE_CHANGED)
                .putExtra(EXTRA_WIFI_CONNECTED, true)
                .putExtra(EXTRA_NETWORK_NAME, currentWifi.getSSID())
                .putExtra(EXTRA_IP_ADDRESS, ipAddress)
        );
    }

    /*
     * Copied from https://stackoverflow.com/questions/16730711/get-my-wifi-ip-address-android
     */

    private String convertIpAddress(int ipAddress) {

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

    @Override
    public void onUnavailable() {
        super.onUnavailable();

        Log.d(TAG, "Sending broadcast: wifi unavailable");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(NETWORK_MONITOR_STATE_CHANGED)
                .putExtra(EXTRA_WIFI_CONNECTED, false)
        );
    }

    @Override
    public void onLost(@NonNull Network network) {
        super.onLost(network);

        Log.d(TAG, "Sending broadcast: wifi lost");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(NETWORK_MONITOR_STATE_CHANGED)
                .putExtra(EXTRA_WIFI_CONNECTED, false)
        );
    }
}

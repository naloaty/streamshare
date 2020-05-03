package com.naloaty.syncshare.communication;

import android.content.Context;
import android.util.Log;

import com.naloaty.syncshare.database.device.NetworkDevice;
import com.naloaty.syncshare.security.SSTrustManager;
import com.naloaty.syncshare.security.SecurityManager;
import com.naloaty.syncshare.security.SecurityUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class SSOkHttpClient {

    private static final String TAG = "SSOkHttpClient";

    public static OkHttpClient getOkHttpClient(final Context context) {
        X509TrustManager trustManager = new SSTrustManager(new SecurityManager(context));
        SSLContext sslContext = SecurityUtils.getSSlContext(context.getFilesDir(), trustManager);

        HostnameVerifier verifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                /*if (networkDevice.getIpAddress().equals(hostname)){
                    Log.d(TAG, "Connection accepted for " + hostname);
                    return true;
                }
                else
                {
                    Log.d(TAG, "Connection declined for " + hostname);
                    return true;
                }*/

                return true;
            }
        };

        return new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                .hostnameVerifier(verifier)
                .build();
    }
}

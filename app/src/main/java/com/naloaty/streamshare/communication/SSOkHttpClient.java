package com.naloaty.streamshare.communication;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.naloaty.streamshare.security.SSTrustManager;
import com.naloaty.streamshare.security.SecurityManager;
import com.naloaty.streamshare.security.SecurityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * This class helps build an instance of OkHttpClient that uses the StreamShare SSL certificate.
 * @see com.naloaty.streamshare.activity.VideoPlayerActivity
 * @see com.naloaty.streamshare.app.SSOkHttpGlideModule
 * @see CommunicationHelper
 */
public class SSOkHttpClient {

    private static final String TAG = "SSOkHttpClient";

    /**
     * Builds an instance of OkHttpClient that uses the StreamShare SSL certificate.
     * @param context The Context in which an instance of OkHttpClient will be built.
     * @return Instance of OkHttpClient that uses the StreamShare SSL certificate.
     */
    public static OkHttpClient getOkHttpClient(@NonNull final Context context) {
        X509TrustManager trustManager = new SSTrustManager(new SecurityManager(context));
        SSLContext sslContext = SecurityUtils.getSSlContext(context.getFilesDir(), trustManager);

        if (sslContext == null) {
            Log.e(TAG, "Cannot create secure okHttpClient: sslContext is null");
            return null;
        }

        return new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                .hostnameVerifier((hostname, session) -> true)
                .build();
    }
}

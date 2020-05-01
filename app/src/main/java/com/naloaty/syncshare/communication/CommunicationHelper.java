package com.naloaty.syncshare.communication;

import android.content.Context;
import android.util.Log;

import com.naloaty.syncshare.config.AppConfig;
import com.naloaty.syncshare.database.NetworkDevice;
import com.naloaty.syncshare.database.SSDevice;
import com.naloaty.syncshare.security.SSTrustManager;
import com.naloaty.syncshare.security.SecurityManager;
import com.naloaty.syncshare.security.SecurityUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CommunicationHelper {

    private static final String TAG = "CommunicationHelper";
    private static final String PROTOCOL = "https://";

    public static Call<SSDevice> requestDeviceInformation(final Context context, final NetworkDevice networkDevice) {

        if (networkDevice == null) {
            Log.w(TAG, "Attempt to request device information of non-existent network device");
            return null;
        }

        String requestAddress = PROTOCOL + networkDevice.getIpAddress() + ":" + AppConfig.MEDIA_SERVER_PORT + "/";

        Retrofit retrofit = buildRetrofit(context, networkDevice, requestAddress);
        DeviceRequest request = retrofit.create(DeviceRequest.class);
        return request.getDeviceInformation();
    }

    public static Call<SimpleServerResponse> sendDeviceInformation(final Context context, final NetworkDevice networkDevice, final SSDevice ssDevice) {

        if (networkDevice == null) {
            Log.w(TAG, "Attempt to send device information to non-existent network device");
            return null;
        }

        String requestAddress = PROTOCOL + networkDevice.getIpAddress() + ":" + AppConfig.MEDIA_SERVER_PORT + "/";

        Retrofit retrofit = buildRetrofit(context, networkDevice, requestAddress);
        DeviceRequest request = retrofit.create(DeviceRequest.class);
        return request.sendDeviceInformation(ssDevice);
    }

    private static Retrofit buildRetrofit(final Context context, final NetworkDevice networkDevice, final String requestAddress) {
        X509TrustManager trustManager = new SSTrustManager(new SecurityManager(context));
        SSLContext sslContext = SecurityUtils.getSSlContext(context.getFilesDir(), trustManager);

        HostnameVerifier verifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                if (networkDevice.getIpAddress().equals(hostname)){
                    Log.d(TAG, "Connection accepted for " + hostname);
                    return true;
                }
                else
                {
                    Log.d(TAG, "Connection declined for " + hostname);
                    return true;
                }
            }
        };

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                .hostnameVerifier(verifier)
                .build();



        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(requestAddress)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }
}

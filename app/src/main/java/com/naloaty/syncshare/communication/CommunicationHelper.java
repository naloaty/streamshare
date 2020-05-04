package com.naloaty.syncshare.communication;

import android.content.Context;
import android.util.Log;

import com.naloaty.syncshare.config.AppConfig;
import com.naloaty.syncshare.config.MediaServerKeyword;
import com.naloaty.syncshare.database.device.NetworkDevice;
import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.database.media.Album;
import com.naloaty.syncshare.media.Media;
import com.naloaty.syncshare.util.NetworkStateMonitor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CommunicationHelper {

    private static final String TAG = "RemoteViewHelper";
    private static final String PROTOCOL = "https://";
    private static final String PROTOCOL_INSECURE = "http://";

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

    public static Call<List<Album>> requestAlbumsList (final Context context, final NetworkDevice networkDevice) {
        if (networkDevice == null) {
            Log.w(TAG, "Attempt to request albums list of non-existent network device");
            return null;
        }

        String requestAddress = PROTOCOL + networkDevice.getIpAddress() + ":" + AppConfig.MEDIA_SERVER_PORT + "/";

        Retrofit retrofit = buildRetrofit(context, networkDevice, requestAddress);
        MediaRequest request = retrofit.create(MediaRequest.class);
        return request.getAlbumsList();
    }

    public static Call<List<Media>> requestMediaList (final Context context, final NetworkDevice networkDevice, final Album album) {
        if (networkDevice == null) {
            Log.w(TAG, "Attempt to request media list of non-existent network device");
            return null;
        }

        String requestAddress = PROTOCOL + networkDevice.getIpAddress() + ":" + AppConfig.MEDIA_SERVER_PORT + "/";

        Retrofit retrofit = buildRetrofit(context, networkDevice, requestAddress);
        MediaRequest request = retrofit.create(MediaRequest.class);
        Log.d(TAG, "Querying media list with albumId -> " + String.valueOf(album.getAlbumId()));
        return request.getMediaList(String.valueOf(album.getAlbumId()));
    }

    private static Retrofit buildRetrofit(final Context context, final NetworkDevice networkDevice, final String requestAddress) {

        OkHttpClient client = SSOkHttpClient.getOkHttpClient(context);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(requestAddress)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }

    public static String getThumbnailRequestURL(NetworkDevice networkDevice) {
        return PROTOCOL
                + networkDevice.getIpAddress()
                + ":"
                + AppConfig.MEDIA_SERVER_PORT
                + "/"
                + MediaServerKeyword.REQUEST_TARGET_MEDIA
                + "/"
                + MediaServerKeyword.REQUEST_THUMBNAIL
                + "/";
    }

    public static String getFullsizeImageRequestURL(NetworkDevice networkDevice) {
        return PROTOCOL
                + networkDevice.getIpAddress()
                + ":"
                + AppConfig.MEDIA_SERVER_PORT
                + "/"
                + MediaServerKeyword.REQUEST_TARGET_MEDIA
                + "/"
                + MediaServerKeyword.REQUEST_FULLSIZE_IMAGE
                + "/";
    }

    public static String getServeRequestURL(NetworkDevice networkDevice) {
        return PROTOCOL_INSECURE
                + networkDevice.getIpAddress()
                + ":"
                + AppConfig.MEDIA_INSECURE_SERVER_PORT
                + "/"
                + MediaServerKeyword.REQUEST_TARGET_MEDIA
                + "/"
                + MediaServerKeyword.REQUEST_SERVE_FILE
                + "/";
    }

}

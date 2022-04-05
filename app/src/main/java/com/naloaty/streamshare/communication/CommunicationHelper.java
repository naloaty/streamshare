package com.naloaty.streamshare.communication;

import android.content.Context;

import androidx.annotation.NonNull;

import com.naloaty.streamshare.config.AppConfig;
import com.naloaty.streamshare.service.Requests;
import com.naloaty.streamshare.database.entity.NetworkDevice;
import com.naloaty.streamshare.database.entity.SSDevice;
import com.naloaty.streamshare.database.entity.Album;
import com.naloaty.streamshare.media.Media;

import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * This class helps send requests to another device.
 * @see com.naloaty.streamshare.activity.ImageViewActivity
 * @see com.naloaty.streamshare.fragment.RemoteAlbumsFragment
 * @see com.naloaty.streamshare.fragment.RemoteMediaFragment
 * @see com.naloaty.streamshare.util.AddDeviceHelper
 * @see com.naloaty.streamshare.adapter.RemoteAlbumsAdapter
 * @see com.naloaty.streamshare.adapter.RemoteMediaAdapter
 */
public class CommunicationHelper {

    private static final String PROTOCOL = "https://";

    /**
     * Requests general information about a remote device.
     * @param context The Context in which this request should be executed.
     * @param networkDevice Network information about remote device. See {@link com.naloaty.streamshare.util.DNSSDHelper}.
     * @return Retrofit network call.
     */
    public static Call<SSDevice> requestDeviceInformation(@NonNull final Context context, @NonNull final NetworkDevice networkDevice) {
        String requestAddress = PROTOCOL + networkDevice.getIpAddress() + ":" + AppConfig.MEDIA_SERVER_PORT + "/";
        Retrofit retrofit = buildRetrofit(context, networkDevice, requestAddress);
        DeviceRequest request = retrofit.create(DeviceRequest.class);

        return request.getDeviceInformation();
    }

    /**
     * Sends general information about a local device to a remote device.
     * @param context The Context in which this request should be executed.
     * @param networkDevice Network information about remote device. See {@link com.naloaty.streamshare.util.DNSSDHelper}.
     * @param ssDevice Information about local device. See {@link com.naloaty.streamshare.util.AppUtils#getLocalDevice(Context)}.
     * @return Retrofit network call.
     */
    public static Call<SimpleServerResponse> sendDeviceInformation(@NonNull final Context context, @NonNull final NetworkDevice networkDevice, @NonNull final SSDevice ssDevice) {
        String requestAddress = PROTOCOL + networkDevice.getIpAddress() + ":" + AppConfig.MEDIA_SERVER_PORT + "/";
        Retrofit retrofit = buildRetrofit(context, networkDevice, requestAddress);
        DeviceRequest request = retrofit.create(DeviceRequest.class);

        return request.sendDeviceInformation(ssDevice);
    }

    /**
     * Requests a list of shared albums on a remote device.
     * @param context The Context in which this request should be executed.
     * @param networkDevice Network information about remote device. See {@link com.naloaty.streamshare.util.DNSSDHelper}.
     * @return Retrofit network call.
     */
    public static Call<List<Album>> requestAlbumsList (@NonNull final Context context, @NonNull final NetworkDevice networkDevice) {
        String requestAddress = PROTOCOL + networkDevice.getIpAddress() + ":" + AppConfig.MEDIA_SERVER_PORT + "/";
        Retrofit retrofit = buildRetrofit(context, networkDevice, requestAddress);
        MediaRequest request = retrofit.create(MediaRequest.class);

        return request.getAlbumsList();
    }

    /**
     * Requests a list of media-files in a specific album on a remote device.
     * @param context The Context in which this request should be executed.
     * @param networkDevice Network information about remote device. See {@link com.naloaty.streamshare.util.DNSSDHelper}.
     * @param album The album whose file list you want to receive.
     * @return Retrofit network call.
     */
    public static Call<List<Media>> requestMediaList (@NonNull final Context context, @NonNull final NetworkDevice networkDevice, @NonNull final Album album) {
        String requestAddress = PROTOCOL + networkDevice.getIpAddress() + ":" + AppConfig.MEDIA_SERVER_PORT + "/";
        Retrofit retrofit = buildRetrofit(context, networkDevice, requestAddress);
        MediaRequest request = retrofit.create(MediaRequest.class);

        return request.getMediaList(String.valueOf(album.getAlbumId()));
    }

    /**
     * Builds Retrofit instance with OkHttpClient that uses StreamShare SSL certificate.
     * @param context The Context in which this request should be executed.
     * @param networkDevice Network information about remote device. See {@link com.naloaty.streamshare.util.DNSSDHelper}.
     * @param requestAddress Address to which the request will be sent.
     * @return Retrofit instance.
     */
    private static Retrofit buildRetrofit(@NonNull final Context context, @NonNull final NetworkDevice networkDevice, @NonNull final String requestAddress) {
        OkHttpClient client = SSOkHttpClient.getOkHttpClient(context);

        return new Retrofit.Builder()
                .baseUrl(requestAddress)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Builds a request URL to retrieve a small thumbnail of media-file.
     * @param networkDevice Network information about remote device. See {@link com.naloaty.streamshare.util.DNSSDHelper}.
     * @return Request URL.
     */
    public static String getThumbnailRequestURL(@NonNull NetworkDevice networkDevice) {
        return PROTOCOL
                + networkDevice.getIpAddress()
                + ":"
                + AppConfig.MEDIA_SERVER_PORT
                + "/"
                + Requests.MEDIA
                + "/"
                + Requests.THUMBNAIL
                + "/";
    }

    /**
     * Builds a request URL to retrieve a full-size thumbnail of media-file.
     * @param networkDevice Network information about remote device. See {@link com.naloaty.streamshare.util.DNSSDHelper}.
     * @return Request URL.
     */
    public static String getFullsizeImageRequestURL(@NonNull NetworkDevice networkDevice) {
        return PROTOCOL
                + networkDevice.getIpAddress()
                + ":"
                + AppConfig.MEDIA_SERVER_PORT
                + "/"
                + Requests.MEDIA
                + "/"
                + Requests.FULL_SIZE_IMAGE
                + "/";
    }

    /**
     * Builds a request URL to retrieve a media-file itself.
     * @param networkDevice Network information about remote device. See {@link com.naloaty.streamshare.util.DNSSDHelper}.
     * @return Request URL.
     */
    public static String getServeRequestURL(@NonNull NetworkDevice networkDevice) {
        return PROTOCOL
                + networkDevice.getIpAddress()
                + ":"
                + AppConfig.MEDIA_SERVER_PORT
                + "/"
                + Requests.MEDIA
                + "/"
                + Requests.SERVE_FILE
                + "/";
    }

}

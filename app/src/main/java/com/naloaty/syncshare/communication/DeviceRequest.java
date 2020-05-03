package com.naloaty.syncshare.communication;

import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.config.MediaServerKeyword;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface DeviceRequest {

    /*
     * device/information
     */
    @GET(MediaServerKeyword.REQUEST_TARGET_DEVICE + "/" + MediaServerKeyword.REQUEST_INFORMATION)
    Call<SSDevice> getDeviceInformation();

    /*
     * device/information
     */
    @POST(MediaServerKeyword.REQUEST_TARGET_DEVICE + "/" + MediaServerKeyword.REQUEST_INFORMATION)
    Call<SimpleServerResponse> sendDeviceInformation(@Body SSDevice ssDevice);
}

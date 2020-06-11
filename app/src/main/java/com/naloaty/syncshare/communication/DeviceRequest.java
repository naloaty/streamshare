package com.naloaty.syncshare.communication;

import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.service.Requests;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * This class is used to send and receive device information.
 * @see CommunicationHelper
 */
public interface DeviceRequest {
    /*
     * device/information
     */
    @GET(Requests.DEVICE + "/" + Requests.INFORMATION)
    Call<SSDevice> getDeviceInformation();

    /*
     * device/information
     */
    @POST(Requests.DEVICE + "/" + Requests.INFORMATION)
    Call<SimpleServerResponse> sendDeviceInformation(@Body SSDevice ssDevice);

}

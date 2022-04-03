package com.naloaty.streamshare.communication;

/**
 * This class represents a simple response from a media server, such as "Device added"
 * @see com.naloaty.streamshare.service.MediaServer
 * @see com.naloaty.streamshare.util.AddDeviceHelper
 */
public class SimpleServerResponse {

    //Response message
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

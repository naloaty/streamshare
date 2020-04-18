package com.naloaty.syncshare.other;

import androidx.room.PrimaryKey;

public class NetworkDevice {


    public String brand;

    public String model;

    public String nickname;

    public String deviceId;

    public long lastUsageTime;

    //TODO: add app version info
    //TODO: this should be stored in database (when we add device)

    public NetworkDevice(String deviceId) {
        this.deviceId = deviceId;
    }
}

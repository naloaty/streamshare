package com.naloaty.syncshare.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ss_devices_table")
public class SSDevice {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String brand;

    private String model;

    private String nickname;

    private String deviceId;

    private long lastUsageTime;

    private String appVersion;

    public void SSDevice(String deviceId, String appVersion) {
        this.deviceId = deviceId;
        this.appVersion = appVersion;
    }

    //id
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }


    //deviceId
    public String getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    //brand
    public String getBrand() {
        return brand;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }

    //model
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }

    //nickname
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    //lastUsageTime
    public long getLastUsageTime() {
        return lastUsageTime;
    }
    public void setLastUsageTime(long lastUsageTime) {
        this.lastUsageTime = lastUsageTime;
    }

    //appVersion
    public String getAppVersion() {
        return appVersion;
    }
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
}

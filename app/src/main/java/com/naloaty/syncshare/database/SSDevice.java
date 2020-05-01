package com.naloaty.syncshare.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ss_devices_table")
public class SSDevice {

    public static final int PLATFORM_MOBILE = 0;
    public static final int PLATFORM_DESKTOP = 1;
    public static final int PLATFORM_UNKNOWN = 2;

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String brand;

    private String model;

    private String nickname;

    private String deviceId;

    private long lastUsageTime;

    private String appVersion;

    private boolean trusted;

    private boolean accessAllowed;

    public SSDevice(String deviceId, String appVersion) {
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

    //trusted
    public boolean isTrusted() {
        return trusted;
    }
    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

    //accessAllowed
    public boolean isAccessAllowed() {
        return accessAllowed;
    }
    public void setAccessAllowed(boolean accessAllowed) {
        this.accessAllowed = accessAllowed;
    }
}

package com.naloaty.streamshare.database.device;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * This class represents a table of trusted devices in a StreamShare database.
 * It contains general information about devices marked by the user as trusted.
 * To understand how it works, you need to get acquainted with the Room library.
 * @see com.naloaty.streamshare.util.AddDeviceHelper
 */
@Entity(tableName = "ss_devices_table")
public class SSDevice {

    public static final int PLATFORM_MOBILE = 0;
    public static final int PLATFORM_DESKTOP = 1;
    public static final int PLATFORM_UNKNOWN = 2;

    /**
     * Database row id.
     */
    @PrimaryKey(autoGenerate = true)
    private int id;

    /**
     * Brand of the trusted device.
     */
    private String brand;

    /**
     * Model of the trusted device.
     */
    private String model;

    /**
     * Nickname of the trusted device.
     */
    private String nickname;

    /**
     * StreamShare ID of the trusted device.
     */
    private String deviceId;

    /**
     * The time when the discovered device was last online.
     */
    private long lastUsageTime;

    /**
     * StreamShare version on the discovered device.
     * @see com.naloaty.streamshare.config.AppConfig
     */
    private String appVersion;

    /**
     * True if user authenticated device.
     */
    private boolean trusted;

    /**
     * True if the user has granted access to their media.
     */
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

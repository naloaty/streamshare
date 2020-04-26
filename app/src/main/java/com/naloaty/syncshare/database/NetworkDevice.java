package com.naloaty.syncshare.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "network_devices_table")
public class NetworkDevice {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String ipAddress;

    private String deviceId;

    private String deviceName;

    private Long lastCheckedDate;

    private String serviceName;

    private String appVersion;


    public NetworkDevice(String ipAddress, String serviceName) {
        this.ipAddress = ipAddress;
        this.serviceName = serviceName;
    }

    //ipAddress
    public String getIpAddress() {
        return ipAddress;
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

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    //deviceName
    public String getDeviceName() {
        return deviceName;
    }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    //lastCheckedDate
    public Long getLastCheckedDate() {
        return lastCheckedDate;
    }
    public void setLastCheckedDate(Long lastCheckedDate) {
        this.lastCheckedDate = lastCheckedDate;
    }

    //serviceName
    public String getServiceName() {
        return serviceName;
    }
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    //appVersion
    public String getAppVersion() {
        return appVersion;
    }
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
}

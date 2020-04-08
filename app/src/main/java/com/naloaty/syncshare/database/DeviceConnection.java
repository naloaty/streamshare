package com.naloaty.syncshare.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "device_connection_table")
public class DeviceConnection {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String ipAddress;

    private String deviceId;

    private Long lastCheckedDate;

    private String serviceName;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public DeviceConnection(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setLastCheckedDate(Long lastCheckedDate) {
        this.lastCheckedDate = lastCheckedDate;
    }

    public int getId() {
        return id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Long getLastCheckedDate() {
        return lastCheckedDate;
    }

    public String getDeviceId() {
        return deviceId;
    }
}

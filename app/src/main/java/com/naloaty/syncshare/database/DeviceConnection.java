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

    private boolean isLocalDevice;


    public DeviceConnection(String ipAddress, String serviceName) {
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

    //isLocalDevice
    public boolean isLocalDevice() {
        return isLocalDevice;
    }
    public void setLocalDevice(boolean localDevice) {
        isLocalDevice = localDevice;
    }

}

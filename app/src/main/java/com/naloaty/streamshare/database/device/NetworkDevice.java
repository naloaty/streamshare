package com.naloaty.streamshare.database.device;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * This class represents a table of network devices in a StreamShare database.
 * It contains network information about current devices on the network.
 * To understand how it works, you need to get acquainted with the Room library.
 * @see com.naloaty.streamshare.util.DNSSDHelper
 */
@Entity(tableName = "network_devices_table")
public class NetworkDevice implements Serializable {

    /**
     * Database row id
     */
    @PrimaryKey(autoGenerate = true)
    private int id;

    /**
     * Current ip address of the discovered device.
     */
    private String ipAddress;

    /**
     * StreamShare ID of the discovered device.
     */
    private String deviceId;

    /**
     * Name of the discovered device.
     */
    private String deviceName;

    /**
     * The time when the discovered device was last online.
     */
    private Long lastCheckedDate;

    /**
     * Service name of the discovered device.
     */
    private String serviceName;

    /**
     * StreamShare version on the discovered device.
     * @see com.naloaty.streamshare.config.AppConfig
     */
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

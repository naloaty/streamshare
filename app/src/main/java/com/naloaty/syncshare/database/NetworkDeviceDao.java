package com.naloaty.syncshare.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NetworkDeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NetworkDevice networkDevice);

    @Delete
    void delete(NetworkDevice networkDevice);

    @Query("SELECT * FROM network_devices_table")
    LiveData<List<NetworkDevice>> getAllDevices();

    @Query("SELECT * FROM network_devices_table")
    List<NetworkDevice> getAllDevicesList();

    @Query("DELETE FROM network_devices_table")
    void deleteAllDevices();

    @Query("SELECT COUNT(*) FROM network_devices_table")
    Integer getDeviceCount();

    @Query("SELECT * FROM network_devices_table WHERE ipAddress=:ipAddress OR deviceId=:deviceId OR serviceName=:serviceName")
    NetworkDevice findDevice(String ipAddress, String deviceId, String serviceName);

    @Query("SELECT * FROM network_devices_table WHERE deviceId='-'")
    List<NetworkDevice> getUnknown();
}

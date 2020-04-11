package com.naloaty.syncshare.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DeviceConnectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DeviceConnection deviceConnection);

    @Delete
    void delete(DeviceConnection deviceConnection);

    @Query("SELECT * FROM device_connection_table")
    LiveData<List<DeviceConnection>> getAllConnections();

    @Query("DELETE FROM device_connection_table")
    void deleteAllConnections();

    @Query("SELECT * FROM device_connection_table WHERE ipAddress=:ipAddress OR deviceId=:deviceId OR serviceName=:serviceName")
    DeviceConnection findConnection(String ipAddress, String deviceId, String serviceName);
}

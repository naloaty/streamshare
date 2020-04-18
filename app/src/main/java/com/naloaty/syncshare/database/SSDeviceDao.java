package com.naloaty.syncshare.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SSDeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SSDevice device);

    @Delete
    void delete(SSDevice device);

    @Query("SELECT * FROM ss_devices_table")
    LiveData<List<SSDevice>> getAllDevices();

    @Query("SELECT * FROM ss_devices_table WHERE deviceId=:deviceId")
    SSDevice findDevice(String deviceId);
}

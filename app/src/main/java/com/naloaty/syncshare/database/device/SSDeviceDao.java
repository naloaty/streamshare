package com.naloaty.syncshare.database.device;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface SSDeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SSDevice device);

    @Delete
    void delete(SSDevice device);

    @Update
    void update(SSDevice device);

    @Query("SELECT COUNT(*) FROM ss_devices_table")
    Integer getDeviceCount();

    @Query("SELECT * FROM ss_devices_table")
    LiveData<List<SSDevice>> getAllDevices();

    @Deprecated
    @Query("SELECT * FROM ss_devices_table WHERE deviceId=:deviceId")
    SSDevice findDeviceDep(String deviceId);

    @Query("SELECT * FROM ss_devices_table WHERE deviceId=:deviceId")
    Single<SSDevice> findDevice(String deviceId);

}

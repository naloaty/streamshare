package com.naloaty.syncshare.database.device;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

@Dao
public interface NetworkDeviceDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(NetworkDevice networkDevice);

    @Update
    Completable update(NetworkDevice networkDevice);

    @Delete
    Completable delete(NetworkDevice networkDevice);

    @Query("SELECT * FROM network_devices_table")
    LiveData<List<NetworkDevice>> getAllDevices();

    @Query("SELECT * FROM network_devices_table")
    List<NetworkDevice> getAllDevicesList();

    @Query("DELETE FROM network_devices_table")
    void deleteAllDevices();

    @Query("SELECT COUNT(*) FROM network_devices_table")
    Integer getDeviceCount();

    @Deprecated
    @Query("SELECT * FROM network_devices_table WHERE ipAddress=:ipAddress OR deviceId=:deviceId OR serviceName=:serviceName")
    NetworkDevice findDeviceDep(String ipAddress, String deviceId, String serviceName);

    @Query("SELECT * FROM network_devices_table WHERE ipAddress=:ipAddress OR deviceId=:deviceId OR serviceName=:serviceName")
    Single<NetworkDevice> findDevice(String ipAddress, String deviceId, String serviceName);
}

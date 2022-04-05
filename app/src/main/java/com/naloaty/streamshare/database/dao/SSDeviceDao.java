package com.naloaty.streamshare.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.naloaty.streamshare.database.entity.SSDevice;

import java.util.List;

import io.reactivex.Single;

/**
 * This class represents Data Access Object of trusted devices table in a StreamShare database.
 * It allows you to retrieve information from the database.
 * To understand how it works, you need to get acquainted with the Room library and Android Architecture Components.
 * @see SSDevice
 */
@Dao
public interface SSDeviceDao {

    /**
     * Inserts general information about device into the database.
     * @param device General information about device. Instance of {@link SSDevice}.
     * TODO: replace with ReactiveX object
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SSDevice device);

    /**
     * Updated general information about device in the database.
     * @param device General information about device. Instance of {@link SSDevice}.
     * TODO: replace with ReactiveX object
     */
    @Update
    void update(SSDevice device);

    /**
     * Deletes general information about device from the database.
     * @param device General information about device. Instance of {@link SSDevice}.
     * TODO: replace with ReactiveX object
     */
    @Delete
    void delete(SSDevice device);

    /**
     * Returns the number of records in the database.
     * @return Number of records.
     */
    @Query("SELECT COUNT(*) FROM ss_devices_table")
    Integer getDeviceCount();

    /**
     * Returns all trusted devices from database.
     * @return A list containing all trusted devices.
     */
    @Query("SELECT * FROM ss_devices_table")
    LiveData<List<SSDevice>> getAllDevices();

    /**
     * Searches for the required trusted device in the database.
     * @param deviceId Device StreamShare identifier.
     * @return Returns general information about the device, if found, as instance of {@link SSDevice}
     */
    @Deprecated
    @Query("SELECT * FROM ss_devices_table WHERE deviceId=:deviceId")
    SSDevice findDeviceDep(String deviceId);

    /**
     * Searches for the required trusted device in the database.
     * @param deviceId Device StreamShare identifier.
     * @return Returns general information about the device, if found, as ReactiveX single object.
     */
    @Query("SELECT * FROM ss_devices_table WHERE deviceId=:deviceId")
    Single<SSDevice> findDevice(String deviceId);

}

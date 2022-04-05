package com.naloaty.streamshare.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.naloaty.streamshare.database.entity.NetworkDevice;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * This class represents Data Access Object of network devices table in a StreamShare database.
 * It allows you to retrieve information from the database.
 * To understand how it works, you need to get acquainted with the Room library and Android Architecture Components.
 * @see NetworkDevice
 */
@Dao
public interface NetworkDeviceDao {

    /**
     * Inserts network information about device into the database.
     * @param networkDevice Network information about device. Instance of {@link NetworkDevice}
     * @return ReactiveX completable object.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(NetworkDevice networkDevice);

    /**
     * Updates network information about device in the database.
     * @param networkDevice Network information about device. Instance of {@link NetworkDevice}
     * @return ReactiveX completable object.
     */
    @Update
    Completable update(NetworkDevice networkDevice);

    /**
     * Deletes network information about device from the database.
     * @param networkDevice Network information about device. Instance of {@link NetworkDevice}
     * @return ReactiveX completable object.
     */
    @Delete
    Completable delete(NetworkDevice networkDevice);

    /**
     * Returns all discovered network devices from database.
     * @return A list containing all discovered network devices and wrapped into LiveData object.
     */
    @Query("SELECT * FROM network_devices_table")
    LiveData<List<NetworkDevice>> getAllDevices();

    /**
     * Returns all discovered network devices from database.
     * @return A list containing all discovered network devices.
     */
    @Query("SELECT * FROM network_devices_table")
    List<NetworkDevice> getAllDevicesList();

    /**
     * Deletes all records from database.
     */
    @Query("DELETE FROM network_devices_table")
    void deleteAllDevices();

    /**
     * Returns the number of records in the database.
     * @return Number of records.
     */
    @Query("SELECT COUNT(*) FROM network_devices_table")
    Integer getDeviceCount();

    /**
     * Searches for the required network device in the database. You can specify only one of three parameters.
     * @param ipAddress Device ip address.
     * @param deviceId Device StreamShare identifier.
     * @param serviceName Device service name. See {@link com.naloaty.streamshare.util.DNSSDHelper}.
     * @return Returns network information about the device, if found, as instance of {@link NetworkDevice}
     */
    @Deprecated
    @Query("SELECT * FROM network_devices_table WHERE ipAddress=:ipAddress OR deviceId=:deviceId OR serviceName=:serviceName")
    NetworkDevice findDeviceDep(String ipAddress, String deviceId, String serviceName);

    /**
     * Searches for the required network device in the database. You can specify only one of three parameters.
     * @param ipAddress Device ip address.
     * @param deviceId Device StreamShare identifier.
     * @param serviceName Device service name. See {@link com.naloaty.streamshare.util.DNSSDHelper}.
     * @return Returns network information about the device, if found, as ReactiveX single object.
     */
    @Query("SELECT * FROM network_devices_table WHERE ipAddress=:ipAddress OR deviceId=:deviceId OR serviceName=:serviceName")
    Single<NetworkDevice> findDevice(String ipAddress, String deviceId, String serviceName);
}

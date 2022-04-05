package com.naloaty.streamshare.database.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.naloaty.streamshare.database.entity.SSDevice;
import com.naloaty.streamshare.database.dao.SSDeviceDao;
import com.naloaty.streamshare.database.repository.SSDeviceRepository;

import java.util.List;

import io.reactivex.Single;

/**
 * This class represents extra abstract layer above {@link SSDeviceRepository}.
 * It connects activity with repository and allows you to retrieve information from the database.
 * To understand how it works, you need to get acquainted with Android Architecture Components.
 * @see SSDevice
 * @see SSDeviceDao
 * @see SSDeviceRepository
 */
public class SSDeviceViewModel extends AndroidViewModel {

    private SSDeviceRepository repository;
    private LiveData<List<SSDevice>> allDevices;
    private Context mContext;

    public SSDeviceViewModel(@NonNull Application application) {
        super(application);

        repository = new SSDeviceRepository(application);
        allDevices = repository.getAllDevices();
        mContext = application;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * Inserts general information about device into the database.
     * @param device General information about device. Instance of {@link SSDevice}.
     */
    public void insert(SSDevice device) {
        repository.insert(device);
    }

    /**
     * Decides whether to insert or update general information about device.
     * @param device General information about device. Instance of {@link SSDevice}.
     */
    public void delete(SSDevice device) {
        repository.delete(device);
    }

    /**
     * Searches for the required trusted device in the database.
     * @param deviceId Device StreamShare identifier.
     * @return Returns general information about the device, if found, as instance of {@link SSDevice}
     */
    @Deprecated
    public SSDevice findDeviceDep(String deviceId) {
        return repository.findDeviceDep(deviceId);
    }

    /**
     * Searches for the required trusted device in the database.
     * @param deviceId Device StreamShare identifier.
     * @return Returns general information about the device, if found, as ReactiveX single object.
     */
    public Single<SSDevice> findDevice(String deviceId) {
        return repository.findDevice(deviceId);
    }

    /**
     * Returns the number of records in the database.
     * @return Number of records.
     */
    public Integer getDeviceCount() {
        return repository.getDeviceCount();
    }

    /**
     * Returns all trusted devices from database.
     * @return A list containing all trusted devices and wrapped into LiveData object.
     */
    public LiveData<List<SSDevice>> getAllDevices () {
        return allDevices;
    }

}


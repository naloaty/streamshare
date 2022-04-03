package com.naloaty.streamshare.database.device;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import io.reactivex.Single;

/**
 * This class represents extra abstract layer above {@link NetworkDeviceRepository}.
 * It connects activity with repository and allows you to retrieve information from the database.
 * To understand how it works, you need to get acquainted with Android Architecture Components.
 * @see NetworkDevice
 * @see NetworkDeviceDao
 * @see NetworkDeviceRepository
 */
public class NetworkDeviceViewModel extends AndroidViewModel {

    private NetworkDeviceRepository repository;
    private LiveData<List<NetworkDevice>> allDevices;
    private Context mContext;

    public NetworkDeviceViewModel(@NonNull Application application) {
        super(application);

        repository = new NetworkDeviceRepository(application);
        allDevices = repository.getAllDevices();
        mContext = application;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * Returns the number of network devices in the database.
     * @return Number of records.
     */
    public Integer getDeviceCount() {
        return repository.getDeviceCount();
    }

    /**
     * Returns all discovered network devices from database.
     * @return A list containing all discovered network devices.
     */
    public List<NetworkDevice> getAllDevicesList(){
        return repository.getAllDevicesList();
    }

    /**
     * Returns of discovered network devices from database.
     * @return A list containing all discovered network devices and wrapped into LiveData object.
     */
    public LiveData<List<NetworkDevice>> getAllDevices() {
        return allDevices;
    }

    /**
     * Searches for the required network device in the database. You can specify only one of three parameters.
     * @param ipAddress Device ip address.
     * @param deviceId Device StreamShare identifier.
     * @param serviceName Device service name. See {@link com.naloaty.streamshare.util.DNSSDHelper}.
     * @return Returns network information about the device, if found, as ReactiveX single object.
     */
    public Single<NetworkDevice> findDevice(String ipAddress, String deviceId, String serviceName) {
        return repository.findDevice(ipAddress, deviceId, serviceName);
    }
}

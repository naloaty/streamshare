package com.naloaty.syncshare.database.device;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

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

    public Integer getDeviceCount() {
        return repository.getDeviceCount();
    }

    public List<NetworkDevice> getAllDevicesList(){
        return repository.getAllDevicesList();
    }

    public LiveData<List<NetworkDevice>> getAllDevices() {
        return allDevices;
    }

    public Single<NetworkDevice> findDevice(String ipAddress, String deviceId, String serviceName) {
        return repository.findDevice(ipAddress, deviceId, serviceName);
    }
}

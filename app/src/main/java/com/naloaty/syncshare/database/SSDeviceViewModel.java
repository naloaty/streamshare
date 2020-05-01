package com.naloaty.syncshare.database;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

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

    public void insert(SSDevice device) {
        repository.insert(device);
    }

    public void delete(SSDevice device) {
        repository.delete(device);
    }

    public SSDevice findDevice(String deviceId) {
        return repository.findDevice(deviceId);
    }

    public Integer getDeviceCount() {
        return repository.getDeviceCount();
    }

    public LiveData<List<SSDevice>> getAllDevices () {
        return allDevices;
    }
}


package com.naloaty.syncshare.database;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class DeviceConnectionViewModel extends AndroidViewModel {

    private DeviceConnectionRepository repository;
    private LiveData<List<DeviceConnection>> allConnections;
    private Context mContext;

    public DeviceConnectionViewModel(@NonNull Application application) {
        super(application);
        repository = new DeviceConnectionRepository(application);
        allConnections = repository.getAllConnections();
        mContext = application;
    }

    public Context getContext() {
        return mContext;
    }

    public void insert(DeviceConnection connection) {
        repository.insert(connection);
    }

    public void delete(DeviceConnection connection) {
        repository.delete(connection);
    }

    public LiveData<List<DeviceConnection>> getAllConnections () {
        return allConnections;
    }
}

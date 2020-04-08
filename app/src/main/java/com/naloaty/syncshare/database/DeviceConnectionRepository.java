package com.naloaty.syncshare.database;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class DeviceConnectionRepository {
    private DeviceConnectionDao deviceConnectionDao;
    private LiveData<List<DeviceConnection>> allConnections;

    public DeviceConnectionRepository(Context context) {
        SSDatabase database = SSDatabase.getInstance(context);

        //Room auto generates these abstract methods
        deviceConnectionDao = database.deviceConnectionDao();
        allConnections = deviceConnectionDao.getAllConnections();
    }

    public void insert(DeviceConnection connection) {
        new InsertConnectionAsyncTask(deviceConnectionDao).execute(connection);
    }

    public void delete(DeviceConnection connection) {
        new DeleteConnectionAsyncTask(deviceConnectionDao).execute(connection);
    }

    public DeviceConnection getConnectionByService(String serviceName) throws ExecutionException, InterruptedException {
        return new FindConnectionAsyncTask(deviceConnectionDao).execute(serviceName).get();
    }

    public LiveData<List<DeviceConnection>> getAllConnections() {
        return allConnections;
    }

    public static class InsertConnectionAsyncTask extends AsyncTask<DeviceConnection, Void, Void> {
        private DeviceConnectionDao deviceConnectionDao;

        private InsertConnectionAsyncTask(DeviceConnectionDao deviceConnectionDao) {
            this.deviceConnectionDao = deviceConnectionDao;
        }

        @Override
        protected Void doInBackground(DeviceConnection... deviceConnections) {
            deviceConnectionDao.insert(deviceConnections[0]);
            return null;
        }
    }

    public static class DeleteConnectionAsyncTask extends AsyncTask<DeviceConnection, Void, Void> {
        private DeviceConnectionDao deviceConnectionDao;

        private DeleteConnectionAsyncTask(DeviceConnectionDao deviceConnectionDao) {
            this.deviceConnectionDao = deviceConnectionDao;
        }

        @Override
        protected Void doInBackground(DeviceConnection... deviceConnections) {
            deviceConnectionDao.delete(deviceConnections[0]);
            return null;
        }
    }

    public static class FindConnectionAsyncTask extends AsyncTask<String, Void, DeviceConnection> {
        private DeviceConnectionDao deviceConnectionDao;

        private FindConnectionAsyncTask(DeviceConnectionDao deviceConnectionDao) {
            this.deviceConnectionDao = deviceConnectionDao;
        }

        @Override
        protected DeviceConnection doInBackground(String... strings) {
            return deviceConnectionDao.getConnectionByService(strings[0]);
        }
    }
}

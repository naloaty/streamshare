package com.naloaty.syncshare.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class DeviceConnectionRepository {
    private static final String TAG = "DeviceConnectionRepo";

    private DeviceConnectionDao deviceConnectionDao;
    private LiveData<List<DeviceConnection>> allConnections;

    public DeviceConnectionRepository(Context context) {
        SSDatabase database = SSDatabase.getInstance(context);

        //Room auto generates these abstract methods
        deviceConnectionDao = database.deviceConnectionDao();
        allConnections = deviceConnectionDao.getAllConnections();
    }

    public void insert(DeviceConnection connection) {
        new InsertConnectionAT(deviceConnectionDao).execute(connection);
    }

    public void delete(DeviceConnection connection) {
        new DeleteConnectionAT(deviceConnectionDao).execute(connection);
    }

    public LiveData<List<DeviceConnection>> getAllConnections() {
        return allConnections;
    }

    public void deleteAllConnections() {
        new DeleteAllConnectionsAT(deviceConnectionDao).execute();
    }

    public DeviceConnection findConnection(String ipAddress, String deviceId, String serviceName) {
        try {
            return new FindConnectionAT(deviceConnectionDao).execute(ipAddress, deviceId, serviceName).get();
        }
        catch (Exception e) {
            Log.d(TAG, "findConnection() exception: " + e.getMessage());
            return null;
        }
    }

    public List<DeviceConnection> getUnknown() {
        try {
            return new GetUnknownAT(deviceConnectionDao).execute().get();
        }
        catch (Exception e) {
            Log.d(TAG, "getUnknown() exception: " + e.getMessage());
            return null;
        }

    }

    public static class GetUnknownAT extends AsyncTask<Void, Void, List<DeviceConnection>> {

        private DeviceConnectionDao deviceConnectionDao;

        private GetUnknownAT(DeviceConnectionDao deviceConnectionDao) {
            this.deviceConnectionDao = deviceConnectionDao;
        }

        @Override
        protected List<DeviceConnection> doInBackground(Void... voids) {
            return deviceConnectionDao.getUnknown();
        }
    }

    public static class InsertConnectionAT extends AsyncTask<DeviceConnection, Void, Void> {
        private DeviceConnectionDao deviceConnectionDao;

        private InsertConnectionAT(DeviceConnectionDao deviceConnectionDao) {
            this.deviceConnectionDao = deviceConnectionDao;
        }

        @Override
        protected Void doInBackground(DeviceConnection... deviceConnections) {
            deviceConnectionDao.insert(deviceConnections[0]);
            return null;
        }
    }

    public static class DeleteConnectionAT extends AsyncTask<DeviceConnection, Void, Void> {
        private DeviceConnectionDao deviceConnectionDao;

        private DeleteConnectionAT(DeviceConnectionDao deviceConnectionDao) {
            this.deviceConnectionDao = deviceConnectionDao;
        }

        @Override
        protected Void doInBackground(DeviceConnection... deviceConnections) {
            deviceConnectionDao.delete(deviceConnections[0]);
            return null;
        }
    }

    public static class DeleteAllConnectionsAT extends AsyncTask<Void, Void, Void> {
        private DeviceConnectionDao deviceConnectionDao;

        private DeleteAllConnectionsAT(DeviceConnectionDao deviceConnectionDao) {
            this.deviceConnectionDao = deviceConnectionDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            deviceConnectionDao.deleteAllConnections();
            return null;
        }
    }

    public static class FindConnectionAT extends AsyncTask<String, Void, DeviceConnection> {
        private DeviceConnectionDao deviceConnectionDao;

        public FindConnectionAT(DeviceConnectionDao deviceConnectionDao){
            this.deviceConnectionDao = deviceConnectionDao;
        }

        @Override
        protected DeviceConnection doInBackground(String... strings) {
            return deviceConnectionDao.findConnection(strings[0], strings[1], strings[2]);
        }
    }
}

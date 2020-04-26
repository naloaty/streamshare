package com.naloaty.syncshare.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

public class NetworkDeviceRepository {
    private static final String TAG = "NetworkDeviceRepo";

    private NetworkDeviceDao networkDeviceDao;
    private LiveData<List<NetworkDevice>> allDevices;

    public NetworkDeviceRepository(Context context) {
        SSDatabase database = SSDatabase.getInstance(context);

        //Room auto generates these abstract methods
        networkDeviceDao = database.NetworkDeviceDao();
        allDevices = networkDeviceDao.getAllDevices();
    }

    public void insert(NetworkDevice connection) {
        new InsertDeviceAT(networkDeviceDao).execute(connection);
    }

    public void delete(NetworkDevice connection) {
        new DeleteDeviceAT(networkDeviceDao).execute(connection);
    }

    public LiveData<List<NetworkDevice>> getAllDevices() {
        return allDevices;
    }

    public void deleteAllConnections() {
        new DeleteAllDevicesAT(networkDeviceDao).execute();
    }

    public NetworkDevice findDevice(String ipAddress, String deviceId, String serviceName) {
        try {
            return new FindDeviceAT(networkDeviceDao).execute(ipAddress, deviceId, serviceName).get();
        }
        catch (Exception e) {
            Log.d(TAG, "findDevice() exception: " + e.getMessage());
            return null;
        }
    }

    public List<NetworkDevice> getUnknown() {
        try {
            return new GetUnknownAT(networkDeviceDao).execute().get();
        }
        catch (Exception e) {
            Log.d(TAG, "getUnknown() exception: " + e.getMessage());
            return null;
        }

    }

    public static class GetUnknownAT extends AsyncTask<Void, Void, List<NetworkDevice>> {

        private NetworkDeviceDao networkDeviceDao;

        private GetUnknownAT(NetworkDeviceDao networkDeviceDao) {
            this.networkDeviceDao = networkDeviceDao;
        }

        @Override
        protected List<NetworkDevice> doInBackground(Void... voids) {
            return networkDeviceDao.getUnknown();
        }


    }

    public static class InsertDeviceAT extends AsyncTask<NetworkDevice, Void, Void> {
        private NetworkDeviceDao networkDeviceDao;

        private InsertDeviceAT(NetworkDeviceDao networkDeviceDao) {
            this.networkDeviceDao = networkDeviceDao;
        }

        @Override
        protected Void doInBackground(NetworkDevice... networkDevices) {
            networkDeviceDao.insert(networkDevices[0]);
            return null;
        }
    }

    public static class DeleteDeviceAT extends AsyncTask<NetworkDevice, Void, Void> {
        private NetworkDeviceDao networkDeviceDao;

        private DeleteDeviceAT(NetworkDeviceDao networkDeviceDao) {
            this.networkDeviceDao = networkDeviceDao;
        }

        @Override
        protected Void doInBackground(NetworkDevice... networkDevices) {
            networkDeviceDao.delete(networkDevices[0]);
            return null;
        }
    }

    public static class DeleteAllDevicesAT extends AsyncTask<Void, Void, Void> {
        private NetworkDeviceDao networkDeviceDao;

        private DeleteAllDevicesAT(NetworkDeviceDao networkDeviceDao) {
            this.networkDeviceDao = networkDeviceDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            networkDeviceDao.deleteAllDevices();
            return null;
        }
    }

    public static class FindDeviceAT extends AsyncTask<String, Void, NetworkDevice> {
        private NetworkDeviceDao networkDeviceDao;

        public FindDeviceAT(NetworkDeviceDao networkDeviceDao){
            this.networkDeviceDao = networkDeviceDao;
        }

        @Override
        protected NetworkDevice doInBackground(String... strings) {
            return networkDeviceDao.findDevice(strings[0], strings[1], strings[2]);
        }
    }
}

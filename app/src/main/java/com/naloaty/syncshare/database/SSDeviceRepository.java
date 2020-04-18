package com.naloaty.syncshare.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

public class SSDeviceRepository {

    private static final String TAG = "SSDeviceRepo";

    private SSDeviceDao ssDeviceDao;
    private LiveData<List<SSDevice>> allDevices;

    public SSDeviceRepository(Context context) {
        SSDatabase database = SSDatabase.getInstance(context);

        //Room auto generates these abstract methods
        ssDeviceDao = database.ssDeviceDao();
        allDevices = ssDeviceDao.getAllDevices();
    }

    public void insert(SSDevice device) {
        new InsertDeviceAT(ssDeviceDao).execute(device);
    }

    public void delete(SSDevice device) {
        new DeleteDeviceAT(ssDeviceDao).execute(device);
    }

    public LiveData<List<SSDevice>> getAllDevices () {
        return allDevices;
    }

    public SSDevice findDevice(String deviceId) {
        try{
            return new FindDeviceAT(ssDeviceDao).execute(deviceId).get();
        }
        catch (Exception e) {
            Log.d(TAG, "findDevice() exception: " + e.getMessage());
            return null;
        }
    }

    public static class InsertDeviceAT extends AsyncTask<SSDevice, Void, Void> {

        private SSDeviceDao ssDeviceDao;

        public InsertDeviceAT(SSDeviceDao ssDeviceDao) {
            this.ssDeviceDao = ssDeviceDao;
        }

        @Override
        protected Void doInBackground(SSDevice... ssDevices) {
            ssDeviceDao.insert(ssDevices[0]);
            return null;
        }
    }

    public static class DeleteDeviceAT extends AsyncTask<SSDevice, Void, Void> {

        private SSDeviceDao ssDeviceDao;

        public DeleteDeviceAT (SSDeviceDao ssDeviceDao) {
            this.ssDeviceDao = ssDeviceDao;
        }

        @Override
        protected Void doInBackground(SSDevice... ssDevices) {
            ssDeviceDao.delete(ssDevices[0]);
            return null;
        }
    }

    public static class FindDeviceAT extends AsyncTask<String, Void, SSDevice> {

        private SSDeviceDao ssDeviceDao;

        public FindDeviceAT(SSDeviceDao ssDeviceDao) {
            this.ssDeviceDao = ssDeviceDao;
        }

        @Override
        protected SSDevice doInBackground(String... strings) {
            return ssDeviceDao.findDevice(strings[0]);
        }
    }
}

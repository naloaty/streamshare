package com.naloaty.syncshare.database.device;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.naloaty.syncshare.database.SSDatabase;

import java.util.List;

import io.reactivex.Single;

/**
 * This class represents extra abstract layer above {@link SSDeviceDao} of {@link SSDevice} in a StreamShare database.
 * It allows you to retrieve information from the database.
 * @see SSDevice
 * @see SSDeviceDao
 */
public class SSDeviceRepository {

    private static final String TAG = "SSDeviceRepo";

    /*
     * TODO: AsyncTask get() method DOES NOT asynchronous (replace by ReactiveX objects)
     */

    private SSDeviceDao ssDeviceDao;
    private LiveData<List<SSDevice>> allDevices;

    public SSDeviceRepository(Context context) {
        SSDatabase database = SSDatabase.getInstance(context);

        //Room auto generates these abstract methods
        ssDeviceDao = database.ssDeviceDao();
        allDevices = ssDeviceDao.getAllDevices();
    }

    /**
     * Inserts general information about device into the database.
     * @param device General information about device. Instance of {@link SSDevice}.
     */
    public void insert(SSDevice device) {
        new InsertDeviceAT(ssDeviceDao).execute(device);
    }

    /**
     * Updates general information about device in the database.
     * @param device General information about device. Instance of {@link SSDevice}.
     */
    public void update(SSDevice device) {
        new UpdateDeviceAT(ssDeviceDao).execute(device);
    }

    /**
     * Decides whether to insert or update general information about device.
     * @param device General information about device. Instance of {@link SSDevice}.
     */
    public void publish(SSDevice device) {
        SSDevice foundedDevice = findDeviceDep(device.getDeviceId());

        if (foundedDevice != null){
            device.setId(foundedDevice.getId());
            device.setTrusted(foundedDevice.isTrusted());
            device.setAccessAllowed(foundedDevice.isAccessAllowed());
            update(device);
        }
        else
        {
            insert(device);
        }
    }

    /**
     * Deletes general information about device from the database.
     * @param device General information about device. Instance of {@link SSDevice}
     */
    public void delete(SSDevice device) {
        new DeleteDeviceAT(ssDeviceDao).execute(device);
    }

    /**
     * Returns all trusted devices from database.
     * @return A list containing all trusted devices and wrapped into LiveData object.
     */
    public LiveData<List<SSDevice>> getAllDevices () {
        return allDevices;
    }

    /**
     * Returns the number of records in the database.
     * @return Number of records.
     */
    public int getDeviceCount() {
        try {
            return new GetDeviceCountAT(ssDeviceDao).execute().get();
        }
        catch (Exception e) {
            Log.d(TAG, "getDeviceCount() exception: " + e.getMessage());
            return 0;
        }

    }

    /**
     * Searches for the required trusted device in the database.
     * @param deviceId Device StreamShare identifier.
     * @return Returns general information about the device, if found, as instance of {@link SSDevice}
     */
    @Deprecated
    public SSDevice findDeviceDep(String deviceId) {
        try{
            return new FindDeviceAT(ssDeviceDao).execute(deviceId).get();
        }
        catch (Exception e) {
            Log.d(TAG, "findDeviceDep() exception: " + e.getMessage());
            return null;
        }
    }

    /**
     * Searches for the required trusted device in the database.
     * @param deviceId Device StreamShare identifier.
     * @return Returns general information about the device, if found, as ReactiveX single object.
     */
    public Single<SSDevice> findDevice(String deviceId) {
        return ssDeviceDao.findDevice(deviceId);
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

    public static class UpdateDeviceAT extends AsyncTask<SSDevice, Void, Void> {

        private SSDeviceDao ssDeviceDao;

        public UpdateDeviceAT(SSDeviceDao ssDeviceDao) {
            this.ssDeviceDao = ssDeviceDao;
        }

        @Override
        protected Void doInBackground(SSDevice... ssDevices) {
            ssDeviceDao.update(ssDevices[0]);
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
            return ssDeviceDao.findDeviceDep(strings[0]);
        }
    }

    public static class GetDeviceCountAT extends AsyncTask<Void, Void, Integer> {

        private SSDeviceDao ssDeviceDao;

        public GetDeviceCountAT(SSDeviceDao ssDeviceDao) {
            this.ssDeviceDao = ssDeviceDao;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return ssDeviceDao.getDeviceCount();
        }
    }
}

package com.naloaty.syncshare.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.naloaty.syncshare.database.NetworkDevice;
import com.naloaty.syncshare.database.NetworkDeviceRepository;
import com.naloaty.syncshare.util.NetworkDeviceManager;

public class DetectiveJobService extends JobService {

    private static final String TAG = "DetectiveJobService";
    private boolean jobCanceled = false;

    /*
     * This job periodically looking for next types of devices
     * 1)Device marked as Unknown (could connect with handshake)
     *
     * So this Detective Job solves that situation
     */

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Detective is hunting now");

        NetworkDeviceRepository repository = new NetworkDeviceRepository(getApplicationContext());

        for (NetworkDevice networkDevice : repository.getUnknown()) {
            NetworkDeviceManager.manageDevice(this, networkDevice);
        }

        Log.d(TAG, "Detective is resting now");
        jobFinished(params, false);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Detective is canceled");
        //true - we want to reschedule job if it was interrupted by system
        //!not necessary for periodic job
        jobCanceled = true;
        return false;
    }
}

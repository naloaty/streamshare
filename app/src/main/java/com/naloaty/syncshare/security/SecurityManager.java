package com.naloaty.syncshare.security;

import android.content.Context;
import android.util.Log;

import com.naloaty.syncshare.database.SSDevice;
import com.naloaty.syncshare.database.SSDeviceRepository;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SecurityManager {

    private static final String TAG = "SecurityManager";

    private final SSDeviceRepository mDeviceRepo;
    private final Context mContext;

    public SecurityManager(final Context context) {
        mDeviceRepo = new SSDeviceRepository(context);
        mContext = context;
    }

    public void checkCertificate(X509Certificate certificate) throws CertificateException {
        String deviceID = SecurityUtils.calculateDeviceId(certificate);
        SSDevice foundedDevice = mDeviceRepo.findDevice(deviceID);

        Log.w(TAG, "Remote device id: " + deviceID);
        //Log.w(TAG, "Remote device: " + certificate.toString());

        if (foundedDevice != null && foundedDevice.isTrusted()){
            Log.i(TAG, "Trust approved for " + deviceID);
            return;
        }

        Log.i(TAG, "Not trusted " + deviceID);
        throw new CertificateException("Untrusted device!");
    }
}

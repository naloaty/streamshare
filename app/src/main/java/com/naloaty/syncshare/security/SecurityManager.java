package com.naloaty.syncshare.security;

import android.content.Context;
import android.util.Log;

import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.database.device.SSDeviceRepository;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SecurityManager {

    private static final String TAG = "SecurityManager";

    public static final String UNTRUSTED_DEVICE = "noTrust";
    public static final String ACCESS_DENIED = "accessDenied";

    private final SSDeviceRepository mDeviceRepo;

    public SecurityManager(final Context context) {
        mDeviceRepo = new SSDeviceRepository(context);
    }

    void checkCertificate(X509Certificate certificate) throws CertificateException {
        String deviceID = SecurityUtils.calculateDeviceId(certificate);
        SSDevice foundedDevice = mDeviceRepo.findDeviceDep(deviceID);

        Log.i(TAG, String.format("New connection with DevId: %s, isTrusted: %s, isAccessAllowed: %s",
                deviceID, foundedDevice.isTrusted(), foundedDevice.isAccessAllowed()));

        if (foundedDevice == null)
            throw new CertificateException(UNTRUSTED_DEVICE);

        if (!foundedDevice.isTrusted())
            throw new CertificateException(UNTRUSTED_DEVICE);

        if (!foundedDevice.isAccessAllowed())
            throw new CertificateException(ACCESS_DENIED);


        Log.i(TAG, "Trust and access approved for " + deviceID);
    }
}

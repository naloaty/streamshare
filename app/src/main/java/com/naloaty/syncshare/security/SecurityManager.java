package com.naloaty.syncshare.security;

import android.content.Context;
import android.util.Log;

import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.database.device.SSDeviceRepository;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * This objective of this class is to authenticate devices.
 * @see SSTrustManager
 */
public class SecurityManager {

    private static final String TAG = "SecurityManager";

    private static final String UNTRUSTED_DEVICE = "noTrust";
    private static final String ACCESS_DENIED = "accessDenied";

    private final SSDeviceRepository mDeviceRepo;

    public SecurityManager(final Context context) {
        mDeviceRepo = new SSDeviceRepository(context);
    }

    /**
     * Checks if the incoming connection is from a trusted device.
     * @param certificate X509 v3 certificate to be verified.
     * @throws CertificateException Drops the connection if the certificate is not trusted.
     */
    void checkCertificate(X509Certificate certificate) throws CertificateException {
        String deviceID = SecurityUtils.calculateDeviceId(certificate);
        SSDevice foundedDevice = mDeviceRepo.findDeviceDep(deviceID);

        Log.i(TAG, String.format("New connection with DevId: %s, isTrusted: %s, isAccessAllowed: %s",
                deviceID, foundedDevice.isTrusted(), foundedDevice.isAccessAllowed()));

        if (!foundedDevice.isTrusted())
            throw new CertificateException(UNTRUSTED_DEVICE);

        if (!foundedDevice.isAccessAllowed())
            throw new CertificateException(ACCESS_DENIED);


        Log.i(TAG, "Trust and access approved for " + deviceID);
    }
}

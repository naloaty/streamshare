package com.naloaty.syncshare.security;

import android.util.Log;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Custom X509TrustManager that uses {@link SecurityManager} to verify certificates.
 */
public class SSTrustManager implements X509TrustManager {

    private static final String TAG = "SSTrustManager";

    private final SecurityManager mSecurityManager;

    public SSTrustManager(final SecurityManager securityManager) {
        mSecurityManager = securityManager;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        Log.d(TAG, "Checking client...");

        for (X509Certificate cert : certs) {
            mSecurityManager.checkCertificate(cert);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {

        Log.d(TAG, "Checking server...");

        for (X509Certificate cert : certs) {
            mSecurityManager.checkCertificate(cert);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        Log.d(TAG, "getAcceptedIssuers");

        return new X509Certificate[0];
    }
}

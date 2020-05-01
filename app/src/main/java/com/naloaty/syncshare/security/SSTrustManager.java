package com.naloaty.syncshare.security;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

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

package com.naloaty.syncshare.security;

import android.util.Log;

import com.naloaty.syncshare.config.KeyConfig;

import org.apache.commons.codec.binary.Base32;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SecurityUtils {

    private static final String TAG = "SecurityUtils";

    private static boolean isProviderInitialized = false;


    static {
        initBCProvider();
    }

    public static void initBCProvider() {
        if (!isProviderInitialized){
            Security.removeProvider("BC");
            Security.insertProviderAt(new BouncyCastleProvider(), 0);
            isProviderInitialized = true;
        }
    }

    public static SSLContext getSSLContext(SecurityManager securityManager, File pemDirectory) {
        return getSSlContext(pemDirectory, new SSTrustManager(securityManager));
    }

    public static SSLContext getSSlContext(File pemDirectory, X509TrustManager trustManager) {
        try
        {
            String tempPassword = "GYg746JD83SJ93782S4";
            KeyStore keyStore = getPKCS12KeyStore(pemDirectory, tempPassword);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, tempPassword.toCharArray());

            KeyManager[] keyManagers = kmf.getKeyManagers();
            SSLContext sslContext = SSLContext.getInstance("TLS");

            sslContext.init(keyManagers, new TrustManager[]{trustManager}, null);

            return sslContext;
        }
        catch (Exception e)
        {
            Log.w(TAG, "Cannot create SSL context: " + e.getMessage());
            return null;
        }
    }

    /*
     * Based on https://stackoverflow.com/questions/9711173/convert-ssl-pem-to-p12-with-or-without-openssl
     */
    public static KeyStore getPKCS12KeyStore(File pemDirectory, String tempPassword)
            throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
    {

        PrivateKey key = loadPrivateKey(KeyConfig.CRYPTO_PROVIDER, new File(pemDirectory, KeyConfig.HTTPS_KEY_FILENAME));
        X509Certificate cert = loadCertificate(KeyConfig.CRYPTO_PROVIDER, new File(pemDirectory, KeyConfig.HTTPS_CERT_FILENAME));

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null);

        ks.setKeyEntry("alias", key, tempPassword.toCharArray(), new Certificate[]{ cert });

        return ks;
    }

    private static X509Certificate loadCertificate(String provider, Object pemObject) {
        try
        {
            if (pemObject instanceof X509CertificateHolder)
                return new JcaX509CertificateConverter().setProvider(provider).getCertificate((X509CertificateHolder)pemObject);
            else
                return null;
        }
        catch (CertificateException e)
        {
            Log.w(TAG, "Cannot load certificate: " + e.getMessage());
            return null;
        }


    }

    public static X509Certificate loadCertificate(String provider, File certFile) {

        try
        {
            FileInputStream is = new FileInputStream(certFile);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate ca = (X509Certificate) cf.generateCertificate(is);
            //return loadCertificate(provider, loadPEMFile(certFile));

            return ca;
        }
        catch (IOException | CertificateException e)
        {
            Log.w(TAG, "Cannot load certificate file: " + e.getMessage());
            return null;
        }
    }

    private static PrivateKey loadPrivateKey(String provider, File keyFile) {
        try
        {
            Object stuff = loadPEMFile(keyFile);

            if (stuff instanceof PEMKeyPair) {
                PEMKeyPair keyPair = (PEMKeyPair)stuff;

                JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(provider);
                PEMKeyPair pemKeyPair = (PEMKeyPair) stuff;

                KeyPair key = converter.getKeyPair(pemKeyPair);
                return key.getPrivate();
            }

            else
                return null;
        }
        catch (IOException e)
        {
            Log.w(TAG, "Cannot load private key: " + e.getMessage());
            return null;
        }
    }

    private static Object loadPEMFile(File pemFile) throws IOException
    {
        FileReader fileReader = new FileReader(pemFile);
        PEMParser pemReader = new PEMParser(fileReader);
        Object stuff = pemReader.readObject();
        pemReader.close();
        fileReader.close();

        return stuff;
    }
    public static String calculateDeviceId(X509Certificate certificate) {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(certificate.getEncoded());

            Base32 base32 = new Base32();
            String encodedHash = base32.encodeAsString(hash);
            encodedHash = encodedHash.substring(0, encodedHash.length() - 4);

            String deviceId = "";

            for (int i = 0; i < encodedHash.length(); i++) {
                if (i != 0 && i % 6 == 0)
                    deviceId += '-';

                deviceId += encodedHash.charAt(i);
            }

            return deviceId;
        }
        catch (NoSuchAlgorithmException | CertificateEncodingException e)
        {
            Log.w(TAG, "Cannot calculate device id because it seems some problems with certificate: " + e.getMessage());
            return null;
        }
    }

    public static boolean checkSecurityStuff(File pemDirectory, boolean prepareForGeneration) {
        File key = new File(pemDirectory, KeyConfig.KEY_FILENAME);
        File cert = new File(pemDirectory, KeyConfig.CERTIFICATE_FILENAME);

        if (!key.exists() || !cert.exists()) {

            if (prepareForGeneration){
                //If one of the files is missing we need to generate new security stuff
                if (key.exists())
                    key.delete();

                if (cert.exists())
                    key.delete();
            }

            return false;
        }

        return true;
    }

}

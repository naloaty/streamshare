package com.naloaty.streamshare.security;

import android.util.Log;

import androidx.annotation.NonNull;

import com.naloaty.streamshare.config.KeyConfig;

import org.apache.commons.codec.binary.Base32;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
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

import retrofit2.internal.EverythingIsNonNull;

/**
 * This class helps to ensure security.
 */
public class SecurityUtils {

    private static final String TAG = "SecurityUtils";

    private static boolean isProviderInitialized = false;

    static {
        initBCProvider();
    }

    /**
     * Replaces the default crypto provider with BouncyCastle.
     */
    static void initBCProvider() {
        if (!isProviderInitialized){
            Security.removeProvider("BC");
            Security.insertProviderAt(new BouncyCastleProvider(), 1);
            isProviderInitialized = true;
        }
    }

    /**
     * Creates the SSl context that uses the StreamShare authentication method.
     * @param securityManager StreamShare security manager. Instance of {@link SecurityManager}.
     * @param pemDirectory Directory where security objects (certificate and key pair) are located.
     * @return SSLContext.
     */
    @EverythingIsNonNull
    public static SSLContext getSSLContext(SecurityManager securityManager, File pemDirectory) {
        return getSSlContext(pemDirectory, new SSTrustManager(securityManager));
    }

    /**
     * Crates the SSl context that uses the StreamShare authentication method.
     * @param pemDirectory Directory where security objects (certificate and key pair) are located.
     * @param trustManager TrustManager to be used for device authentication.
     * @return SSLContext.
     */
    @EverythingIsNonNull
    public static SSLContext getSSlContext(File pemDirectory, X509TrustManager trustManager) {
        try {
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

    /**
     * Creates a PKS12 key store that contains the local device security objects.
     * @param pemDirectory Directory where security objects (certificate and key pair) are located.
     * @param tempPassword Temporary password that will be used for secure key store.
     * @return PKS12 key store.
     */
    @EverythingIsNonNull
    private static KeyStore getPKCS12KeyStore(File pemDirectory, String tempPassword)
            throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
    {

        PrivateKey key = loadPrivateKey(KeyConfig.CRYPTO_PROVIDER, new File(pemDirectory, KeyConfig.HTTPS_KEY_FILENAME));
        X509Certificate cert = loadCertificate(new File(pemDirectory, KeyConfig.HTTPS_CERT_FILENAME));

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null);

        ks.setKeyEntry("SecObject", key, tempPassword.toCharArray(), new Certificate[] {cert} );

        return ks;
    }

    /**
     * Loads the X509 certificate from the PEM file.
     * @param certFile Certificate file.
     * @return X509 certificate.
     */
    @EverythingIsNonNull
    public static X509Certificate loadCertificate(File certFile) {
        try {
            FileInputStream is = new FileInputStream(certFile);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            return (X509Certificate) cf.generateCertificate(is);
        }
        catch (IOException | CertificateException e)
        {
            Log.w(TAG, "Cannot load certificate file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Loads the private key from the PEM file.
     * @param provider Crypto provider to be used to load private key.
     * @param keyFile Private key file.
     * @return Private key.
     */
    @EverythingIsNonNull
    private static PrivateKey loadPrivateKey(String provider, File keyFile) {
        try {
            Object stuff = loadPEMFile(keyFile);

            if (stuff instanceof PEMKeyPair) {
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

    /**
     * Loads the PEM file as security object.
     * @param pemFile PEM file to be loaded.
     * @return Security object.
     */
    private static Object loadPEMFile(@NonNull File pemFile) throws IOException {
        FileReader fileReader = new FileReader(pemFile);
        PEMParser pemReader = new PEMParser(fileReader);
        Object stuff = pemReader.readObject();
        pemReader.close();
        fileReader.close();

        return stuff;
    }

    /**
     * Calculates the StreamShare device ID from X509 certificate.
     * @param certificate X509 certificate.
     * @return The StreamShare device ID.
     */
    public static String calculateDeviceId(@NonNull X509Certificate certificate) {
        try {
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

    /**
     * Checks if all security objects are presented.
     * @param pemDirectory Directory where security objects (certificate and key pair) are located.
     * @param prepareForGeneration Delete existing security objects.
     */
    @EverythingIsNonNull
    public static boolean checkSecurityStuff(File pemDirectory, boolean prepareForGeneration) {
        File key = new File(pemDirectory, KeyConfig.KEY_FILENAME);
        File cert = new File(pemDirectory, KeyConfig.CERTIFICATE_FILENAME);

        if (!key.exists() || !cert.exists()) {
            if (prepareForGeneration){
                //If one of the files is missing we need to generate new security stuff.
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

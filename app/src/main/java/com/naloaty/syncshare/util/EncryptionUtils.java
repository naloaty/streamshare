package com.naloaty.syncshare.util;

import android.content.Context;
import android.os.Debug;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.codec.binary.Base32;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Date;
import java.util.Scanner;
import java.util.Vector;

/*
 * Based on https://medium.com/@abel.suviri.payan/create-rsa-key-on-android-for-sign-and-verify-9debbb566541
 */
public class EncryptionUtils {

    private static final String TAG = "EncryptionUtils";

    private static final int
            //TODO: is 3072 bit too heavy for android devices?
            KEY_SIZE = 2048,
            CERTIFICATE_VALID_FOR_DAYS = 365;

    private static final String
            //TODO: maybe we should to use AndroidKeystore instead?
            KEYSTORE_PROVIDER = "BC",
            KEY_ALIAS = "SyncShareRSAKeypair",
            SS_KEY_FILENAME = "key.pem",
            SS_CERTIFICATE_FILENAME = "cert.pem",
            SS_CERTIFICATE_ISSUER = "SyncShare",
            SS_CERTIFICATE_SUBJECT = "SyncShare",
            SS_CERTIFICATE_SIGNATURE_ALGORITHM = "SHA256withRSA";


    public static void generateStuff(Context context) {

        Log.d(TAG, "Generating stuff...");

        try
        {
            KeyPair keyPair = generateKeyPair(context);
            X509Certificate certificate = generateCertificate(keyPair);

            if (keyPair == null)
                return;

            if (certificate == null)
                return;

            File key = new File(context.getFilesDir(), SS_KEY_FILENAME);
            File cert = new File(context.getFilesDir(), SS_CERTIFICATE_FILENAME);

            saveStuff(key, keyPair.getPrivate());
            saveStuff(cert, certificate);
        }
        finally
        {
            Log.d(TAG, "Generating stuff... DONE!");
        }
    }

    private static void saveStuff(File file, Object stuff) {
        try
        {
            FileWriter keyFW = new FileWriter(file);
            JcaPEMWriter keyPemWriter = new JcaPEMWriter(keyFW);
            keyPemWriter.writeObject(stuff);
            keyPemWriter.flush();
            keyPemWriter.close();
            keyFW.close();
        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception while saving stuff");
            e.printStackTrace();
        }

    }

    public static boolean checkStuff(Context context) {
        File key = new File(context.getFilesDir(), SS_KEY_FILENAME);
        File cert = new File(context.getFilesDir(), SS_CERTIFICATE_FILENAME);

        return key.exists() && cert.exists();
    }

    private static KeyPair generateKeyPair(Context context) {
        try
        {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE_PROVIDER);

        /*KeyGenParameterSpec spec =
                new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_VERIFY | KeyProperties.PURPOSE_SIGN)
                .setCertificateSerialNumber(BigInteger.valueOf(777)) //Serial number used for the self-signed certificate of the generated key pair, default is 1
                .setCertificateSubject(new X500Principal("CN=SyncShare")) //Subject used for the self-signed certificate of the generated key pair, default is CN=fake
                .setDigests(KeyProperties.DIGEST_SHA256) //Set of digests algorithms with which the key can be used
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1) //Set of padding schemes with which the key can be used when signing/verifying
                .setCertificateNotBefore(startDate.getTime()) //Start of the validity period for the self-signed certificate of the generated, default Jan 1 1970
                .setCertificateNotAfter(endDate.getTime()) //End of the validity period for the self-signed certificate of the generated key, default Jan 1 2048
                .setUserAuthenticationRequired(false) //Sets whether this key is authorized to be used only if the user has been authenticated, default false
                .setUserAuthenticationValidityDurationSeconds(30) //Duration(seconds) for which this key is authorized to be used after the user is successfully authenticated
                .setKeySize(KEY_SIZE)
                .build();*/


            RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(KEY_SIZE, RSAKeyGenParameterSpec.F4);
            keyPairGenerator.initialize(spec, new SecureRandom());

            return keyPairGenerator.generateKeyPair();
        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception while generating keypair");
            e.printStackTrace();

            return null;
        }
    }

    private static X509Certificate generateCertificate(KeyPair keyPair) {

        /**
         * Generator of self-signed X509 v3 certificate
         * Based on: https://www.programcreek.com/java-api-examples/?api=org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
         * BC doc: https://www.bouncycastle.org/docs/pkixdocs1.5on/index.html
         * Orientation on https://docs.syncthing.net/dev/device-ids.html
         */

        Security.addProvider(new BouncyCastleProvider());

        try
        {
            AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find(SS_CERTIFICATE_SIGNATURE_ALGORITHM);
            AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
            AsymmetricKeyParameter privateKeyAsymKeyParam = PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded());

            ContentSigner signatureBuilder = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(privateKeyAsymKeyParam);

            X500Name issuer                    = new X500Name("CN=" + SS_CERTIFICATE_ISSUER);
            BigInteger serialNumber            = new BigInteger(64, new SecureRandom()); //or BigInteger.valueOf(System.currentTimeMillis())
            Date notBefore                     = new Date();
            Date notAfter                      = new Date(notBefore.getTime() + CERTIFICATE_VALID_FOR_DAYS * 86400000L);
            X500Name subject                   = new X500Name("CN=" + SS_CERTIFICATE_SUBJECT);
            SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

            X509v3CertificateBuilder builder = new X509v3CertificateBuilder(issuer, serialNumber, notBefore, notAfter, subject, publicKeyInfo);

            //https://stackoverflow.com/questions/16412315/creating-custom-x509-v3-extensions-in-java-with-bouncy-castle

            //Key usage extension
            X509KeyUsage keyUsage = new X509KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign);
            Extension extKeyUsage = new Extension(Extension.keyUsage, true, keyUsage.getEncoded());

            //Extended key usage (we can use that certificate in https)
            KeyPurposeId[] extendedUsages = {KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth};
            ExtendedKeyUsage extendedKeyUsage = new ExtendedKeyUsage(extendedUsages);
            Extension extExtendedKeyUsage = Extension.create(Extension.extendedKeyUsage, false, extendedKeyUsage);

            //Basic constraints
            BasicConstraints basicConstraints = new BasicConstraints(false);
            Extension extBasicConstraints = new Extension(Extension.basicConstraints, true, basicConstraints.getEncoded());

            builder.addExtension(extKeyUsage);
            builder.addExtension(extExtendedKeyUsage);
            builder.addExtension(extBasicConstraints);

            X509CertificateHolder certificateHolder = builder.build(signatureBuilder);
            return new JcaX509CertificateConverter().setProvider(KEYSTORE_PROVIDER).getCertificate(certificateHolder);
        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception while generating certificate");
            e.printStackTrace();

            return null;
        }

    }

    private static String loadFileAsString(File file) {
        try
        {
            Scanner myReader = new Scanner(file);
            String data = "";

            while (myReader.hasNextLine()) {
                data += myReader.nextLine() + "\n";
            }

            return data;
        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception while converting stuff to string");
            return null;
        }
    }

    private static PrivateKey loadSecretKey(Context context, String filename) {
        File secretKey = new File(context.getFilesDir(), filename);

        Object stuff = loadStuff(secretKey);

        if (stuff instanceof PrivateKey)
            return (PrivateKey) stuff;
        else
            return null;
    }

    private static X509Certificate loadCertificate(Context context, String filename) {
        File certificate = new File(context.getFilesDir(), filename);
        Object stuff = loadStuff(certificate);

        try
        {
            if (stuff instanceof X509CertificateHolder)
                return new JcaX509CertificateConverter().setProvider(KEYSTORE_PROVIDER).getCertificate((X509CertificateHolder)stuff);
        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception while loading certificate");
            e.printStackTrace();
        }

        return null;
    }

    private static X509Certificate loadCertificate(String certificate) {
        Object stuff = loadStuff(certificate);

        try
        {
            if (stuff instanceof X509CertificateHolder)
                return new JcaX509CertificateConverter().setProvider(KEYSTORE_PROVIDER).getCertificate((X509CertificateHolder)stuff);
        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception while loading certificate");
            e.printStackTrace();
        }

        return null;
    }

    public static String loadCertificateAsString(Context context) {
        File cert = new File(context.getFilesDir(), SS_CERTIFICATE_FILENAME);

        return loadFileAsString(cert);
    }

    private static Object loadStuff(String stuffStr) {

        try
        {
            StringReader stringReader = new StringReader(stuffStr);
            PEMParser pemReader = new PEMParser(stringReader);
            Object stuff = pemReader.readObject();
            pemReader.close();
            stringReader.close();

            return stuff;
        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception while reading stuff string");

            return null;
        }
    }

    private static Object loadStuff(File stuffFile) {
        try
        {
            FileReader fileReader = new FileReader(stuffFile);
            PEMParser pemReader = new PEMParser(fileReader);
            Object stuff = pemReader.readObject();
            pemReader.close();
            fileReader.close();

            return stuff;
        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception while reading stuff file");

            return null;
        }
    }

    private static String calculateDeviceId(X509Certificate certificate) {
        if (certificate == null) {
            Log.d(TAG, "Cannot calculate device id: certificate is null");
            return null;
        }

        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(certificate.toString().getBytes(StandardCharsets.UTF_8));

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
        catch (Exception e)
        {
            Log.d(TAG, "Exception while calculating device id");
            e.printStackTrace();
            return null;
        }
    }

    public static String calculateDeviceId(Context context) {
        return calculateDeviceId(loadCertificate(context, SS_CERTIFICATE_FILENAME));
    }

    public static String calculateDeviceId(String certificate) {
        return calculateDeviceId(loadCertificate(certificate));
    }

    private static void signData() {
        try {
            //We get the Keystore instance
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);

            //Retrieves the private key from the keystore
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(KEY_ALIAS, null);

            //We sign the data with the private key. We use RSA algorithm along SHA-256 digest algorithm
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(privateKey);
            sign.update("Test data".getBytes());

            byte[] signature = sign.sign();



            if (signature != null) {
                //We encode and store in a variable the value of the signature
                String signatureResult = Base64.encodeToString(signature, Base64.DEFAULT);
            }

        } catch (KeyPermanentlyInvalidatedException e) {
            //Exception thrown when the key has been invalidated for example when lock screen has been disabled.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void verifyData() throws Exception{
        //We get the Keystore instance
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);

        //We get the certificate from the keystore
        Certificate certificate = keyStore.getCertificate(KEY_ALIAS);

        if (certificate != null) {
            //We decode the signature value
            byte[] signature = Base64.decode("here signature", Base64.DEFAULT);

            //We check if the signature is valid. We use RSA algorithm along SHA-256 digest algorithm
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initVerify(certificate);
            Boolean isValid = sign.verify(signature);
        }
    }
}

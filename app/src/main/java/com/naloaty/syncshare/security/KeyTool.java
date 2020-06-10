package com.naloaty.syncshare.security;

import android.os.AsyncTask;
import android.security.keystore.KeyProperties;
import android.util.Log;

import com.naloaty.syncshare.config.KeyConfig;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Date;

//TODO: add documentation
public class KeyTool {

    private static final String TAG = "KeyTool";

    static {
        SecurityUtils.initBCProvider();
    }

    public static void createSecurityStuff(final File saveDirectory, final KeyGeneratorCallback callback) {
        new GenerateSecurityStuffAT(saveDirectory, callback).execute();
    }

    private static void saveStuff(File file, Object stuff) throws IOException{
        FileWriter fileWriter = new FileWriter(file);
        JcaPEMWriter pemWriter = new JcaPEMWriter(fileWriter);
        pemWriter.writeObject(stuff);
        pemWriter.flush();
        pemWriter.close();
        fileWriter.close();

    }

    private static KeyPair generateKeyPair(String provider, int keysize) throws NoSuchProviderException
    {
        try
        {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, provider);
            RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(keysize, RSAKeyGenParameterSpec.F4);
            keyPairGenerator.initialize(spec, new SecureRandom());

            return keyPairGenerator.generateKeyPair();
        }
        catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e)
        {
            Log.w(TAG, "Key generator is not configured correctly: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generator of self-signed X509 v3 certificate
     */
    private static X509Certificate generateCertificate(final String provider, final CertificateConfig config) {

        try
        {
            AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find(config.getSignatureAlgorithm());
            AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
            AsymmetricKeyParameter privateKeyAsymKeyParam = PrivateKeyFactory.createKey(config.getKeyPair().getPrivate().getEncoded());

            ContentSigner signatureBuilder = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(privateKeyAsymKeyParam);

            X500Name issuer                    = new X500Name("CN=" + config.getIssuer());
            BigInteger serialNumber            = new BigInteger(64, new SecureRandom()); //or BigInteger.valueOf(System.currentTimeMillis())
            Date notBefore                     = config.getNotBefore();
            Date notAfter                      = config.getNotAfter();
            X500Name subject                   = new X500Name("CN=" + config.getSubject());
            SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(config.getKeyPair().getPublic().getEncoded());

            X509v3CertificateBuilder builder = new X509v3CertificateBuilder(issuer, serialNumber, notBefore, notAfter, subject, publicKeyInfo);

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
            return new JcaX509CertificateConverter().setProvider(provider).getCertificate(certificateHolder);
        }
        catch (IOException | CertificateException | OperatorCreationException e)
        {
            Log.w(TAG, "Cannot create security certificate: " + e.getMessage());
            return null;
        }

    }

    private static class GenerateSecurityStuffAT extends AsyncTask<Void, Void, Void> {

        private final KeyGeneratorCallback callback;
        private final File saveDirectory;

        public GenerateSecurityStuffAT(final File saveDirectory, final KeyGeneratorCallback callback) {
            this.callback = callback;
            this.saveDirectory = saveDirectory;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            callback.onStart();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try
            {
                KeyPair keyPair = generateKeyPair(KeyConfig.CRYPTO_PROVIDER, KeyConfig.KEY_SIZE);

                if (keyPair == null)
                    cancel(true);

                CertificateConfig certConfig = new CertificateConfig(keyPair);
                X509Certificate certificate = generateCertificate(KeyConfig.CRYPTO_PROVIDER, certConfig);

                if (certificate == null)
                    cancel(true);

                File key = new File(saveDirectory, KeyConfig.KEY_FILENAME);
                File cert = new File(saveDirectory, KeyConfig.CERTIFICATE_FILENAME);

                saveStuff(key, keyPair.getPrivate());
                saveStuff(cert, certificate);

            }
            catch (NoSuchProviderException e)
            {
                Log.w(TAG, "Crypto provider is not configured correctly: " + e.getMessage());
                cancel(true);
            }
            catch (IOException e)
            {
                Log.w(TAG, "Cannot save generated stuff: " + e.getMessage());
                cancel(true);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            callback.onFinish();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            callback.onFail();
        }
    }

    public static class CertificateConfig
    {
        private KeyPair keyPair;
        private String signatureAlgorithm = null;
        private String issuer = null;
        private String subject = null;
        private Date notBefore = null;
        private Date notAfter = null;

        public CertificateConfig(KeyPair keyPair) {
            this.keyPair = keyPair;
        }

        public KeyPair getKeyPair() {
            return keyPair;
        }

        public String getSignatureAlgorithm() {
            return signatureAlgorithm == null ? KeyConfig.CERTIFICATE_SIGNATURE_ALGORITHM : signatureAlgorithm;
        }

        public void setSignatureAlgorithm(String signatureAlgorithm) {
            this.signatureAlgorithm = signatureAlgorithm;
        }

        public String getIssuer() {
            return issuer == null ? KeyConfig.CERTIFICATE_ISSUER : issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getSubject() {
            return subject == null ? KeyConfig.CERTIFICATE_SUBJECT : subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public Date getNotBefore() {
            return notBefore == null ? new Date() : notBefore;
        }

        public void setNotBefore(Date notBefore) {
            this.notBefore = notBefore;
        }

        public Date getNotAfter() {
            return notAfter == null ? new Date(getNotBefore().getTime() + KeyConfig.CERTIFICATE_VALID_PERIOD * 86400000L) : notAfter;
        }

        public void setNotAfter(Date notAfter) {
            this.notAfter = notAfter;
        }
    }

    public interface KeyGeneratorCallback {
        void onStart();
        void onFinish();
        void onFail();
    }

}

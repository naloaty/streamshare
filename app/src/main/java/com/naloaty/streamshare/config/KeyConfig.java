package com.naloaty.streamshare.config;

/**
 * This class contains private key and ssl certificate configuration values.
 */
public class KeyConfig {

    /* RSA KEY */
    public static final int KEY_SIZE = 2048;

    public static final String CRYPTO_PROVIDER = "BC";

    public static final String KEY_FILENAME         = "key.pem";
    public static final String HTTPS_KEY_FILENAME   = "key.pem";
    public static final String HTTPS_CERT_FILENAME  = "cert.pem";
    public static final String CERTIFICATE_FILENAME = "cert.pem";

    /**
     * These values used as default in {@link com.naloaty.streamshare.security.KeyTool.CertificateConfig}
     */
    public static final String CERTIFICATE_SIGNATURE_ALGORITHM = "SHA256withRSA";
    public static final String CERTIFICATE_ISSUER              = "StreamShare";
    public static final String CERTIFICATE_SUBJECT             = "StreamShare";
    public static final int    CERTIFICATE_VALID_PERIOD        = 365 * 5;

}

package com.naloaty.streamshare.config;

/**
 * This class contains general StreamShare configuration values.
 */
public class AppConfig {

    public static final String APP_VERSION         = "0.4.1 indev::mobile";
    public static final String DNSSD_SERVICE_NAME  = "StreamShare";
    public static final String DNSSD_SERVICE_TYPE  = "_sscomm._tcp.";
    public static final String PLATFORM_MOBILE     = "mobile";
    public static final String PLATFORM_DESKTOP    = "desktop";
    public static final String DEFAULT_PREFERENCES = "default";

    public static final int DNSSD_SERVER_PORT = 12321;
    public static final int MEDIA_SERVER_PORT = 12322;
}

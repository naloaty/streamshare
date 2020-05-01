package com.naloaty.syncshare.config;

import com.naloaty.syncshare.database.SSDevice;

public class AppConfig {
    public static final String
        APP_VERSION = "1.0 indev::mobile",
        DNSSD_SERVICE_NAME = "SyncShare",
        DNSSD_SERVICE_TYPE = "_syncsharecomm._tcp.",
        PLATFORM_MOBILE = "mobile",
        PLATFORM_DESKTOP = "desktop";

    public static final int
        DNSSD_SERVER_PORT = 12321,
        MEDIA_SERVER_PORT = 12322;
}

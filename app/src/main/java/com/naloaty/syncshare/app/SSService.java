package com.naloaty.syncshare.app;


import com.naloaty.syncshare.util.NotificationUtils;

abstract public class SSService extends android.app.Service {

    private NotificationUtils mNotificationUtils;


    protected NotificationUtils getNotificationUtils() {
        if (mNotificationUtils == null)
            mNotificationUtils = new NotificationUtils(getApplicationContext());

        return mNotificationUtils;
    }

}

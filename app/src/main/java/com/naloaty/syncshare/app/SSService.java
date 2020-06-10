package com.naloaty.syncshare.app;

import com.naloaty.syncshare.util.NotificationUtils;

/**
 * This class helps manage the notification utils
 * @see NotificationUtils
 * @see com.naloaty.syncshare.service.CommunicationService
 */
abstract public class SSService extends android.app.Service {

    private NotificationUtils mNotificationUtils;

    /**
     * @return Instance of notification utils.
     */
    protected NotificationUtils getNotificationUtils() {
        if (mNotificationUtils == null)
            mNotificationUtils = new NotificationUtils(getApplicationContext());

        return mNotificationUtils;
    }

}

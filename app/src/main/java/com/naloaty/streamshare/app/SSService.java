package com.naloaty.streamshare.app;

import com.naloaty.streamshare.util.NotificationUtils;

/**
 * This class helps manage the notification utils
 * @see NotificationUtils
 * @see com.naloaty.streamshare.service.CommunicationService
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

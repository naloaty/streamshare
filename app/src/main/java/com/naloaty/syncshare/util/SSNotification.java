package com.naloaty.syncshare.util;


import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * This class helps to manage notifications in the status bar.
 */
public class SSNotification extends NotificationCompat.Builder {

    private int mNotificationId;
    private NotificationManagerCompat mNotificationManager;

    /**
     * @param context The Context in which this instance should be created.
     * @param channelId ID of the channel on which the notification will be displayed.
     * @param notificationId ID of the notification.
     */
    public SSNotification(Context context, NotificationManagerCompat manager, String channelId, int notificationId) {
        super(context, channelId);

        this.mNotificationId = notificationId;
        this.mNotificationManager = manager;
    }

    /**
     * Shows a notification in the status bar.
     * @return Instance of this class.
     */
    public SSNotification show() {
        mNotificationManager.notify(mNotificationId, build());
        return this;
    }

    /**
     * Dismisses a notification in the status bar.
     * @return Instance of this class.
     */
    public SSNotification cancel() {
        mNotificationManager.cancel(mNotificationId);
        return this;
    }
}

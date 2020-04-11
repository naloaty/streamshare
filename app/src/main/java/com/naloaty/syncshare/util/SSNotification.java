package com.naloaty.syncshare.util;


import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class SSNotification extends NotificationCompat.Builder {

    private int mNotificationId;
    private NotificationManagerCompat mNotificationManager;

    public SSNotification(Context context, NotificationManagerCompat manager, String channelId, int notificationId) {
        super(context, channelId);

        this.mNotificationId = notificationId;
        this.mNotificationManager = manager;
    }

    public SSNotification show() {
        mNotificationManager.notify(mNotificationId, build());
        return this;
    }

    public SSNotification cancel() {
        mNotificationManager.cancel(mNotificationId);
        return this;
    }
}

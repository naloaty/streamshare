package com.naloaty.syncshare.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;

import com.naloaty.syncshare.R;

/**
 * This class helps to display notifications in the status bar.
 * @see CommunicationNotification
 */
public class NotificationUtils {

    public static final String NOTIFICATION_CHANNEL_HIGH = "ssHighPriority";
    public static final String NOTIFICATION_CHANNEL_LOW = "ssLowPriority";

    private Context mContext;
    private NotificationManagerCompat mNotificationManager;

    /**
     * @param context The Context in which this instance should be created.
     */
    public NotificationUtils(Context context) {
        this.mContext = context;
        this.mNotificationManager = NotificationManagerCompat.from(context);

        //Allow user to disable channels in system settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel channelHigh =
                    new NotificationChannel(
                            NOTIFICATION_CHANNEL_HIGH,
                            mContext.getString(R.string.text_ncHighPriority),
                            NotificationManager.IMPORTANCE_HIGH);
            channelHigh.setDescription(context.getString(R.string.text_ncHighPriorityDescription));

            notificationManager.createNotificationChannel(channelHigh);

            NotificationChannel channelLow =
                    new NotificationChannel(NOTIFICATION_CHANNEL_LOW,
                            mContext.getString(R.string.text_ncLowPriority),
                            NotificationManager.IMPORTANCE_LOW);
            channelLow.setDescription(context.getString(R.string.text_ncLowPriorityDescription));

            notificationManager.createNotificationChannel(channelLow);
        }
    }

    /**
     * Creates a new notification.
     * @param channelId ID of the channel on which the notification will be displayed.
     * @param notificationId ID of the notification.
     */
    public SSNotification createNotification(String channelId, int notificationId) {
        return new SSNotification(mContext, mNotificationManager, channelId, notificationId);
    }

    /**
     * Cancels an existing notification.
     * @param notificationId ID of the notification.
     */
    public void cancelNotification(int notificationId) {
        mNotificationManager.cancel(notificationId);
    }

    public Context getContext(){
        return mContext;
    }
}

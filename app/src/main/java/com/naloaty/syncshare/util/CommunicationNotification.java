package com.naloaty.syncshare.util;

import android.app.PendingIntent;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.service.CommunicationService;

public class CommunicationNotification {

    public static int SERVICE_NOTIFICATION_ID = 12345;
    private NotificationUtils notificationUtils;

    public CommunicationNotification (NotificationUtils notificationUtils) {
        this.notificationUtils = notificationUtils;
    }

    public SSNotification getServiceNotification() {
        SSNotification notification =
                notificationUtils.createNotification(NotificationUtils.NOTIFICATION_CHANNEL_LOW, SERVICE_NOTIFICATION_ID);

        String titleContent = notificationUtils.getContext().getString(R.string.text_communicationServiceIdle);
        String textContent = notificationUtils.getContext().getString(R.string.text_communicationServiceIdleAction);

        //Setting up action on tap
        int requestCode = AppUtils.getUniqueNumber();

        Intent intent = new Intent(notificationUtils.getContext(), CommunicationService.class);
        intent.setAction(CommunicationService.ACTION_STOP_SHARING);

        PendingIntent contentAction =
                PendingIntent.getService(notificationUtils.getContext(), requestCode, intent, 0);

        //------------------------

        notification.setSmallIcon(R.drawable.ic_share_24dp)
                .setContentTitle(titleContent)
                .setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true) //After tap notification will be auto canceled
                .setContentIntent(contentAction)
                .setOngoing(true);


        return notification.show();
    }
}

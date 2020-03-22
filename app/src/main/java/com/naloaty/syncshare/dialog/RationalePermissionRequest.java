package com.naloaty.syncshare.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.Activity;


public class RationalePermissionRequest extends AlertDialog.Builder
{
    public PermissionRequest mPermissionQueue;

    public RationalePermissionRequest(final Activity activity,
                                      @NonNull PermissionRequest permission,
                                      boolean killActivityOtherwise)
    {
        super(activity);

        mPermissionQueue = permission;

        setCancelable(false);
        setTitle(permission.title);
        setMessage(permission.message);

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, mPermissionQueue.permission))
            setNeutralButton(R.string.btn_settings, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    Intent intent = new Intent()
                            .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(Uri.fromParts("package", activity.getPackageName(), null));

                    activity.startActivity(intent);
                }
            });

        setPositiveButton(R.string.btn_ask, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                ActivityCompat.requestPermissions(activity, new String[]{mPermissionQueue.permission}, Activity.REQUEST_PERMISSION_ALL);
            }
        });

        if (killActivityOtherwise)
            setNegativeButton(R.string.btn_reject, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    activity.finish();
                }
            });
        else
            setNegativeButton(R.string.btn_close, null);
    }

    //This method will show dialog if permission not granted
    public static AlertDialog requestIfNecessary(Activity activity,
                                                 PermissionRequest permissionQueue,
                                                 boolean killActivityOtherwise)
    {
        return ActivityCompat.checkSelfPermission(activity, permissionQueue.permission) == PackageManager.PERMISSION_GRANTED
                ? null
                : new RationalePermissionRequest(activity, permissionQueue, killActivityOtherwise).show();
    }

    //This inner class contains information that will be shown in dialog
    public static class PermissionRequest
    {
        public String permission;
        public String title;
        public String message;
        boolean required;

        public PermissionRequest(String permission, String title, String message)
        {
            this(permission, title, message, true);
        }

        public PermissionRequest(String permission, String title, String message, boolean required)
        {
            this.permission = permission;
            this.title = title;
            this.message = message;
            this.required = required;
        }

        public PermissionRequest(Context context, String permission, int titleRes, int messageRes)
        {
            this(permission, context.getString(titleRes), context.getString(messageRes));
        }
    }
}
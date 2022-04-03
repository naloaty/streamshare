package com.naloaty.streamshare.dialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.naloaty.streamshare.R;
import com.naloaty.streamshare.app.SSActivity;
import com.naloaty.streamshare.util.PermissionHelper;

/**
 * This class represents a dialog that asks the user for permission.
 * Also displays a short description of the permission.
 * @see PermissionHelper
 */
public class PermissionRequest extends AlertDialog.Builder {

    private PermissionHelper.Permission mPermission;

    public PermissionRequest(final Activity activity, @NonNull PermissionHelper.Permission permission, boolean killActivityOtherwise)
    {
        super(activity);

        mPermission = permission;

        setCancelable(false);
        setTitle(permission.getTitleResource());
        setMessage(permission.getMessageResource());

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, mPermission.getPermission())){
            setNeutralButton(R.string.btn_settings, (dialog, which) -> {
                Intent intent = new Intent()
                        .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.fromParts("package", activity.getPackageName(), null));

                activity.startActivity(intent);
            } );
        }

        setPositiveButton(R.string.btn_ask, (dialog, which)
                -> ActivityCompat.requestPermissions(activity, new String[]{mPermission.getPermission()}, SSActivity.PERMISSION_REQUEST));

        if (killActivityOtherwise)
            setNegativeButton(R.string.btn_reject, (dialog, which) -> activity.finish());
        else
            setNegativeButton(R.string.btn_close, null);
    }
}
package com.naloaty.syncshare.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.dialog.PermissionRequest;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.POWER_SERVICE;
import static com.naloaty.syncshare.util.AppUtils.OPTIMIZATION_DISABLE;

public class PermissionHelper {

    public static boolean checkRequiredPermissions(Context context)
    {
        for (PermissionHelper.Permission request : getRequiredPermissions())
            if (ActivityCompat.checkSelfPermission(context, request.permission) != PackageManager.PERMISSION_GRANTED)
                return false;

        return true;
    }

    public static boolean checkBatteryOptimizationDisabled(Context context) {
        if (Build.VERSION.SDK_INT < 23)
            return true;

        String packageName = context.getPackageName();
        PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);

        return pm.isIgnoringBatteryOptimizations(packageName);
    }

    public static void requestDisableBatteryOptimization(Activity activity) {
        if (Build.VERSION.SDK_INT < 23)
            return;

        String packageName = activity.getPackageName();

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + packageName));
        activity.startActivityForResult(intent, OPTIMIZATION_DISABLE);
    }


    public static List<Permission> getRequiredPermissions()
    {
        List<Permission> permissions = new ArrayList<>();

        permissions.add(new Permission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                R.string.title_storagePermission,
                R.string.text_storagePermissionHelp));

        /*if (Build.VERSION.SDK_INT >= 26) {
            permissions.add(new Permission(
                    Manifest.permission.READ_PHONE_STATE,
                    R.string.title_readPhoneStatePermission,
                    R.string.text_readPhoneStatePermissionHelp));
        }*/

        return permissions;
    }

    /*public static void requestLocationPermission(final Activity activity, final int requestId)
    {
        if (!checkLocationPermission(activity)) {
            new AlertDialog.Builder(activity)
                    .setMessage(R.string.text_locationPermissionHelp)
                    .setNegativeButton(R.string.btn_close, null)
                    .setPositiveButton(R.string.btn_ask, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION}, requestId);
                        }
                    }).show();
        }

    }*/

    public static boolean checkLocationPermission(Context context) {

        if (Build.VERSION.SDK_INT < 23)
            return true;

        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    public static AlertDialog requestIfNotGranted(Activity activity, Permission permission, boolean killActivityOtherwise)
    {
        return ActivityCompat.checkSelfPermission(activity, permission.permission) == PackageManager.PERMISSION_GRANTED
                ? null
                : new PermissionRequest(activity, permission, killActivityOtherwise).show();
    }

    public static class Permission
    {
        String permission;
        int titleResource;
        int messageResource;
        boolean required;

        public Permission(String permission, int titleResource, int messageResource)
        {
            this(permission, titleResource, messageResource, true);
        }

        public Permission(String permission, int titleResource, int messageResource, boolean required)
        {
            this.permission = permission;
            this.titleResource = titleResource;
            this.messageResource = messageResource;
            this.required = required;
        }

        public String getPermission() {
            return permission;
        }

        public void setPermission(String permission) {
            this.permission = permission;
        }

        public int getTitleResource() {
            return titleResource;
        }

        public void setTitleResource(int titleResource) {
            this.titleResource = titleResource;
        }

        public int getMessageResource() {
            return messageResource;
        }

        public void setMessageResource(int messageResource) {
            this.messageResource = messageResource;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }
    }



}

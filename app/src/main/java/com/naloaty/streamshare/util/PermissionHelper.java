package com.naloaty.streamshare.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.naloaty.streamshare.R;
import com.naloaty.streamshare.dialog.PermissionRequest;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.POWER_SERVICE;
import static com.naloaty.streamshare.util.AppUtils.BATTERY_OPTIMIZATION_DISABLE;

/**
 * This class helps to request and check permissions.
 */
public class PermissionHelper {

    /**
     * Checks if all required permissions are granted.
     * @param context The Context in which this operation should be executed.
     * @return True if all required permissions are granted.
     */
    public static boolean checkRequiredPermissions(Context context) {
        for (PermissionHelper.Permission request : getRequiredPermissions())
            if (ActivityCompat.checkSelfPermission(context, request.permission) != PackageManager.PERMISSION_GRANTED)
                return false;

        return true;
    }

    /**
     * Checks if battery optimization is disabled for StreamShare.
     * @param context The Context in which this operation should be executed.
     * @return True if battery optimization is disabled for StreamShare.
     */
    public static boolean checkBatteryOptimizationDisabled(Context context) {
        if (Build.VERSION.SDK_INT < 23)
            return true;

        String packageName = context.getPackageName();
        PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);

        if (pm == null)
            return false;

        return pm.isIgnoringBatteryOptimizations(packageName);
    }

    /**
     * Requests disabling battery optimization for StreamShare.
     * @param activity The Activity in which context this request should be executed.
     */
    public static void requestDisableBatteryOptimization(Activity activity) {
        if (Build.VERSION.SDK_INT < 23)
            return;

        String packageName = activity.getPackageName();

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + packageName));
        activity.startActivityForResult(intent, BATTERY_OPTIMIZATION_DISABLE);
    }


    /**
     * @return A list of all required permissions.
     */
    public static List<Permission> getRequiredPermissions() {
        List<Permission> permissions = new ArrayList<>();

        permissions.add(new Permission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                R.string.title_storagePermission,
                R.string.text_storagePermissionHelp));


        return permissions;
    }

    /**
     * Shows permission request dialog if permission is not granted.
     * @param activity The Activity in which context this request should be executed.
     * @param killActivityOtherwise Kill activity if the user denied this permission.
     * @see PermissionRequest
     */
    public static AlertDialog requestIfNotGranted(Activity activity, Permission permission, boolean killActivityOtherwise) {
        return ActivityCompat.checkSelfPermission(activity, permission.permission) == PackageManager.PERMISSION_GRANTED
                ? null
                : new PermissionRequest(activity, permission, killActivityOtherwise).show();
    }

    /**
     * This class represents permission.
     * @see #getRequiredPermissions()
     */
    public static class Permission {
        /**
         * Permission.
         */
        String permission;

        /**
         * Dialog title resource.
         */
        int titleResource;

        /**
         * Dialog message resource.
         */
        int messageResource;

        public Permission(String permission, int titleResource, int messageResource) {
            this.permission = permission;
            this.titleResource = titleResource;
            this.messageResource = messageResource;
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

    }

}

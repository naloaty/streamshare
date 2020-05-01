package com.naloaty.syncshare.util;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.config.AppConfig;
import com.naloaty.syncshare.config.Keyword;
import com.naloaty.syncshare.database.SSDevice;
import com.naloaty.syncshare.dialog.RationalePermissionRequest;
import com.naloaty.syncshare.config.KeyConfig;
import com.naloaty.syncshare.security.SecurityUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.POWER_SERVICE;


public class AppUtils {

    private static final String TAG = AppUtils.class.getSimpleName();
    private static int mUniqueNumber = 0;
    private static SharedPreferences mSharedPreferences;
    private static final String DEFAULT_PREF = "default";
    public static final int OPTIMIZATION_DISABLE = 756;

    private static DNSSDHelper mDNSSDHelper;

    public static SharedPreferences getDefaultSharedPreferences(final Context context) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(DEFAULT_PREF, Context.MODE_PRIVATE);
        }

        return mSharedPreferences;
    }

    //This method returns list of 'dangerous' permissions, that require dialog with description
    public static List<RationalePermissionRequest.PermissionRequest> getRequiredPermissions(Context context)
    {
        List<RationalePermissionRequest.PermissionRequest> permissionRequests = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= 16) {
            permissionRequests.add(new RationalePermissionRequest.PermissionRequest(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    R.string.text_requestPermissionStorage,
                    R.string.text_requestPermissionStorageSummary));
        }

        if (Build.VERSION.SDK_INT >= 26) {
            permissionRequests.add(new RationalePermissionRequest.PermissionRequest(context,
                    Manifest.permission.READ_PHONE_STATE,
                    R.string.text_requestPermissionReadPhoneState,
                    R.string.text_requestPermissionReadPhoneStateSummary));
        }

        return permissionRequests;
    }

    public static boolean checkRunningConditions(Context context)
    {
        for (RationalePermissionRequest.PermissionRequest request : getRequiredPermissions(context))
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

    public static void applyDeviceToJSON(Context context, JSONObject object) throws JSONException
    {
        SSDevice device = getLocalDevice(context);
        JSONObject deviceInformation = new JSONObject();

        deviceInformation.put(Keyword.DEVICE_INFO_SERIAL, device.getDeviceId());
        deviceInformation.put(Keyword.DEVICE_INFO_BRAND, device.getBrand());
        deviceInformation.put(Keyword.DEVICE_INFO_MODEL, device.getModel());
        deviceInformation.put(Keyword.DEVICE_INFO_USER, device.getNickname());
        deviceInformation.put(Keyword.APP_INFO_VERSION, AppConfig.APP_VERSION);

        object.put(Keyword.DEVICE_INFO, deviceInformation);
    }

    public static SSDevice getLocalDevice(Context context)
    {
        SSDevice device = new SSDevice(getDeviceId(context), AppConfig.APP_VERSION);

        device.setBrand(Build.BRAND);
        device.setModel(Build.MODEL);
        device.setNickname(AppUtils.getLocalDeviceName());

        return device;
    }

    public static String getDeviceId(Context context)
    {
        File certFile = new File(context.getFilesDir(), KeyConfig.CERTIFICATE_FILENAME);
        X509Certificate myCert = SecurityUtils.loadCertificate(KeyConfig.CRYPTO_PROVIDER, certFile);

        Log.w(TAG, "My device id: " + SecurityUtils.calculateDeviceId(myCert));
        //Log.w(TAG, "My certificate: " + myCert.toString());

        return SecurityUtils.calculateDeviceId(myCert);
    }

    public static String getLocalDeviceName()
    {
        //TODO: add ability to change name
        return Build.MODEL.toUpperCase();
    }

    public static int getUniqueNumber()
    {
        return (int) (System.currentTimeMillis() / 1000) + (++mUniqueNumber);
    }

    public static DNSSDHelper getDNSSDHelper(Context context) {
        if (mDNSSDHelper == null)
            mDNSSDHelper = new DNSSDHelper(context);

        return mDNSSDHelper;
    }

    /*
     * Copied from https://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android/5921190#5921190
     */
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())){
                return true;
            }
        }
        return false;
    }

    /*
     * Copied from https://tips.androidhive.info/2015/04/android-how-to-check-if-the-app-is-in-background-or-foreground/
     */
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }
}

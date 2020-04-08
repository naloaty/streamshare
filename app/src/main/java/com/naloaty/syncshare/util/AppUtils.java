package com.naloaty.syncshare.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.config.Keyword;
import com.naloaty.syncshare.database.DeviceConnectionRepository;
import com.naloaty.syncshare.dialog.RationalePermissionRequest;
import com.naloaty.syncshare.other.NetworkDevice;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class AppUtils {

    private static final String TAG = AppUtils.class.getSimpleName();
    private static int mUniqueNumber = 0;
    private static SharedPreferences mSharedPreferences;
    public static final String DEFAULT_PREF = "default";

    private static DeviceConnectionRepository mDeviceConnectionRepo;

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

    public static void applyDeviceToJSON(Context context, JSONObject object) throws JSONException
    {
        NetworkDevice device = getLocalDevice(context);
        JSONObject deviceInformation = new JSONObject();

        deviceInformation.put(Keyword.DEVICE_INFO_SERIAL, device.deviceId);
        deviceInformation.put(Keyword.DEVICE_INFO_BRAND, device.brand);
        deviceInformation.put(Keyword.DEVICE_INFO_MODEL, device.model);
        deviceInformation.put(Keyword.DEVICE_INFO_USER, device.nickname);

        object.put(Keyword.DEVICE_INFO, deviceInformation);
    }

    public static NetworkDevice getLocalDevice(Context context)
    {
        NetworkDevice device = new NetworkDevice(getDeviceSerial(context));

        device.brand = Build.BRAND;
        device.model = Build.MODEL;
        device.nickname = AppUtils.getLocalDeviceName();
        device.isLocalAddress = true;

        //TODO: add app version info

        return device;
    }

    public static String getDeviceSerial(Context context)
    {
        return Build.VERSION.SDK_INT < 26
                ? Build.SERIAL
                : (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED ? Build.getSerial() : null);
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

    public static DeviceConnectionRepository getDeviceConnectionRepository(Context context) {
        if (mDeviceConnectionRepo == null)
            mDeviceConnectionRepo = new DeviceConnectionRepository(context);

        return mDeviceConnectionRepo;
    }

    public static void startForegroundService(Context context, Intent intent)
    {
        if (Build.VERSION.SDK_INT >= 26)
            context.startForegroundService(intent);
        else
            context.startService(intent);

        Log.i(TAG, "Starting service");
    }
}

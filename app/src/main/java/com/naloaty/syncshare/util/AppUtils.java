package com.naloaty.syncshare.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.naloaty.syncshare.config.AppConfig;
import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.config.KeyConfig;
import com.naloaty.syncshare.security.SecurityUtils;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * This class contains useful utilities.
 */
public class AppUtils {

    private static final String TAG = "AppUtils";

    public static final int BATTERY_OPTIMIZATION_DISABLE = 756;
    private static int mUniqueNumber                     = 0;

    /**
     * @param r Resources.
     * @return The height of the status bar in pixels.
     * @see com.naloaty.syncshare.activity.VideoPlayerActivity
     * @see com.naloaty.syncshare.app.MediaActivity
     */
    public static int getStatusBarHeight(Resources r) {
        int resourceId = r.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            return r.getDimensionPixelSize(resourceId);

        return 0;
    }

    /**
     * @param context The Context in which this operation should be executed.
     * @return The height of the navigation bar in pixels.
     * @see com.naloaty.syncshare.activity.VideoPlayerActivity
     * @see com.naloaty.syncshare.app.MediaActivity
     */
    public static int getNavigationBarHeight(Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = getRealScreenSize(context);

        //navigation bar on the right
        if (appUsableSize.x < realScreenSize.x) {
            return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y).y;
        }

        //navigation bar at the bottom
        if (appUsableSize.y < realScreenSize.y) {
            return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y).y;
        }

        //navigation bar is not present
        return 0;
    }

    /**
     * @param context The Context in which this operation should be executed.
     * @return Usable screen size.
     * @see #getNavigationBarHeight(Context)
     */
    private static Point getAppUsableScreenSize(Context context) {
        Point size = new Point();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        if (windowManager == null)
            return size;

        Display display = windowManager.getDefaultDisplay();
        display.getSize(size);

        return size;
    }

    /**
     * @param context The Context in which this operation should be executed.
     * @return Real screen size.
     * @see #getNavigationBarHeight(Context)
     */
    private static Point getRealScreenSize(Context context) {
        Point size = new Point();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        if (windowManager == null)
            return size;

        Display display = windowManager.getDefaultDisplay();
        display.getRealSize(size);

        return size;
    }

    /**
     * @param context The Context in which this operation should be executed.
     * @return Information about local device.
     */
    public static SSDevice getLocalDevice(Context context) {
        SSDevice device = new SSDevice(getDeviceId(context), AppConfig.APP_VERSION);

        device.setBrand(Build.BRAND);
        device.setModel(Build.MODEL);
        device.setNickname(AppUtils.getLocalDeviceName());

        return device;
    }

    /**
     * TODO: see in bug tracker
     * @param context The Context in which this operation should be executed.
     * @return The StreamShare ID of local device.
     */
    public static String getDeviceId(Context context) {
        File certFile = new File(context.getFilesDir(), KeyConfig.CERTIFICATE_FILENAME);
        X509Certificate myCert = SecurityUtils.loadCertificate(certFile);

        if (myCert == null) {
            Log.i(TAG, "Cannot calculate local device id");
            return "undefined";
        }

        return SecurityUtils.calculateDeviceId(myCert);
    }

    /**
     * @return The name of local device.
     */
    public static String getLocalDeviceName() {
        //TODO: add ability to change name
        return Build.MODEL.toUpperCase();
    }

    /**
     * @return Unique number.
     */
    public static int getUniqueNumber() {
        return (int) (System.currentTimeMillis() / 1000) + (++mUniqueNumber);
    }

    /**
     * Checks if the service is running.
     * @param context The Context in which this operation should be executed.
     * @param serviceClass The class of service you want to check.
     */
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager == null)
            return false;

        List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())){
                return true;
            }
        }
        return false;
    }
}

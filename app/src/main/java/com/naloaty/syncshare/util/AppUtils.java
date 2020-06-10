package com.naloaty.syncshare.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
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

    public static int getStatusBarHeight(Resources r) {
        int resourceId = r.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            return r.getDimensionPixelSize(resourceId);

        return 0;
    }

    public static int getNavigationBarHeight(Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = getRealScreenSize(context);

        // navigation bar on the right
        if (appUsableSize.x < realScreenSize.x) {
            return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y).y;
        }

        // navigation bar at the bottom
        if (appUsableSize.y < realScreenSize.y) {
            return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y).y;
        }

        // navigation bar is not present
        return 0;
    }

    private static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    private static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        return size;
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

package com.naloaty.syncshare.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.naloaty.syncshare.R;

public class PermissionHelper {

    public static int REQUEST_LOCATION_PERMISSION = 1;

    public static void requestLocationPermission(final Activity activity, final int requestId)
    {
        if (!checkLocationPermission(activity)) {
            new AlertDialog.Builder(activity)
                    .setMessage(R.string.text_requestPermissionLocationServiceSummary)
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

    }

    public static boolean checkLocationPermission(Context context) {

        if (Build.VERSION.SDK_INT < 23)
            return true;

        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationService(Activity activity) {

        if (!checkLocationService(activity)) {
            new AlertDialog.Builder(activity)
                    .setMessage(R.string.text_requestPermissionLocationServiceSummary)
                    .setNegativeButton(R.string.btn_close, null)
                    .setPositiveButton(R.string.btn_enableLocationService, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                    }).show();
        }
    }

    public static boolean checkLocationService(Context context) {
        if (Build.VERSION.SDK_INT < 23)
            return true;

        LocationManager locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

}

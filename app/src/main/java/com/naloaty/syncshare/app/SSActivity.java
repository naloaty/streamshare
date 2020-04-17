package com.naloaty.syncshare.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.naloaty.syncshare.activity.WelcomeActivity;
import com.naloaty.syncshare.dialog.RationalePermissionRequest;
import com.naloaty.syncshare.service.CommunicationService;
import com.naloaty.syncshare.util.AppUtils;



public class SSActivity extends AppCompatActivity {

    public static final int REQUEST_PERMISSION_ALL = 1;
    public static final String WELCOME_SHOWN = "welcome_shown";

    private AlertDialog mOngoingRequest;
    private boolean mSkipPermissionRequest = false;
    private boolean mWelcomePageDisallowed = false;

    @Override
    protected void onResume() {
        super.onResume();

        if (!hasWelcomeScreenShown() && !mWelcomePageDisallowed) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        }
        else if (!AppUtils.checkRunningConditions(this)) {
            if (!mSkipPermissionRequest)
                requestRequiredPermissions(true);
        }
        else {
            Log.i("BASE_Activity", "onResume() -> start service");
            startService(new Intent(this, CommunicationService.class)
                        .setAction("kostyl")); //TODO: kostyl
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        /*if (AppUtils.checkRunningConditions(this)) {
            Log.i("BASE_Activity", "onPause() -> delete connections");

            AppUtils.startForegroundService(this,
                    new Intent(this, CommunicationService.class)
                            .setAction(CommunicationService.ACTION_STOP_DISCOVERING));
        }*/
    }


    protected SharedPreferences getDefaultSharedPreferences() {
        return AppUtils.getDefaultSharedPreferences(this);
    }

    public boolean requestRequiredPermissions(boolean killActivityOtherwise) {
        if (mOngoingRequest != null && mOngoingRequest.isShowing())
            return false;

        //Если permission not granted, тогда функция вернёт false и других диалоговых окон паказно не будет
        //Если пользователь нажал Ask, то следующее диалоговое окно будет показано by onRequestPermissionsResult
        for (RationalePermissionRequest.PermissionRequest request : AppUtils.getRequiredPermissions(this)){
            if ((mOngoingRequest = RationalePermissionRequest.requestIfNecessary(this, request, killActivityOtherwise)) != null)
                return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (AppUtils.checkRunningConditions(this)){
            Log.i("BASE_Activity", "onRequestPermissionsResult() -> start service");
            startService(new Intent(this, CommunicationService.class)
                            .setAction("kostyl")); //TODO: kostyl
        }
        else
            requestRequiredPermissions(!mSkipPermissionRequest);

    }

    public boolean hasWelcomeScreenShown() {
        return getDefaultSharedPreferences().getBoolean(WELCOME_SHOWN, false);
    }

    public void setSkipPermissionRequest(boolean skip)
    {
        mSkipPermissionRequest = skip;
    }

    public void setWelcomePageDisallowed(boolean disallow)
    {
        mWelcomePageDisallowed = disallow;
    }

}

package com.naloaty.syncshare.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.naloaty.syncshare.activity.WelcomeActivity;
import com.naloaty.syncshare.dialog.RationalePermissionRequest;
import com.naloaty.syncshare.util.AppUtils;



public class Activity extends AppCompatActivity {

    public static final int REQUEST_PERMISSION_ALL = 1;
    public static final String WELCOME_SHOWN = "welcome_shown";

    private AlertDialog mOngoingRequest;
    private boolean mSkipPermissionRequest = false;
    private boolean mWelcomePageDisallowed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!hasWelcomeScreenShown() && !mWelcomePageDisallowed) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        }
        else if (!AppUtils.checkRunningConditions(this))
        {
            if (!mSkipPermissionRequest)
                requestRequiredPermissions(true);
        }
    }

    protected SharedPreferences getDefaultSharedPreferences()
    {
        return AppUtils.getDefaultSharedPreferences(this);
    }

    public boolean requestRequiredPermissions(boolean killActivityOtherwise)
    {
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

        /*if (AppUtils.checkRunningConditions(this))
            AppUtils.startForegroundService(this, new Intent(this, CommunicationService.class));
        else
            requestRequiredPermissions(!mSkipPermissionRequest);*/

        if (!AppUtils.checkRunningConditions(this))
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

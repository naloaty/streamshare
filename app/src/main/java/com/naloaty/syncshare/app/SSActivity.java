package com.naloaty.syncshare.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.activity.WelcomeActivity;
import com.naloaty.syncshare.dialog.RationalePermissionRequest;
import com.naloaty.syncshare.dialog.SSProgressDialog;
import com.naloaty.syncshare.security.KeyTool;
import com.naloaty.syncshare.security.SecurityUtils;
import com.naloaty.syncshare.service.CommunicationService;
import com.naloaty.syncshare.util.AppUtils;


public class SSActivity extends AppCompatActivity {

    private static final String TAG = "SSActivity";
    public static final int REQUEST_PERMISSION_ALL = 1;
    public static final String WELCOME_SHOWN = "welcome_shown";

    private AlertDialog mOngoingRequest;
    private boolean mSkipPermissionRequest = false;
    private boolean mWelcomePageDisallowed = false;
    private boolean mSkipStuffGeneration = false;

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
        else if (!SecurityUtils.checkSecurityStuff(getFilesDir(), true)) {
            if (mSkipStuffGeneration)
                return;

            generateSecurityStuff();
        }

    }

    private void generateSecurityStuff() {
        final SSProgressDialog ssDialog = new SSProgressDialog(SSActivity.this);

        ssDialog.setMessage(R.string.text_generatingInProgress);

        KeyTool.KeyGeneratorCallback callback = new KeyTool.KeyGeneratorCallback() {
            @Override
            public void onStart() {
                Log.i(TAG, "Creation of security stuff is started");
                ssDialog.show();
            }

            @Override
            public void onFinish() {
                Log.i(TAG, "Creation of security stuff is finished");
                if (ssDialog.isShowing()){
                    ssDialog.dismiss();
                    Toast.makeText(SSActivity.this, getText(R.string.toast_keysCreationSuccess), Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFail() {
                Log.i(TAG, "Creation of security stuff is failed");

                if (ssDialog.isShowing())
                    ssDialog.dismiss();

                DialogInterface.OnClickListener tryAgain = new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        generateSecurityStuff();
                    }
                };

                DialogInterface.OnClickListener exit = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exitApp();
                    }
                };

                new AlertDialog.Builder(SSActivity.this)
                        .setTitle(R.string.title_generationFailed)
                        .setMessage(R.string.text_generationFailed)
                        .setNegativeButton(R.string.btn_exitApp, exit)
                        .setPositiveButton(R.string.btn_tryAgain, tryAgain)
                        .show();
            }
        };

        KeyTool.createSecurityStuff(getFilesDir(), callback);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void exitApp()
    {
        stopService(new Intent(this, CommunicationService.class));
        finish();
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

    public void setSkipStuffGeneration(boolean mSkipStuffGeneration) {
        this.mSkipStuffGeneration = mSkipStuffGeneration;
    }
}

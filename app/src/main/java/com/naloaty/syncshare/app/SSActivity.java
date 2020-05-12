package com.naloaty.syncshare.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.activity.WelcomeActivity;
import com.naloaty.syncshare.dialog.PermissionRequest;
import com.naloaty.syncshare.dialog.SSProgressDialog;
import com.naloaty.syncshare.security.KeyTool;
import com.naloaty.syncshare.security.SecurityUtils;
import com.naloaty.syncshare.service.CommunicationService;
import com.naloaty.syncshare.util.AppUtils;
import com.naloaty.syncshare.util.PermissionHelper;


public class SSActivity extends AppCompatActivity {

    private static final String TAG = "SSActivity";
    public static final int PERMISSION_REQUEST = 1;

    public static final String WELCOME_SHOWN = "welcome_shown";
    public static final String PERMISSION_REQUEST_RESULT = "permission_request_result";
    public static final String SECURITY_STUFF_GENERATION_RESULT = "sec_stuff_gen_result";

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
        else if (!PermissionHelper.checkRequiredPermissions(this)) {
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

                Intent intent = new Intent(SECURITY_STUFF_GENERATION_RESULT);
                LocalBroadcastManager.getInstance(SSActivity.this).sendBroadcast(intent);

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

        for (PermissionHelper.Permission permission: PermissionHelper.getRequiredPermissions()){

            mOngoingRequest = PermissionHelper.requestIfNotGranted(this, permission, killActivityOtherwise);

            /* next dialog will be shown by onRequestPermissionsResult() */
            if (mOngoingRequest != null)
                return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (!PermissionHelper.checkRequiredPermissions(this))
            requestRequiredPermissions(!mSkipPermissionRequest);

        Log.d(TAG, "Sending broadcast");
        Intent intent = new Intent(PERMISSION_REQUEST_RESULT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

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

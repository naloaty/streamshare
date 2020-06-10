package com.naloaty.syncshare.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.activity.WelcomeActivity;
import com.naloaty.syncshare.dialog.SSProgressDialog;
import com.naloaty.syncshare.security.KeyTool;
import com.naloaty.syncshare.security.SecurityUtils;
import com.naloaty.syncshare.service.CommunicationService;
import com.naloaty.syncshare.util.AppUtils;
import com.naloaty.syncshare.util.PermissionHelper;

/**
 * This class helps manage permissions and checks if SSL certificate is presented.
 * It also displays a “welcome screen,” if necessary.
 * All activities in the StreamShare project extend this class.
 */
public abstract class SSActivity extends AppCompatActivity {

    private static final String TAG = "SSActivity";

    public static final int    PERMISSION_REQUEST               = 1;
    public static final String WELCOME_SHOWN                    = "welcome_shown";
    public static final String PERMISSION_REQUEST_RESULT        = "permission_request_result";
    public static final String SECURITY_STUFF_GENERATION_RESULT = "sec_stuff_gen_result";

    private AlertDialog mOngoingRequest;

    /**
     * Do not initialize other activities until the Welcome Activity has been completed.
     * @see #onResume()
     */
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

    /**
     * Initializes the process of creating an SSl certificate.
     * A dialog is displayed during the process.
     * @see KeyTool
     */
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

                new AlertDialog.Builder(SSActivity.this)
                        .setTitle(R.string.title_generationFailed)
                        .setMessage(R.string.text_generationFailed)
                        .setNegativeButton(R.string.btn_exitApp, (dialog, which) -> exitApp())
                        .setPositiveButton(R.string.btn_tryAgain, (dialog, which) -> generateSecurityStuff())
                        .show();
            }
        };

        KeyTool.createSecurityStuff(getFilesDir(), callback);
    }

    /**
     * Forces application to stop
     */
    public void exitApp() {
        stopService(new Intent(this, CommunicationService.class));
        finish();
    }

    /**
     * @return StreamShare default settings store
     * @see AppUtils
     */
    protected SharedPreferences getDefaultSharedPreferences() {
        return AppUtils.getDefaultSharedPreferences(this);
    }

    /**
     * Iterates over the required permissions and requests those that have not yet been granted.
     * @return Returns true if all required permissions are granted.
     * @see PermissionHelper
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        /* Requests permissions that have not yet been granted */
        if (!PermissionHelper.checkRequiredPermissions(this))
            requestRequiredPermissions(!mSkipPermissionRequest);

        Intent intent = new Intent(PERMISSION_REQUEST_RESULT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    /**
     * @return True if the welcome screen has already been shown.
     * @see #onResume()
     */
    public boolean hasWelcomeScreenShown() {
        return getDefaultSharedPreferences().getBoolean(WELCOME_SHOWN, false);
    }

    /**
     * Do not request the required permissions, even if they are not granted.
     * @see #requestRequiredPermissions(boolean)
     * @see #onResume()
     */
    public void setSkipPermissionRequest(boolean skip) {
        mSkipPermissionRequest = skip;
    }

    /**
     * Do not show the welcome screen, even if it was not shown.
     * @see #onResume()
     */
    public void setWelcomePageDisallowed(boolean disallow) {
        mWelcomePageDisallowed = disallow;
    }

    /**
     * Do not create an SSl certificate even if it's not created.
     * @see #generateSecurityStuff()
     * @see #onResume()
     */
    public void setSkipStuffGeneration(boolean mSkipStuffGeneration) {
        this.mSkipStuffGeneration = mSkipStuffGeneration;
    }
}

package com.naloaty.syncshare.activity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.SSActivity;
import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.dialog.EnterDeviceIdDialog;
import com.naloaty.syncshare.dialog.SingleTextInputDialog;
import com.naloaty.syncshare.fragment.DeviceInfoFragment;
import com.naloaty.syncshare.fragment.OptionFragment;
import com.naloaty.syncshare.fragment.AddOptionsFragment;
import com.naloaty.syncshare.service.CommunicationService;
import com.naloaty.syncshare.util.AddDeviceHelper;
import com.naloaty.syncshare.util.PermissionHelper;

import org.json.JSONObject;

public class AddDeviceActivity extends SSActivity {

    private static final String TAG = "AddDeviceActivity";

    public static final int REQUEST_LOCATION_BY_CODE_SCANNER = 3;
    public static final String ACTION_CHANGE_FRAGMENT = "com.naloaty.intent.action.PAIR_DEVICE_CHANGE_FRAGMENT";
    public static final String EXTRA_TARGET_FRAGMENT = "targetFragment";

    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mToolBarLayout;
    private Toolbar mToolBar;

    private AddOptionsFragment mAddOptionsFragment;
    private DeviceInfoFragment mConnectionInfoFragment ;

    private final AddDeviceHelper.AddDeviceCallback callback = new AddDeviceHelper.AddDeviceCallback() {
        @Override
        public void onSuccessfullyAdded() {
            DialogInterface.OnClickListener btnClose = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onBackPressed();
                }
            };

            new AlertDialog.Builder(AddDeviceActivity.this)
                    .setMessage(R.string.text_onSuccessfullyAdded)
                    .setPositiveButton(R.string.btn_close, btnClose)
                    .setTitle(R.string.title_success)
                    .show();
        }

        @Override
        public void onException(int errorCode) {

            Log.w(TAG, "Device added with exception. Error code is " + errorCode);

            DialogInterface.OnClickListener btnClose = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onBackPressed();
                }
            };

            int helpResource = R.string.text_defaultValue;
            int titleResource = R.string.text_defaultValue;

            switch (errorCode) {
                case AddDeviceHelper.ERROR_ALREADY_ADDED:
                    Toast.makeText(AddDeviceActivity.this, R.string.toast_alreadyAdded, Toast.LENGTH_LONG).show();
                    return;

                case AddDeviceHelper.ERROR_UNTRUSTED_DEVICE:
                    break;

                case AddDeviceHelper.ERROR_DEVICE_OFFLINE:
                    helpResource = R.string.text_offlineDevice;
                    break;

                case AddDeviceHelper.ERROR_BAD_RESPONSE:
                case AddDeviceHelper.ERROR_HANDSHAKE_EXCEPTION:
                    helpResource = R.string.text_handShakeException;
                    titleResource = R.string.title_step2;
                    break;

                case AddDeviceHelper.ERROR_REQUEST_FAILED:
                case AddDeviceHelper.ERROR_SENDING_FAILED:
                    helpResource = R.string.text_oExchangeFailed;
                    break;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(AddDeviceActivity.this)
                    .setMessage(helpResource)
                    .setPositiveButton(R.string.btn_close, btnClose);

            if (titleResource != R.string.text_defaultValue)
                builder.setTitle(titleResource);

            builder.show();

        }
    };

    private final IntentFilter mFilter = new IntentFilter();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (ACTION_CHANGE_FRAGMENT.equals(intent.getAction())
                    && intent.hasExtra(EXTRA_TARGET_FRAGMENT)) {

                //OptionFragment is enum
                OptionFragment targetFragment = OptionFragment.valueOf(intent.getStringExtra(EXTRA_TARGET_FRAGMENT));

                switch (targetFragment){

                    case EnterDeviceId:

                        SingleTextInputDialog.OnEnteredListener listener = new SingleTextInputDialog.OnEnteredListener() {
                            @Override
                            public void onEntered(String text) {
                                SSDevice ssDevice = AddDeviceHelper.getEmptyDevice();
                                ssDevice.setDeviceId(text);

                                AddDeviceHelper helper = new AddDeviceHelper(AddDeviceActivity.this, ssDevice, callback);
                                helper.processDevice();
                            }
                        };

                        new EnterDeviceIdDialog(AddDeviceActivity.this, listener).show();
                        break;

                    case ScanQR:
                        openCodeScanner();
                        break;

                    default:
                        setFragment(targetFragment);
                }


            }
            else if (intent.getAction().equals(CommunicationService.SERVICE_STATE_CHANGED)) {
                if (mAddOptionsFragment != null)
                    mAddOptionsFragment.setServiceState(intent.getBooleanExtra(CommunicationService.EXTRA_SERVICE_SATE, false));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_add);

        //======== Init collapsing toolbar ==========
        mAppBarLayout = findViewById(R.id.add_device_app_bar_layout);
        mToolBarLayout = findViewById(R.id.add_device_toolbar_layout);
        mToolBar = findViewById(R.id.add_device_toolbar);

        mToolBarLayout.setTitle(getString(R.string.title_addDevice));
        mAppBarLayout.setExpanded(true, true);

        //Important to call this BEFORE setNavigationOnClickListener()
        setSupportActionBar(mToolBar);

        //To make "close" animation (this instead of using "parent activity")
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_24dp);
        getSupportActionBar().setHomeButtonEnabled(true);

        //======== Init fragments =========
        mAddOptionsFragment = new AddOptionsFragment();
        mConnectionInfoFragment = new DeviceInfoFragment();

        setFragment(OptionFragment.Options);

        //======= Init broadcast =========
        mFilter.addAction(ACTION_CHANGE_FRAGMENT);
        mFilter.addAction(CommunicationService.SERVICE_STATE_CHANGED);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    public void onBackPressed() {

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.add_device_fragment_placeholder);

        if (currentFragment instanceof AddOptionsFragment)
            super.onBackPressed();
        else
            setFragment(OptionFragment.Options);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            processQRCode(result.getContents());
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_BY_CODE_SCANNER)
            if (PermissionHelper.checkLocationPermission(this))
                openCodeScanner();
    }

    private void openCodeScanner() {
        //TODO: it definitely should be replaced with customized scanner (with toolbar)
        /*
         * Since I don’t have enough time to create customized scanner,
         * so far the standard version of the scanner will be used
         */

        IntentIntegrator integrator = new IntentIntegrator(AddDeviceActivity.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setOrientationLocked(false);
        integrator.setBeepEnabled(false);
        integrator.initiateScan();

    }

    private void processQRCode(String qrCodeContents) {
        if (qrCodeContents == null)
            return;

        try {
            JSONObject codeDevice = new JSONObject(qrCodeContents);

            if (!codeDevice.has(DeviceInfoFragment.QR_CODE_DEVICE_NICKNAME))
                throw new Exception("Device nickname is missing");

            if (!codeDevice.has(DeviceInfoFragment.QR_CODE_APP_VERSION))
                throw new Exception("App version is missing");

            if (!codeDevice.has(DeviceInfoFragment.QR_CODE_DEVICE_ID))
                throw new Exception("Device ID is missing");


            SSDevice ssDevice = AddDeviceHelper.getEmptyDevice();
            ssDevice.setDeviceId(codeDevice.getString(DeviceInfoFragment.QR_CODE_DEVICE_ID));
            ssDevice.setNickname(codeDevice.getString(DeviceInfoFragment.QR_CODE_DEVICE_NICKNAME));
            ssDevice.setAppVersion(codeDevice.getString(DeviceInfoFragment.QR_CODE_APP_VERSION));

            AddDeviceHelper helper = new AddDeviceHelper(this, ssDevice, callback);
            helper.processDevice();

        }
        catch (Exception e) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.title_unrecognizedCode)
                    .setMessage(qrCodeContents)
                    .setPositiveButton(R.string.btn_close, null)
                    .show();
        }
    }

    private void setFragment(OptionFragment targetFragment) {

        Fragment candidate = null;

        switch (targetFragment) {
            case Options:
                candidate = mAddOptionsFragment;
                break;

            case DeviceInfo:
                candidate = mConnectionInfoFragment;
                break;
        }

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.add_device_fragment_placeholder);

        if (currentFragment == null ||  currentFragment != candidate) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (currentFragment != null)
                transaction.remove(currentFragment);

            if (currentFragment != null && candidate instanceof AddOptionsFragment)
                transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
            else
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);

            transaction.add(R.id.add_device_fragment_placeholder, candidate);
            transaction.commit();

            setToolbar(targetFragment);
        }
    }

    private void setToolbar(OptionFragment fragment) {

        int titleResource;
        boolean isOptions = false;

        switch (fragment) {
            case Options:
                titleResource = R.string.title_addDevice;
                isOptions = true;
                break;

            case DeviceInfo:
                titleResource = R.string.title_deviceInfo;
                break;

            default:
                titleResource = R.string.title_addDevice;
        }

        if (fragment.equals(OptionFragment.Options))
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_24dp);
        else
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp);

        mToolBarLayout.setTitle(getString(titleResource));
        mAppBarLayout.setExpanded(isOptions, true);
    }
}
package com.naloaty.streamshare.activity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
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
import com.naloaty.streamshare.R;
import com.naloaty.streamshare.app.SSActivity;
import com.naloaty.streamshare.database.device.SSDevice;
import com.naloaty.streamshare.dialog.EnterDeviceIdDialog;
import com.naloaty.streamshare.dialog.SingleTextInputDialog;
import com.naloaty.streamshare.fragment.DeviceInfoFragment;
import com.naloaty.streamshare.fragment.OptionFragment;
import com.naloaty.streamshare.fragment.AddOptionsFragment;
import com.naloaty.streamshare.service.CommunicationService;
import com.naloaty.streamshare.util.AddDeviceHelper;

import org.json.JSONObject;

/**
 * This activity represents the device adding screen.
 *
 * Related fragments:
 * @see AddOptionsFragment
 * @see DeviceInfoFragment
 * @see com.naloaty.streamshare.fragment.NearbyDiscoveryFragment
 */
public class AddDeviceActivity extends SSActivity {

    private static final String TAG = "AddDeviceActivity";
    public static final int REQUEST_LOCATION_BY_CODE_SCANNER = 3;
    public static final String ACTION_CHANGE_FRAGMENT = "com.naloaty.intent.action.PAIR_DEVICE_CHANGE_FRAGMENT";
    public static final String EXTRA_TARGET_FRAGMENT = "targetFragment";
    public static final String EXTRA_SAVED_FRAGMENT = "saved_fragment";

    private final IntentFilter mFilter = new IntentFilter();

    /* UI elements */
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mToolBarLayout;
    private Toolbar mToolBar;

    /*
     * TODO: Memory leak
     * Replace by getSupportFragmentManager().findFragmentById(R.id.add_device_fragment_placeholder);
     */
    private AddOptionsFragment mAddOptionsFragment;
    private DeviceInfoFragment mConnectionInfoFragment ;

    private OptionFragment mCurrentFragment;

    /**
     * This callback is called by {@link AddDeviceHelper}
     * @see #processQRCode(String)
     */
    private final AddDeviceHelper.AddDeviceCallback addDeviceCallback = new AddDeviceHelper.AddDeviceCallback() {
        @Override
        public void onSuccessfullyAdded() {
            DialogInterface.OnClickListener btnClose = (dialog, which) -> onBackPressed();

            new AlertDialog.Builder(AddDeviceActivity.this)
                    .setMessage(R.string.text_onSuccessfullyAdded)
                    .setPositiveButton(R.string.btn_close, btnClose)
                    .setTitle(R.string.title_success)
                    .show();
        }

        @Override
        public void onException(int errorCode) {
            Log.w(TAG, "Device added with exception. Error code is " + errorCode);

            DialogInterface.OnClickListener btnClose = (dialog, which) -> onBackPressed();

            int helpResource = R.string.text_defaultValue;
            int titleResource = R.string.text_defaultValue;

            switch (errorCode) {
                case AddDeviceHelper.ERROR_ALREADY_ADDED:
                    Toast.makeText(AddDeviceActivity.this, R.string.toast_alreadyAdded, Toast.LENGTH_LONG).show();
                    return;

                case AddDeviceHelper.ERROR_UNTRUSTED_DEVICE:
                    return;

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

    /**
     * Receives a broadcast about CommunicationService state changes and fragment change requests
     * @see #setFragment(OptionFragment)
     * @see #openCodeScanner()
     * @see AddOptionsFragment#setServiceState(boolean)
     * @see AddDeviceHelper
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_CHANGE_FRAGMENT.equals(action) && intent.hasExtra(EXTRA_TARGET_FRAGMENT)) {
                //OptionFragment is enum
                OptionFragment targetFragment = OptionFragment.valueOf(intent.getStringExtra(EXTRA_TARGET_FRAGMENT));

                switch (targetFragment){

                    case EnterDeviceId:
                        SingleTextInputDialog.OnEnteredListener listener = text -> {
                            SSDevice ssDevice = AddDeviceHelper.getEmptyDevice();
                            ssDevice.setDeviceId(text);

                            AddDeviceHelper helper = new AddDeviceHelper(AddDeviceActivity.this, ssDevice, addDeviceCallback);
                            helper.processDevice();
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
            else if (CommunicationService.SERVICE_STATE_CHANGED.equals(action)) {
                if (mAddOptionsFragment != null)
                    mAddOptionsFragment.setServiceState(intent.getBooleanExtra(CommunicationService.EXTRA_SERVICE_SATE, false));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_add);

        mAppBarLayout = findViewById(R.id.add_device_app_bar_layout);
        mToolBarLayout = findViewById(R.id.add_device_toolbar_layout);
        mToolBar = findViewById(R.id.add_device_toolbar);

        if (mToolBarLayout != null)
            mToolBarLayout.setTitle(getString(R.string.title_addDevice));
        else
            mToolBar.setTitle(R.string.title_addDevice);

        if (mAppBarLayout != null)
            mAppBarLayout.setExpanded(true, true);

        //Important to call this BEFORE setNavigationOnClickListener()
        setSupportActionBar(mToolBar);

        //To make "close" animation (this instead of using "parent activity")
        mToolBar.setNavigationOnClickListener(v -> onBackPressed());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_24dp);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        else
            Log.w(TAG, "Toolbar is not properly initialized");

        mAddOptionsFragment = new AddOptionsFragment();
        mConnectionInfoFragment = new DeviceInfoFragment();

        if (savedInstanceState != null) {
            mCurrentFragment = OptionFragment.valueOf(savedInstanceState.getString(EXTRA_SAVED_FRAGMENT));
            setFragment(mCurrentFragment);
        }
        else
            setFragment(OptionFragment.Options);

        mFilter.addAction(ACTION_CHANGE_FRAGMENT);
        mFilter.addAction(CommunicationService.SERVICE_STATE_CHANGED);

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(EXTRA_SAVED_FRAGMENT, mCurrentFragment.toString());
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
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            processQRCode(result.getContents());
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_BY_CODE_SCANNER)
            openCodeScanner();

    }

    /**
     * Opens a QR code scanner activity (ZXing default)
     */
    private void openCodeScanner() {
        //TODO: it definitely should be replaced with customized scanner (with toolbar)

        IntentIntegrator integrator = new IntentIntegrator(AddDeviceActivity.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setOrientationLocked(false);
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }

    /**
     * Handles contents of scanned QR code
     * @param qrCodeContents Contents of qr code as JSON string
     * @see AddDeviceHelper
     */
    private void processQRCode(String qrCodeContents) {
        if (qrCodeContents == null)
            return;

        /* Catches exceptions that may be caused by incorrect qr code content */
        try {
            JSONObject codeDevice = new JSONObject(qrCodeContents);

            //TODO: create custom exception type
            if (!codeDevice.has(DeviceInfoFragment.QR_CODE_DEVICE_NICKNAME))
                throw new Exception("Device nickname is missing");

            if (!codeDevice.has(DeviceInfoFragment.QR_CODE_APP_VERSION))
                throw new Exception("App version is missing");

            if (!codeDevice.has(DeviceInfoFragment.QR_CODE_DEVICE_ID))
                throw new Exception("Device ID is missing");

            /* QR code contains device id and main information, so it should be added */
            SSDevice ssDevice = AddDeviceHelper.getEmptyDevice();
            ssDevice.setDeviceId(codeDevice.getString(DeviceInfoFragment.QR_CODE_DEVICE_ID));
            ssDevice.setNickname(codeDevice.getString(DeviceInfoFragment.QR_CODE_DEVICE_NICKNAME));
            ssDevice.setAppVersion(codeDevice.getString(DeviceInfoFragment.QR_CODE_APP_VERSION));

            AddDeviceHelper helper = new AddDeviceHelper(this, ssDevice, addDeviceCallback);
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

    /**
     * Replaces current fragment with required one
     * @param targetFragment Required fragment
     */
    private void setFragment(@NonNull OptionFragment targetFragment) {

        Fragment candidate = null;

        switch (targetFragment) {
            case Options:
                candidate = mAddOptionsFragment;
                break;

            case DeviceInfo:
                candidate = mConnectionInfoFragment;
                break;
        }

        if (candidate == null) {
            Log.d(TAG, String.format("Cannot set fragment: %s is not found", targetFragment));
            return;
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

            mCurrentFragment = targetFragment;

            setToolbar(targetFragment);
        }
    }

    /**
     * Sets the title and state of toolbar depending on the required fragment
     * @param fragment Required fragment
     */
    private void setToolbar(@NonNull OptionFragment fragment) {

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

        if (getSupportActionBar() != null) {
            if (fragment.equals(OptionFragment.Options))
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_24dp);
            else
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp);
        }
        else
            Log.w(TAG, "Toolbar is not properly initialized");

        if (mToolBarLayout != null)
            mToolBarLayout.setTitle(getString(titleResource));
        else
            mToolBar.setTitle(titleResource);

        if (mAppBarLayout != null)
            mAppBarLayout.setExpanded(isOptions, true);
    }
}
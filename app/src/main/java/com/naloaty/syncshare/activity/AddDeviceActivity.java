package com.naloaty.syncshare.activity;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.SSActivity;
import com.naloaty.syncshare.dialog.EnterIpAddressDialog;
import com.naloaty.syncshare.dialog.SingleTextInputDialog;
import com.naloaty.syncshare.fragment.ConnectionInfoFragment;
import com.naloaty.syncshare.fragment.OptionFragment;
import com.naloaty.syncshare.fragment.AddOptionsFragment;
import com.naloaty.syncshare.util.AddDeviceHelper;
import com.naloaty.syncshare.util.NetworkStateMonitor;
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
    private ConnectionInfoFragment mConnectionInfoFragment ;

    private final IntentFilter mFilter = new IntentFilter();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (ACTION_CHANGE_FRAGMENT.equals(intent.getAction())
                && intent.hasExtra(EXTRA_TARGET_FRAGMENT)) {

                //OptionFragment is enum
                OptionFragment targetFragment = OptionFragment.valueOf(intent.getStringExtra(EXTRA_TARGET_FRAGMENT));

                switch (targetFragment){

                    case EnterIP:

                        SingleTextInputDialog.OnEnteredListener listener = new SingleTextInputDialog.OnEnteredListener() {
                            @Override
                            public void onEntered(String text) {
                                AddDeviceHelper.proccessDevice(text);
                            }
                        };

                        new EnterIpAddressDialog(AddDeviceActivity.this, listener).show();
                        break;

                    case ScanQR:
                        openCodeScanner();
                        break;

                    default:
                        setFragment(targetFragment);
                }


            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_pair);

        //======== Init collapsing toolbar ==========
        mAppBarLayout = findViewById(R.id.pair_device_app_bar_layout);
        mToolBarLayout = findViewById(R.id.pair_device_toolbar_layout);
        mToolBar = findViewById(R.id.pair_device_toolbar);

        mToolBarLayout.setTitle(getString(R.string.title_pairDevice));
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
        mConnectionInfoFragment = new ConnectionInfoFragment();

        setFragment(OptionFragment.Options);

        //======= Init broadcast =========
        mFilter.addAction(ACTION_CHANGE_FRAGMENT);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mReceiver);
    }

    @Override
    public void onBackPressed() {

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.pair_device_fragment_placeholder);

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
        boolean locationGranted = PermissionHelper.checkLocationPermission(this);
        boolean locationServiceEnabled = PermissionHelper.checkLocationService(this);

        if (!locationGranted){
            PermissionHelper.requestLocationPermission(this, REQUEST_LOCATION_BY_CODE_SCANNER);
            return;
        }

        if (!locationServiceEnabled) {
            PermissionHelper.requestLocationService(this);
            return;
        }

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
            JSONObject codeNetwork = new JSONObject(qrCodeContents);
            JSONObject localNetwork = NetworkStateMonitor.getCurrentWfifInfo(this);

            if (localNetwork == null) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_cannotVerify)
                        .setMessage(R.string.text_notConnected)
                        .setPositiveButton(R.string.btn_close, null)
                        .show();

                return;
            }

            if (!codeNetwork.has(NetworkStateMonitor.JSON_WIFI_BSSID))
                throw new Exception("Unrecognized code");

            if (!codeNetwork.getString(NetworkStateMonitor.JSON_WIFI_BSSID)
                    .equals(localNetwork.getString(NetworkStateMonitor.JSON_WIFI_BSSID))){

                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_differentNetwork)
                        .setMessage(R.string.text_connectToSameNetwork)
                        .setPositiveButton(R.string.btn_close, null)
                        .show();

                return;
            }

            AddDeviceHelper.proccessDevice(codeNetwork.getString(NetworkStateMonitor.JSON_WIFI_IP_ADDRESS));

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

            case ConnectionInfo:
                candidate = mConnectionInfoFragment;
                break;
        }

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.pair_device_fragment_placeholder);

        if (currentFragment == null ||  currentFragment != candidate) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (currentFragment != null)
                transaction.remove(currentFragment);

            if (currentFragment != null && candidate instanceof AddOptionsFragment)
                transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
            else
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);

            transaction.add(R.id.pair_device_fragment_placeholder, candidate);
            transaction.commit();

            setToolbar(targetFragment);
        }
    }

    private void setToolbar(OptionFragment fragment) {

        int titleResource;
        boolean isOptions = false;

        switch (fragment) {
            case Options:
                titleResource = R.string.title_pairDevice;
                isOptions = true;
                break;

            case ConnectionInfo:
                titleResource = R.string.title_connectionInfo;
                break;

            default:
                titleResource = R.string.title_pairDevice;
        }

        if (fragment.equals(OptionFragment.Options))
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_24dp);
        else
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp);

        mToolBarLayout.setTitle(getString(titleResource));
        mAppBarLayout.setExpanded(isOptions, true);
    }
}

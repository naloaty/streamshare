package com.naloaty.syncshare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.SSActivity;

/**
 * This activity represents the device manage screen, that allows users to manage their devices (deny access, remove).
 *
 * Related fragment:
 * {@link com.naloaty.syncshare.fragment.MyDevicesFragment}
 */
public class DeviceManageActivity extends SSActivity {

    private static final String TAG = "DeviceManageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_manage);
        Toolbar toolBar = findViewById(R.id.activity_device_manage_toolbar);

        //Important to call this function BEFORE setNavigationOnClickListener()
        setSupportActionBar(toolBar);

        //To make "close" animation (instead of using "parent activity")
        toolBar.setNavigationOnClickListener(v -> onBackPressed());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(R.string.title_deviceManage);
        }
        else
            Log.w(TAG, "Toolbar is not properly initialized");

        AppCompatButton addDeviceButton = findViewById(R.id.device_manage_add_device_btn);
        addDeviceButton.setOnClickListener((v) -> startActivity(new Intent(DeviceManageActivity.this, AddDeviceActivity.class)));
    }
}

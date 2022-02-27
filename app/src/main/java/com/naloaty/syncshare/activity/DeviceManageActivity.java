package com.naloaty.syncshare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.SSActivity;
import com.naloaty.syncshare.util.DeviceUtils;
import com.naloaty.syncshare.widget.LinearLayoutCollapser;
import com.naloaty.syncshare.widget.ViewRevealHelper;

/**
 * This activity represents the device manage screen, that allows users to manage their devices (deny access, remove).
 *
 * Related fragment:
 * @see com.naloaty.syncshare.fragment.MyDevicesFragment
 */
public class DeviceManageActivity extends SSActivity {

    private static final String TAG = "DeviceManageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_manage);
        Toolbar toolBar = findViewById(R.id.toolbar);

        //Important to call this function BEFORE setNavigationOnClickListener()
        setSupportActionBar(toolBar);

        //To make "close" animation (instead of using "parent activity")
        toolBar.setNavigationOnClickListener(v -> onBackPressed());

        View helpHint = findViewById(R.id.help_hint);
        TextView helpText = helpHint.findViewById(R.id.help_hint_text);
        helpText.setText(R.string.text_myDevicesHelp);

        FloatingActionButton fab = findViewById(R.id.fab);

        final LinearLayoutCollapser collapser = new LinearLayoutCollapser((LinearLayout) helpHint, getResources().getInteger(R.integer.help_expand_duration), false);

        fab.setOnClickListener((v) -> {
            collapser.toggleLayout();
        });


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

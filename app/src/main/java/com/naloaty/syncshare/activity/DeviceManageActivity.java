package com.naloaty.syncshare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.SSActivity;

public class DeviceManageActivity extends SSActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manage);

        Toolbar toolBar = findViewById(R.id.activity_device_manage_toolbar);

        //Important to call this BEFORE setNavigationOnClickListener()
        setSupportActionBar(toolBar);

        //To make "close" animation (this instead of using "parent activity")
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_24dp);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.text_deviceManageTitle);

        AppCompatButton addDeviceButton = findViewById(R.id.device_manage_add_device_btn);
        addDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DeviceManageActivity.this, PairDeviceActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.toolbar_menu_device_manage, menu);
        return true;
    }
}

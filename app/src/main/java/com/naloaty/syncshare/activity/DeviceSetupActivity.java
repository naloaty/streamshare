package com.naloaty.syncshare.activity;

import android.os.Bundle;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.SSActivity;

public class DeviceSetupActivity extends SSActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_pair);
    }
}

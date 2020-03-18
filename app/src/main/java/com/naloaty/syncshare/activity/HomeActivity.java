package com.naloaty.syncshare.activity;

import android.os.Bundle;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.Activity;

public class HomeActivity extends Activity {
    public static final int REQUEST_PERMISSION_ALL = 1;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}

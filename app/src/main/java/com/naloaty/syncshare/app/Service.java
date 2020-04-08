package com.naloaty.syncshare.app;


import android.content.SharedPreferences;

import com.naloaty.syncshare.util.AppUtils;

abstract public class Service extends android.app.Service {

    protected SharedPreferences getDefaultSharedPreferences()
    {
        return AppUtils.getDefaultSharedPreferences(getApplicationContext());
    }

}

package com.weiba.commonhybridapp;

import android.app.Application;

import com.weiba.web.sharelibrary.util.WebToolUtil;

/**
 * Created by david on 16/7/26.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    {
        WebToolUtil.setJurisdiction();
    }
}

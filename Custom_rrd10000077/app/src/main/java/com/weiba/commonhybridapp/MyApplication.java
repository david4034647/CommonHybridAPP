package com.weiba.commonhybridapp;

import com.mob.MobApplication;
import com.weiba.web.sharelibrary.util.WebToolUtil;

/**
 * Created by david on 16/7/26.
 */
public class MyApplication extends MobApplication {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    {
        WebToolUtil.setJurisdiction();
    }
}

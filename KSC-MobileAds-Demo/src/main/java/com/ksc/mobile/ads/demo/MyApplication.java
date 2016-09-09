package com.ksc.mobile.ads.demo;

import android.app.Application;

/**
 * Created by Alamusi on 2016/9/7.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }
}

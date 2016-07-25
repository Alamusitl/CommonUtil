package com.ksc.client.core;

import android.content.Context;

import com.baidu.gamesdk.BDGameApplication;

/**
 * Created by Alamusi on 2016/7/25.
 */

public class KSCApplication extends BDGameApplication {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        KSCSDK.getInstance().onApplicationAttachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        KSCSDK.getInstance().onApplicationCreate(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        KSCSDK.getInstance().onApplicationTerminate(getApplicationContext());
    }
}

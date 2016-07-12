package com.ksc.client.core;

import android.app.Application;
import android.content.Context;

/**
 * Created by Alamusi on 2016/6/21.
 */
public class KSCApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        KSCSDK.getInstance().onApplicationCreate(getApplicationContext());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        KSCSDK.getInstance().onApplicationAttachBaseContext(base);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        KSCSDK.getInstance().onApplicationTerminate(getApplicationContext());
    }
}

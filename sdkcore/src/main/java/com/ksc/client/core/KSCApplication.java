package com.ksc.client.core;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.ksc.client.toolbox.HttpRequestManager;

/**
 * Created by Alamusi on 2016/6/21.
 */
public class KSCApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HttpRequestManager.init();
        String currentProcessName = getCurrentProcessName();
        if (getPackageName().equals(currentProcessName)) {
            KSCSDK.getInstance().onApplicationCreate(getApplicationContext());
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        KSCSDK.getInstance().onApplicationAttachBaseContext(base);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        HttpRequestManager.destroy();
        KSCSDK.getInstance().onApplicationTerminate(getApplicationContext());
    }

    private String getCurrentProcessName() {
        String currentProcessName = "";
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                currentProcessName = processInfo.processName;
                break;
            }
        }
        return currentProcessName;
    }
}

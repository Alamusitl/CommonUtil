package com.ksc.client.core.base.entity;

import android.content.pm.ActivityInfo;

/**
 * Created by Alamusi on 2016/6/21.
 */
public class AppInfo {

    private String mAppId = null;// appId
    private String mAppKey = null;// appKey
    private int mScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;// 横竖屏，默认竖屏

    public String getAppId() {
        return mAppId;
    }

    public void setAppId(String appId) {
        this.mAppId = appId;
    }

    public String getAppKey() {
        return mAppKey;
    }

    public void setAppKey(String appKey) {
        this.mAppKey = appKey;
    }

    public int getScreenOrientation() {
        return mScreenOrientation;
    }

    public void setScreenOrientation(int orientation) {
        this.mScreenOrientation = orientation;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "mAppId='" + mAppId + '\'' +
                ", mAppKey='" + mAppKey + '\'' +
                ", mScreenOrientation=" + mScreenOrientation +
                '}';
    }
}

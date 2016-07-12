package com.ksc.client.core.base.entity;

import android.content.pm.ActivityInfo;

/**
 * Created by Alamusi on 2016/6/21.
 */
public class AppInfo {

    private String mAppid = null;// appid
    private String mAppkey = null;// appkey
    private int mScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;// 横竖屏，默认竖屏

    public String getAppid() {
        return mAppid;
    }

    public void setAppid(String appid) {
        this.mAppid = appid;
    }

    public String getAppkey() {
        return mAppkey;
    }

    public void setAppkey(String appkey) {
        this.mAppkey = appkey;
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
                "mAppid='" + mAppid + '\'' +
                ", mAppkey='" + mAppkey + '\'' +
                ", mScreenOrientation=" + mScreenOrientation +
                '}';
    }
}

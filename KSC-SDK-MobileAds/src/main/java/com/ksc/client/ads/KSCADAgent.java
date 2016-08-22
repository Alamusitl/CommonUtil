package com.ksc.client.ads;

import android.app.Activity;

import com.ksc.client.toolbox.HttpRequestParam;

/**
 * Created by Alamusi on 2016/8/16.
 */
public class KSCADAgent {

    public static KSCADAgent getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void init(Activity activity) {
        HttpRequestParam requestParam = new HttpRequestParam("http://120.92.9.140:8080/api/def", HttpRequestParam.METHOD_POST);
        requestParam.setContentType("application/x-protobuf");

    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void onDestroy() {

    }

    public void showAdVideo(Activity activity) {

    }

    private static class SingletonHolder {
        public static final KSCADAgent INSTANCE = new KSCADAgent();
    }

}

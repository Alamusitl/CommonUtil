package com.ksc.client.ads;

import android.app.Activity;

/**
 * Created by Alamusi on 2016/8/16.
 */
public class KSCADAgent {

    public static KSCADAgent getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void init(Activity activity) {

    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void showAdVideo() {

    }

    private static class SingletonHolder {
        public static final KSCADAgent INSTANCE = new KSCADAgent();
    }

}

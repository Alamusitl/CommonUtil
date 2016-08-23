package com.ksc.client.ads;

import android.os.Handler;

/**
 * Created by Alamusi on 2016/8/23.
 */
public class KSCBlackBoard {

    private static Handler mTransformHandler;

    public static Handler getTransformHandler() {
        return mTransformHandler;
    }

    public static void setTransformHandler(Handler handler) {
        mTransformHandler = handler;
    }
}

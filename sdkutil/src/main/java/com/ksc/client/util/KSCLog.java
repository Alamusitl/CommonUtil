package com.ksc.client.util;

import android.util.Log;

/**
 * SDK Log工具类
 * Created by Alamusi on 2016/6/22.
 */
public class KSCLog {

    private static final String TAG = KSCLog.class.getSimpleName();

    public static void d(String info) {
        Log.d(TAG, info);
    }

    public static void d(String tag, String info) {
        Log.d(tag, info);
    }

    public static void i(String info) {
        Log.i(TAG, info);
    }

    public static void i(String tag, String info) {
        Log.i(tag, info);
    }

    public static void w(String info) {
        Log.w(TAG, info);
    }

    public static void w(String tag, String info) {
        Log.i(tag, info);
    }

    public static void e(String errorInfo) {
        Log.e(TAG, errorInfo);
    }

    public static void e(String tag, String info) {
        Log.i(tag, info);
    }

    public static void e(String errorInfo, Throwable t) {
        Log.e(TAG, errorInfo, t);
    }
}

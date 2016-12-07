package com.afk.client.util;

import android.util.Log;

/**
 * SDK Log工具类
 * Created by Alamusi on 2016/6/22.
 */
public class Logger {

    private static final String TAG = Logger.class.getSimpleName();
    public static boolean mIsDebug = false;

    public static void v(String info) {
        if (mIsDebug) {
            Log.d(TAG, info);
        }
    }

    public static void v(String tag, String info) {
        if (mIsDebug) {
            Log.d(tag, info);
        }
    }

    public static void d(String info) {
        if (mIsDebug) {
            Log.d(TAG, info);
        }
    }

    public static void d(String tag, String info) {
        if (mIsDebug) {
            Log.d(tag, info);
        }
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

    public static void e(String errorInfo, Throwable t) {
        if (t == null) {
            e(errorInfo);
        } else {
            Log.e(TAG, errorInfo, t);
        }
    }

    public static void e(String errorInfo) {
        Log.e(TAG, errorInfo);
    }

    public static void e(String tag, String errorInfo, Throwable t) {
        if (t == null) {
            e(tag, errorInfo);
        } else {
            Log.e(tag, errorInfo, t);
        }
    }

    public static void e(String tag, String errorInfo) {
        Log.i(tag, errorInfo);
    }
}

package com.ksc.client.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by Alamusi on 2016/7/5.
 */
public class KSCToastUtils {

    public static boolean mIsShowToast = true;

    public static void showToast(Context context, String msg) {
        if (mIsShowToast) {
            showToast(context, msg, Toast.LENGTH_SHORT);
        }
    }

    public static void showToast(Context context, int msg) {
        if (mIsShowToast) {
            showToast(context, msg, Toast.LENGTH_SHORT);
        }
    }

    public static void showToastLong(Context context, String msg) {
        if (mIsShowToast) {
            showToast(context, msg, Toast.LENGTH_LONG);
        }
    }

    public static void showToastLong(Context context, int msg) {
        if (mIsShowToast) {
            showToast(context, msg, Toast.LENGTH_LONG);
        }
    }

    private static void showToast(final Context context, final String msg, final int duration) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, duration).show();
            }
        });
    }

    private static void showToast(final Context context, final int msg, final int duration) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, duration).show();
            }
        });
    }
}

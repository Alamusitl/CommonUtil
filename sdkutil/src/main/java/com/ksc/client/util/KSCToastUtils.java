package com.ksc.client.util;

import android.app.Activity;
import android.widget.Toast;

/**
 * Created by Alamusi on 2016/7/5.
 */
public class KSCToastUtils {

    public static void showTaost(Activity activity, String msg) {
        showToast(activity, msg, Toast.LENGTH_SHORT);
    }

    public static void showToastLong(Activity activity, String msg) {
        showToast(activity, msg, Toast.LENGTH_LONG);
    }

    public static void showToast(final Activity activity, final String msg, final int duration) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, msg, duration).show();
            }
        });
    }
}

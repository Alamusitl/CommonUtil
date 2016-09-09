package com.ksc.client.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Alamusi on 2016/9/9.
 */
public class KSCPermissionUtils {

    public static final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 1000;
    public static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1001;

    public static boolean checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity activity, String permission, int requestCode) {
        if (!checkPermission(activity, permission)) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
        }
    }

}

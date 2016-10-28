package com.afk.permission;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility to request and check System permissions for apps targeting Android M
 * Created by Alamusi on 2016/10/24.
 */

public class PermissionManager {

    static final String KEY_PERMISSION = "permissions";
    private static PermissionManager mInstance = null;
    private List<String> mUnRequestedPermissions;
    private PermissionCallBack mCallBack;

    public static PermissionManager getInstance() {
        if (mInstance == null) {
            synchronized (PermissionManager.class) {
                if (mInstance == null) {
                    mInstance = new PermissionManager();
                }
            }
        }
        return mInstance;
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        mUnRequestedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                mUnRequestedPermissions.add(permission);
            }
        }
        return mUnRequestedPermissions.isEmpty();
    }

    public void requestPermission(Context context, PermissionCallBack callBack, String... permissions) {
        mCallBack = callBack;
        if (hasPermissions(context, permissions)) {
            return;
        }
        Intent intent = new Intent(context, RequestPermissionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_PERMISSION, mUnRequestedPermissions.toArray(new String[mUnRequestedPermissions.size()]));
        context.startActivity(intent);
    }

    void onRequestPermissionResult(String[] permissions, int[] grantResults, boolean[] shouldShowRequestPermissionRationale) {
        for (int i = 0; i < permissions.length; i++) {
            mUnRequestedPermissions.remove(permissions[i]);
            if (mCallBack != null) {
                boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                mCallBack.onPermissionRequestResult(new Permission(permissions[i], granted, shouldShowRequestPermissionRationale[i]));
            }
        }
    }
}

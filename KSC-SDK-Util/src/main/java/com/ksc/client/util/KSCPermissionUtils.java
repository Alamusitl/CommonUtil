package com.ksc.client.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Alamusi on 2016/9/9.
 */
public class KSCPermissionUtils {

    protected static final int REQUEST_PERMISSION_CODE = 1000;

    private static ConcurrentHashMap<String, Boolean> mAllPrivatePermissions = new ConcurrentHashMap<>();

    /**
     * 检查权限，没有权限请求授权
     *
     * @param activity 上下文
     */
    public static void requestNeedPermission(final Activity activity) {
        KSCLog.d("request need permissions begin");
        mAllPrivatePermissions.put(Manifest.permission_group.LOCATION, true);
        mAllPrivatePermissions.put(Manifest.permission_group.STORAGE, true);
        mAllPrivatePermissions.put(Manifest.permission.READ_PHONE_STATE, true);

        List<String> needRequestPermissions = new ArrayList<>();
        List<String> shouldShowRequestPermissions = new ArrayList<>();
        if (!checkPermission(Manifest.permission_group.STORAGE)) {
            mAllPrivatePermissions.put(Manifest.permission_group.STORAGE, false);
            needRequestPermissions.add(Manifest.permission_group.STORAGE);
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission_group.STORAGE)) {
                shouldShowRequestPermissions.add("存储");
            }
        } else {
            mAllPrivatePermissions.put(Manifest.permission_group.STORAGE, true);
        }
        if (!checkPermission(Manifest.permission_group.LOCATION)) {
            mAllPrivatePermissions.put(Manifest.permission_group.LOCATION, false);
            needRequestPermissions.add(Manifest.permission_group.LOCATION);
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission_group.LOCATION)) {
                shouldShowRequestPermissions.add("定位");
            }
        } else {
            mAllPrivatePermissions.put(Manifest.permission_group.LOCATION, true);
        }
        if (!checkPermission(Manifest.permission.READ_PHONE_STATE)) {
            mAllPrivatePermissions.put(Manifest.permission.READ_PHONE_STATE, false);
            needRequestPermissions.add(Manifest.permission.READ_PHONE_STATE);
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE)) {
                shouldShowRequestPermissions.add("手机状态");
            }
        } else {
            mAllPrivatePermissions.put(Manifest.permission.READ_PHONE_STATE, true);
        }
        if (needRequestPermissions.size() > 0) {
            if (shouldShowRequestPermissions.size() > 0) {
                StringBuilder builder = new StringBuilder();
                builder.append("您需要授权权限").append(shouldShowRequestPermissions.get(0));
                for (int i = 1; i < shouldShowRequestPermissions.size(); i++) {
                    builder.append(", ").append(shouldShowRequestPermissions.get(i));
                }
                final String msg = builder.toString();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            ActivityCompat.requestPermissions(activity, needRequestPermissions.toArray(new String[needRequestPermissions.size()]), REQUEST_PERMISSION_CODE);
        }
        KSCLog.d("request need permissions end");
        for (String permission : mAllPrivatePermissions.keySet()) {
            KSCLog.d("permission " + permission + " status= " + mAllPrivatePermissions.get(permission));
        }
    }

    /**
     * 接受授权信息
     *
     * @param requestCode  请求码
     * @param permissions  权限数组
     * @param grantResults 授权信息数组
     */
    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        mAllPrivatePermissions.put(permissions[i], true);
                    } else {
                        mAllPrivatePermissions.put(permissions[i], false);
                    }
                    KSCLog.d("onRequestPermissionsResult " + permissions[i] + " status [" + mAllPrivatePermissions.get(permissions[i]));
                }
                break;
        }
    }

    /**
     * 检查是否有权限
     *
     * @param permission 权限或权限组
     * @return 如果有权限返回true，没有权限返回false
     */
    public static boolean checkPermission(String permission) {
        return mAllPrivatePermissions.get(permission);
    }

    public static void requestPermission(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    /**
     * 检查并授权权限
     * 当没有权限的时候申请权限，返回false
     * 当有权限时返回true
     *
     * @param activity    上下文
     * @param permission  需要授权的权限集合
     * @param requestCode 请求码
     * @return 是否有权限
     */
    public static boolean checkRequestPermission(final Activity activity, String permission, int requestCode) {
        if (!checkPermission(permission)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "您已禁止权限，请重新开启", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
            return false;
        } else {
            return true;
        }
    }

}

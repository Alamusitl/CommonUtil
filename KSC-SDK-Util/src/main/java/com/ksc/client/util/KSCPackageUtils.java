package com.ksc.client.util;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;

/**
 * Created by Alamusi on 2016/8/4.
 */
public class KSCPackageUtils {

    /**
     * 获得包名
     *
     * @param context
     * @return
     */
    public static String getPackageName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            KSCLog.e("can not found package " + context.getPackageName(), e);
        }
        return null;
    }

    /**
     * 获得包VersionCode
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            KSCLog.e("can not found package " + context.getPackageName(), e);
        }
        return 0;
    }

    /**
     * 获得包VersionName
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            KSCLog.e("can not found package " + context.getPackageName(), e);
        }
        return null;
    }

    /**
     * 判断Manifest文件里面是否存在Activity声明
     *
     * @param context
     * @param activityName
     * @return
     */
    public static boolean checkActivityExist(Context context, String activityName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            for (int i = 0; i < packageInfo.activities.length; i++) {
                ActivityInfo info = packageInfo.activities[i];
                if (info.name.equals(activityName)) {
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            KSCLog.e("can not found package " + context.getPackageName(), e);
        }
        return false;
    }

    /**
     * 判断Manifest文件里面是否存在Service声明
     *
     * @param context
     * @param serviceName
     * @return
     */
    public static boolean checkServiceExist(Context context, String serviceName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            for (int i = 0; i < packageInfo.services.length; i++) {
                ServiceInfo info = packageInfo.services[i];
                if (info.name.equals(serviceName)) {
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            KSCLog.e("can not found package " + context.getPackageName(), e);
        }
        return false;
    }

    /**
     * 判断Manifest文件里面是否存在Receiver声明
     *
     * @param context
     * @param receiversName
     * @return
     */
    public static boolean checkReceiversExist(Context context, String receiversName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            for (int i = 0; i < packageInfo.receivers.length; i++) {
                ActivityInfo info = packageInfo.receivers[i];
                if (info.name.equals(receiversName)) {
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            KSCLog.e("can not found package " + context.getPackageName(), e);
        }
        return false;
    }

    /**
     * 判断Manifest文件里面是否存在Provider声明
     *
     * @param context
     * @param providersName
     * @return
     */
    public static boolean checkProvidersExist(Context context, String providersName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            for (int i = 0; i < packageInfo.providers.length; i++) {
                ProviderInfo info = packageInfo.providers[i];
                if (info.name.equals(providersName)) {
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            KSCLog.e("can not found package " + context.getPackageName(), e);
        }
        return false;
    }

    /**
     * 判断Manifest文件里面是否存在Permission声明
     *
     * @param context
     * @param permissionName
     * @return
     */
    public static boolean checkPermissionExist(Context context, String permissionName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            for (int i = 0; i < packageInfo.permissions.length; i++) {
                PermissionInfo info = packageInfo.permissions[i];
                if (info.name.equals(permissionName)) {
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            KSCLog.e("can not found package " + context.getPackageName(), e);
        }
        return false;
    }
}

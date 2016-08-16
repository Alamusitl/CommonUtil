package com.ksc.client.util;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.util.Locale;
import java.util.UUID;

/**
 * Created by Alamusi on 2016/6/22.
 */
public class KSCDeviceUtils {

    /**
     * 获得设备ID
     *
     * @param context
     * @return
     */
    public static String getDeviceID(Context context) {
        String imei = getImei(context);
        if (!TextUtils.isEmpty(imei)) {
            return imei;
        }
        String mac = getMac(context);
        if (!TextUtils.isEmpty(mac)) {
            return mac;
        }
        return getUUID(context);
    }

    /**
     * 获得Android 设备IMEI
     *
     * @param context
     * @return
     */
    public static String getImei(Context context) {
        try {
            TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephonyMgr != null) {
                String imei = mTelephonyMgr.getDeviceId();
                if ("012345678912345".equals(imei)) {
                    return null;
                }
                if ("000000000000000".equals(imei)) {
                    return null;
                }
                if (!TextUtils.isEmpty(imei)) {
                    return "imei_" + imei;
                }
            }
        } catch (SecurityException e) {
            KSCLog.e(e.getMessage());
        }
        return null;
    }

    /**
     * 获得Android 设备Mac地址
     *
     * @param context
     * @return
     */
    public static String getMac(Context context) {
        try {
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifi != null) {
                WifiInfo info = wifi.getConnectionInfo();
                if (info != null && !TextUtils.isEmpty(info.getMacAddress())) {
                    return "mac_" + info.getMacAddress();
                }
            }
        } catch (SecurityException e) {
            KSCLog.e(e.getMessage());
        }
        return null;
    }

    /**
     * 获得一个不变的随机变量
     *
     * @param context
     * @return
     */
    public static String getUUID(Context context) {
        String uuid = KSCPreferencesUtils.getUUID(context);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            KSCPreferencesUtils.setUUID(context, uuid);
        }
        return uuid;
    }

    /**
     * 获得手机屏幕宽高
     *
     * @param activity
     * @return
     */
    public static String getScreenSize(Activity activity) {
        DisplayMetrics outMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels + "*" + outMetrics.heightPixels;
    }

    /**
     * 判断当前的设备是不是模拟器
     *
     * @return
     */
    public static boolean isInEmulator() {
        if (!Build.HARDWARE.equals("goldfish")) {
            return false;
        }

        if (!Build.BRAND.startsWith("generic")) {
            return false;
        }

        if (!Build.DEVICE.startsWith("generic")) {
            return false;
        }

        if (!Build.PRODUCT.contains("sdk")) {
            return false;
        }

        return Build.MODEL.toLowerCase(Locale.US).contains("sdk");

    }
}

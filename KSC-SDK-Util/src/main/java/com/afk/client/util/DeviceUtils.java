package com.afk.client.util;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.Locale;
import java.util.UUID;

/**
 * Created by Alamusi on 2016/6/22.
 */
public class DeviceUtils {

    /**
     * 获得设备ID, 优先级：ANDROID_ID -> IMEI -> UUID
     *
     * @param context 上下文
     * @return Android Id
     */
    public static String getAndroidID(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (!"9774d56d682e549c".equals(androidId)) {
            return androidId;
        }
        String imei = getImei(context);
        if (!TextUtils.isEmpty(imei)) {
            return imei;
        }
        return getUUID(context);
    }

    /**
     * 获得Android 设备IMEI
     *
     * @param context 上下文
     * @return IMEI
     */
    public static String getImei(Context context) {
        try {
            TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephonyMgr != null) {
                String imei = mTelephonyMgr.getDeviceId();
                if ("012345678912345".equals(imei)) {
                    return "";
                }
                if ("000000000000000".equals(imei)) {
                    return "";
                }
                if (!TextUtils.isEmpty(imei)) {
                    return imei;
                }
            }
        } catch (SecurityException e) {
            Logger.e(e.getMessage());
        }
        return "";
    }

    /**
     * 获得一个不变的随机变量
     *
     * @param context 上下文
     * @return
     */
    public static String getUUID(Context context) {
        String uuid = (String) PreferencesUtils.get(context, "UUID", "");
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            PreferencesUtils.put(context, "UUID", uuid);
        }
        return uuid;
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

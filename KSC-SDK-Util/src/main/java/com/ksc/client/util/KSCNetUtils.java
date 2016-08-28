package com.ksc.client.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Created by Alamusi on 2016/6/23.
 */
public class KSCNetUtils {

    /**
     * 获得当前网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED;
    }

    /**
     * 获得当前活跃网络的类型，当WIFI时返回wifi, 手机移动网络是返回2G/3G/4G, 其他情况下返回系统返回的网络类型名
     *
     * @param context
     * @return
     */
    public static String getNetType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return null;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || networkInfo.getState() != NetworkInfo.State.CONNECTED) {
            return null;
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return "WIFI";
        }
        // Mobile network
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = telephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
        }
        return networkInfo.getTypeName();
    }

    /**
     * 获得IPV4地址
     *
     * @return IPV4地址
     */
    public static String getIp() {
        String ip = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface networkInterface = en.nextElement();
                for (Enumeration<InetAddress> ipAddress = networkInterface.getInetAddresses(); ipAddress.hasMoreElements(); ) {
                    InetAddress inetAddress = ipAddress.nextElement();
                    // ipv4地址
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        ip = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            KSCLog.e(ex.getMessage());
        }
        return ip;
    }

    /**
     * 获得Android 设备Mac地址
     *
     * @param context 上下文
     * @return Mac地址
     */
    public static String getMac(Context context) {
        String mac = "";
        try {
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifi != null) {
                WifiInfo info = wifi.getConnectionInfo();
                if (info != null && !TextUtils.isEmpty(info.getMacAddress())) {
                    mac = info.getMacAddress();
                }
            }
        } catch (SecurityException e) {
            KSCLog.e(e.getMessage());
        }
        return mac;
    }


}

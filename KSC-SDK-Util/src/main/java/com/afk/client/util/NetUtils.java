package com.afk.client.util;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;

import com.afk.permission.PermissionManager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * 网络辅助类
 * Created by Alamusi on 2016/6/23.
 */
public class NetUtils {

    public static final int NETWORK_TYPE_INVALID = 0;
    public static final int NETWORK_TYPE_2G = 1;
    public static final int NETWORK_TYPE_3G = 2;
    public static final int NETWORK_TYPE_4G = 3;
    public static final int NETWORK_TYPE_WIFI = 4;
    public static final int NETWORK_TYPE_ETHERNET = 5;
    public static final int NETWORK_TYPE_BLUETOOTH = 6;
    public static final int NETWORK_TYPE_UNKNOWN = 7;

    /**
     * 获得当前网络是否可用
     *
     * @param context 上下文
     * @return 网络是否可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * 获得当前活跃网络的类型
     *
     * @param context 上下文
     * @return 网络类型
     */
    public static int getNetType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return NETWORK_TYPE_INVALID;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            return NETWORK_TYPE_INVALID;
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return NETWORK_TYPE_WIFI;
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
            return NETWORK_TYPE_ETHERNET;
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_BLUETOOTH) {
            return NETWORK_TYPE_BLUETOOTH;
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            // Mobile network
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int networkType = telephonyManager.getNetworkType();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return NETWORK_TYPE_2G;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return NETWORK_TYPE_3G;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return NETWORK_TYPE_4G;
                default:
                    if (networkInfo.getSubtypeName().equalsIgnoreCase("TD-SCDMA") || networkInfo.getSubtypeName().equalsIgnoreCase("WCDMA") || networkInfo.getSubtypeName().equalsIgnoreCase("CDMA2000")) {
                        return NETWORK_TYPE_3G;
                    } else {
                        return NETWORK_TYPE_UNKNOWN;
                    }
            }
        }
        return NETWORK_TYPE_UNKNOWN;
    }

    /**
     * 获得运营商
     *
     * @param context 上下文
     * @return 0.未知, 1.中国移动, 2.中国联通, 3.中国电信
     */
    public static int getOperators(Context context) {
        int operatorsName = 0;
        String imsi = "";
        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (manager == null) {
                return operatorsName;
            }
            if (PermissionManager.getInstance().hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                imsi = manager.getSubscriberId();
            } else {
                PermissionManager.getInstance().requestPermission(context, null, Manifest.permission.READ_PHONE_STATE);
            }
        } catch (Exception e) {
            Logger.e("get Operators exception", e);
        }
        if (imsi == null) {
            return operatorsName;
        }
        if (imsi.startsWith("46000") || imsi.startsWith("46002") || imsi.startsWith("46007")) {
            operatorsName = 1;
        } else if (imsi.startsWith("46001") || imsi.startsWith("46006")) {
            operatorsName = 2;
        } else if (imsi.startsWith("46003") || imsi.startsWith("46005")) {
            operatorsName = 3;
        }
        return operatorsName;
    }

    /**
     * 获得基站ID
     *
     * @param context 上下文
     * @return 基站ID
     */
    public static int getCellId(Activity context) {
        int cellId = 0;
        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (manager == null) {
                return cellId;
            }
            if (PermissionManager.getInstance().hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                String imsi = manager.getSubscriberId();
                if (imsi == null || imsi.equals("")) {
                    return cellId;
                }
                if (imsi.startsWith("46003") || imsi.startsWith("46005")) {
                    CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) manager.getCellLocation();
                    if (cdmaCellLocation != null) {
                        cellId = cdmaCellLocation.getBaseStationId();
                    }
                } else {
                    GsmCellLocation gsmCellLocation = (GsmCellLocation) manager.getCellLocation();
                    if (gsmCellLocation != null) {
                        cellId = gsmCellLocation.getCid();
                    }
                }
            } else {
                PermissionManager.getInstance().requestPermission(context, null, Manifest.permission.ACCESS_FINE_LOCATION);
            }
        } catch (Exception e) {
            Logger.e("get cell id exception", e);
        }
        return cellId;
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
                if (networkInterface == null) {
                    continue;
                }
                for (Enumeration<InetAddress> ipAddress = networkInterface.getInetAddresses(); ipAddress.hasMoreElements(); ) {
                    InetAddress inetAddress = ipAddress.nextElement();
                    if (inetAddress == null) {
                        continue;
                    }
                    // ipv4地址
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        ip = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            Logger.e(ex.getMessage() == null ? "" : ex.getMessage(), ex);
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
        } catch (SecurityException ex) {
            Logger.e(ex.getMessage() == null ? "" : ex.getMessage(), ex);
        }
        return mac;
    }

    /**
     * 获得信号强度
     *
     * @param context 上下文
     * @return 信号强度
     */
    public static int getRssi(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (manager == null) {
            return 0;
        }
        WifiInfo wifiInfo = manager.getConnectionInfo();
        if (wifiInfo == null) {
            return 0;
        }
        return wifiInfo.getRssi();
    }

    /**
     * 获得链接WiFi热点名称
     *
     * @param context 上下文
     * @return 热点名称
     */
    public static String getWifiName(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (manager == null) {
            return "";
        }
        WifiInfo wifiInfo = manager.getConnectionInfo();
        if (wifiInfo == null) {
            return "";
        }
        return wifiInfo.getSSID();
    }

    /**
     * 判断当前链接的网络是否是WIFI
     *
     * @param context 上下文
     * @return true为是WIFI，false为不是WIFI
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 打开网络设置界面
     *
     * @param activity
     */
    public static void openNetSettings(Activity activity) {
        Intent intent;
        if (android.os.Build.VERSION.SDK_INT > 10) {
            intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
        } else {
            intent = new Intent();
            ComponentName component = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
            intent.setComponent(component);
            intent.setAction("android.intent.action.VIEW");
        }
        activity.startActivity(intent);
    }

}

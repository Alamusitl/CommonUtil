package com.ksc.client.ads.proto;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;

import com.google.protobuf.ByteString;
import com.ksc.client.util.KSCDeviceUtils;
import com.ksc.client.util.KSCLocationUtils;
import com.ksc.client.util.KSCLog;
import com.ksc.client.util.KSCNetUtils;
import com.ksc.client.util.KSCPackageUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Alamusi on 2016/8/22.
 */
public class KSCMobileAdProtoAPI {

    public static KSCMobileAdProtoAPI getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 获得请求广告的请求
     *
     * @param activity
     * @param appId
     * @param channelId
     * @return 请求
     */
    public KSCMobileAdsProto530.MobadsRequest getRequest(Activity activity, String appId, String channelId,
                                                         String adSlot_id) {
        KSCMobileAdsProto530.MobadsRequest.Builder requestBuilder = KSCMobileAdsProto530.MobadsRequest.newBuilder();
        requestBuilder.setRequestId(appId + adSlot_id + System.currentTimeMillis());
        requestBuilder.setApiVersion(getApiVersion());
        requestBuilder.setAdslot(getAdSlot(adSlot_id));
        requestBuilder.setApp(getAppInfo(activity, appId, channelId));
        requestBuilder.setDevice(getDeviceInfo(activity));
        requestBuilder.setNetwork(getNetwork(activity));
        requestBuilder.setGps(getGps(activity));
        requestBuilder.setIsDebug(false);
        return requestBuilder.build();
    }

    /**
     * 返回API版本号信息
     *
     * @return API版本号信息
     */
    private KSCMobileAdsProto530.Version getApiVersion() {
        KSCMobileAdsProto530.Version.Builder versionBuilder = KSCMobileAdsProto530.Version.newBuilder();
        versionBuilder.setMajor(5);
        versionBuilder.setMinor(3);
        versionBuilder.setMicro(0);
        return versionBuilder.build();
    }

    /**
     * 获得广告位信息
     *
     * @param adSlot_id
     * @param width
     * @param height
     * @return 广告位信息
     */
    private KSCMobileAdsProto530.AdSlot getAdSlot(String adSlot_id, int width, int height) {
        KSCMobileAdsProto530.AdSlot.Builder adSlotBuilder = KSCMobileAdsProto530.AdSlot.newBuilder();
        adSlotBuilder.setAdslotId(adSlot_id);
        adSlotBuilder.setAdslotSize(getAdSlotSize(width, height));
        adSlotBuilder.setVideo(getVideo(title, contentLength, copyRight));
        adSlotBuilder.setAdslotType(-8);// 广告位类型
        adSlotBuilder.setAds(1);// 返回广告数量
        return adSlotBuilder.build();
    }

    /**
     * 获得广告位尺寸
     *
     * @param width  宽度
     * @param height 高度
     * @return 广告位尺寸
     */
    private KSCMobileAdsProto530.Size getAdSlotSize(int width, int height) {
        KSCMobileAdsProto530.Size.Builder sizeBuilder = KSCMobileAdsProto530.Size.newBuilder();
        sizeBuilder.setWidth(width);
        sizeBuilder.setHeight(height);
        return sizeBuilder.build();
    }

    /**
     * 获得广告视频信息
     *
     * @param title         视频Title
     * @param contentLength 视频长度
     * @param copyRight     版权信息
     * @return 视频信息
     */
    private KSCMobileAdsProto530.Video getVideo(String title, int contentLength, KSCMobileAdsProto530.Video.CopyRight copyRight) {
        KSCMobileAdsProto530.Video.Builder videoBuilder = KSCMobileAdsProto530.Video.newBuilder();
        videoBuilder.setTitle(ByteString.copyFromUtf8(title));
        videoBuilder.setContentLength(contentLength);
        videoBuilder.setCopyright(copyRight);
        return videoBuilder.build();
    }

    /**
     * 获得应用信息
     *
     * @param context   上下文
     * @param appId     应用ID，在Mobile SSP 注册
     * @param channelId 渠道ID
     * @return 应用信息
     */
    private KSCMobileAdsProto530.App getAppInfo(Context context, String appId, String channelId) {
        KSCMobileAdsProto530.App.Builder appBuilder = KSCMobileAdsProto530.App.newBuilder();
        appBuilder.setAppId(appId);
        appBuilder.setChannelId(channelId);
        appBuilder.setAppVersion(getAppVersion(context));
        appBuilder.setAppPackage(KSCPackageUtils.getPackageName(context));
        return appBuilder.build();
    }

    /**
     * 获得APP版本信息
     *
     * @param context 上下文
     * @return APP版本信息
     */
    private KSCMobileAdsProto530.Version getAppVersion(Context context) {
        KSCMobileAdsProto530.Version.Builder versionBuilder = KSCMobileAdsProto530.Version.newBuilder();
        String versionName = KSCPackageUtils.getVersionName(context);
        if (versionName == null || versionName.equals("")) {
            return versionBuilder.build();
        }
        String[] appVersion = versionName.split(".");
        for (int i = 0; i < appVersion.length; i++) {
            if (i == 0) {
                versionBuilder.setMajor(Integer.valueOf(appVersion[i]));
            } else if (i == 1) {
                versionBuilder.setMinor(Integer.valueOf(appVersion[i]));
            } else if (i == 2) {
                versionBuilder.setMicro(Integer.valueOf(appVersion[i]));
            }
        }
        return versionBuilder.build();
    }

    /**
     * 获得屏幕宽高信息
     *
     * @param activity 上下文
     * @return 屏幕信息
     */
    private KSCMobileAdsProto530.Size getScreenSize(Activity activity) {
        DisplayMetrics matrix = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(matrix);
        KSCMobileAdsProto530.Size.Builder sizeBuilder = KSCMobileAdsProto530.Size.newBuilder();
        sizeBuilder.setWidth(matrix.widthPixels);
        sizeBuilder.setHeight(matrix.heightPixels);
        return sizeBuilder.build();
    }

    /**
     * 获得设备信息
     *
     * @param activity 上下文
     * @return 设备信息
     */
    private KSCMobileAdsProto530.Device getDeviceInfo(Activity activity) {
        KSCMobileAdsProto530.Device.Builder deviceBuilder = KSCMobileAdsProto530.Device.newBuilder();
        deviceBuilder.setDeviceType(getDeviceType(activity));
        deviceBuilder.setOsType(KSCMobileAdsProto530.Device.OsType.ANDROID);
        deviceBuilder.setOsVersion(getOsVersion());
        try {
            deviceBuilder.setVendor(ByteString.copyFromUtf8(URLEncoder.encode(Build.MANUFACTURER, "UTF-8")));
            deviceBuilder.setModel(ByteString.copyFromUtf8(URLEncoder.encode(Build.MODEL, "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            KSCLog.e("URLEncode MANUFACTURER or MODEL exception: " + e.getMessage());
        }
        deviceBuilder.setUdid(getUdId(activity));
        deviceBuilder.setScreenSize(getScreenSize(activity));
        return null;
    }

    /**
     * 获得手机操作系统的版本信息
     *
     * @return 操作系统版本信息
     */
    private KSCMobileAdsProto530.Version getOsVersion() {
        KSCMobileAdsProto530.Version.Builder versionBuilder = KSCMobileAdsProto530.Version.newBuilder();
        String[] systemOsVersion = Build.VERSION.RELEASE.split(".");
        for (int i = 0; i < systemOsVersion.length; i++) {
            if (i == 0) {
                versionBuilder.setMajor(Integer.valueOf(systemOsVersion[i]));
            } else if (i == 1) {
                versionBuilder.setMinor(Integer.valueOf(systemOsVersion[i]));
            } else if (i == 2) {
                versionBuilder.setMicro(Integer.valueOf(systemOsVersion[i]));
            }
        }
        return versionBuilder.build();
    }

    /**
     * 获得设备类型
     *
     * @param activity 上下文
     * @return 设备类型
     */
    private KSCMobileAdsProto530.Device.DeviceType getDeviceType(Activity activity) {
        KSCMobileAdsProto530.Device.DeviceType deviceType;
        if ((activity.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
            deviceType = KSCMobileAdsProto530.Device.DeviceType.TABLET;
        } else {
            deviceType = KSCMobileAdsProto530.Device.DeviceType.PHONE;
        }
        return deviceType;
    }

    /**
     * 获得用户唯一标识 信息
     *
     * @param activity 上下文
     * @return 唯一标识信息
     */
    private KSCMobileAdsProto530.UdId getUdId(Activity activity) {
        KSCMobileAdsProto530.UdId.Builder udIdBuilder = KSCMobileAdsProto530.UdId.newBuilder();
        udIdBuilder.setImei(KSCDeviceUtils.getImei(activity));
        udIdBuilder.setMac("");
        udIdBuilder.setAndroidId("");
        udIdBuilder.setImeiMd5("");
        udIdBuilder.setAndroididMd5("");
        return udIdBuilder.build();
    }

    /**
     * 获得网络环境信息
     *
     * @return 网络环境信息
     */
    private KSCMobileAdsProto530.Network getNetwork(Context context) {
        KSCMobileAdsProto530.Network.Builder networkBuilder = KSCMobileAdsProto530.Network.newBuilder();
        networkBuilder.setIpv4(KSCNetUtils.getIp());
        networkBuilder.setConnectionType();
        networkBuilder.setOperatorType();
        networkBuilder.setCellularId();
        networkBuilder.setWifiAps(0, getWifiAp(context));
        return networkBuilder.build();
    }

    /**
     * 获得WIFI热点信息
     *
     * @return WIFI热点信息
     */
    private KSCMobileAdsProto530.WiFiAp getWifiAp(Context context) {
        KSCMobileAdsProto530.WiFiAp.Builder wifiBuilder = KSCMobileAdsProto530.WiFiAp.newBuilder();
        wifiBuilder.setApMac(KSCNetUtils.getMac(context));
        wifiBuilder.setRssi();
        wifiBuilder.setApName();
        wifiBuilder.setIsConnected();
        return wifiBuilder.build();
    }

    /**
     * 获得GPS信息
     *
     * @return GPS信息
     */
    private KSCMobileAdsProto530.Gps getGps(Context context) {
        KSCMobileAdsProto530.Gps.Builder gpsBuilder = KSCMobileAdsProto530.Gps.newBuilder();
        gpsBuilder.setCoordinateType(KSCMobileAdsProto530.Gps.CoordinateType.WGS84);
        gpsBuilder.setLongitude(KSCLocationUtils.getLongitude(context));
        gpsBuilder.setLatitude(KSCLocationUtils.getLatitude(context));
        gpsBuilder.setTimestamp((int) (System.currentTimeMillis() / 1000));
        return gpsBuilder.build();
    }

    private static class SingletonHolder {
        public static final KSCMobileAdProtoAPI INSTANCE = new KSCMobileAdProtoAPI();
    }

}

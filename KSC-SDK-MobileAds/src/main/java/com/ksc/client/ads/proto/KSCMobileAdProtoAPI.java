package com.ksc.client.ads.proto;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;

import com.google.protobuf.ByteString;
import com.ksc.client.util.KSCDeviceUtils;
import com.ksc.client.util.KSCLog;
import com.ksc.client.util.KSCPackageUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Alamusi on 2016/8/22.
 */
public class KSCMobileAdProtoAPI {

    private String adSlot_id = "";

    public static KSCMobileAdProtoAPI getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public KSCMobileAdsProto530.MobadsRequest getRequest(Context context, String appId, String channelId) {
        KSCMobileAdsProto530.MobadsRequest.Builder requestBuilder = KSCMobileAdsProto530.MobadsRequest.newBuilder();
        requestBuilder.setRequestId("");
        requestBuilder.setApiVersion(getApiVersion());
        requestBuilder.setAdslot(getAdSlot(adSlot_id));
        requestBuilder.setApp(getAppInfo(context, appId, channelId));

        return requestBuilder.build();
    }

    public KSCMobileAdsProto530.Version getApiVersion() {
        KSCMobileAdsProto530.Version.Builder versionBuilder = KSCMobileAdsProto530.Version.newBuilder();
        versionBuilder.setMajor(5);
        versionBuilder.setMinor(3);
        versionBuilder.setMicro(0);
        return versionBuilder.build();
    }

    private KSCMobileAdsProto530.App getAppInfo(Context context, String appId, String channelId) {
        KSCMobileAdsProto530.App.Builder appBuilder = KSCMobileAdsProto530.App.newBuilder();
        appBuilder.setAppId(appId);
        appBuilder.setChannelId(channelId);
        appBuilder.setAppVersion(getAppVersion(context));
        appBuilder.setAppPackage(KSCPackageUtils.getPackageName(context));
        return appBuilder.build();
    }

    public KSCMobileAdsProto530.AdSlot getAdSlot(String adSlot_id) {
        KSCMobileAdsProto530.AdSlot.Builder adSlotBuilder = KSCMobileAdsProto530.AdSlot.newBuilder();
        adSlotBuilder.setAdslotId(adSlot_id);
        return adSlotBuilder.build();
    }

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

    public KSCMobileAdsProto530.Size getSize(Activity activity) {
        DisplayMetrics matrix = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(matrix);
        KSCMobileAdsProto530.Size.Builder sizeBuilder = KSCMobileAdsProto530.Size.newBuilder();
        sizeBuilder.setWidth(matrix.widthPixels);
        sizeBuilder.setHeight(matrix.heightPixels);
        return sizeBuilder.build();
    }

    public KSCMobileAdsProto530.Device getDeviceInfo(Activity activity) {
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
        deviceBuilder.setScreenSize(getSize(activity));
        return null;
    }

    public KSCMobileAdsProto530.Version getOsVersion() {
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

    public KSCMobileAdsProto530.Device.DeviceType getDeviceType(Activity activity) {
        KSCMobileAdsProto530.Device.DeviceType deviceType;
        if ((activity.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
            deviceType = KSCMobileAdsProto530.Device.DeviceType.TABLET;
        } else {
            deviceType = KSCMobileAdsProto530.Device.DeviceType.PHONE;
        }
        return deviceType;
    }

    public KSCMobileAdsProto530.UdId getUdId(Activity activity) {
        KSCMobileAdsProto530.UdId.Builder udIdBuilder = KSCMobileAdsProto530.UdId.newBuilder();
        udIdBuilder.setImei(KSCDeviceUtils.getImei(activity));
        udIdBuilder.setMac("");
        udIdBuilder.setAndroidId("");
        udIdBuilder.setImeiMd5("");
        udIdBuilder.setAndroididMd5("");
        return udIdBuilder.build();
    }

    private static class SingletonHolder {
        public static final KSCMobileAdProtoAPI INSTANCE = new KSCMobileAdProtoAPI();
    }

}

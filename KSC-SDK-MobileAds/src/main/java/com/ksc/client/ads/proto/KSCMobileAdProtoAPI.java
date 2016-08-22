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

    public static KSCMobileAdProtoAPI getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public KSCMobileAdsProto521.MobadsRequest getRequest(Context context, String appId, String channelId) {
        KSCMobileAdsProto521.MobadsRequest.Builder requestBuilder = KSCMobileAdsProto521.MobadsRequest.newBuilder();
        requestBuilder.setApiVersion(getApiVersion());
        requestBuilder.setAdslot(getAdSlot());
        requestBuilder.setApp(getAppInfo(context, appId, channelId));

        return requestBuilder.build();
    }

    private KSCMobileAdsProto521.App getAppInfo(Context context, String appId, String channelId) {
        KSCMobileAdsProto521.App.Builder appBuilder = KSCMobileAdsProto521.App.newBuilder();
        appBuilder.setAppId(appId);
        appBuilder.setChannelId(channelId);
        appBuilder.setAppVersion(getAppVersion(context));
        appBuilder.setAppPackage(KSCPackageUtils.getPackageName(context));
        return appBuilder.build();
    }

    private KSCMobileAdsProto521.Version getAppVersion(Context context) {
        KSCMobileAdsProto521.Version.Builder versionBuilder = KSCMobileAdsProto521.Version.newBuilder();
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

    public KSCMobileAdsProto521.Version getApiVersion() {
        KSCMobileAdsProto521.Version.Builder versionBuilder = KSCMobileAdsProto521.Version.newBuilder();
        versionBuilder.setMajor(5);
        versionBuilder.setMinor(1);
        versionBuilder.setMicro(2);
        return versionBuilder.build();
    }

    public KSCMobileAdsProto521.AdSlot getAdSlot() {
        KSCMobileAdsProto521.AdSlot.Builder adSlotBuilder = KSCMobileAdsProto521.AdSlot.newBuilder();
        adSlotBuilder.setAdslotId("");
        return adSlotBuilder.build();
    }

    public KSCMobileAdsProto521.Size getSize(Activity activity) {
        DisplayMetrics matrix = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(matrix);
        KSCMobileAdsProto521.Size.Builder sizeBuilder = KSCMobileAdsProto521.Size.newBuilder();
        sizeBuilder.setWidth(matrix.widthPixels);
        sizeBuilder.setHeight(matrix.heightPixels);
        return sizeBuilder.build();
    }

    public KSCMobileAdsProto521.Device getDeviceInfo(Activity activity) {
        KSCMobileAdsProto521.Device.Builder deviceBuilder = KSCMobileAdsProto521.Device.newBuilder();
        deviceBuilder.setDeviceType(getDeviceType(activity));
        deviceBuilder.setOsType(KSCMobileAdsProto521.Device.OsType.ANDROID);
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

    public KSCMobileAdsProto521.Version getOsVersion() {
        KSCMobileAdsProto521.Version.Builder versionBuilder = KSCMobileAdsProto521.Version.newBuilder();
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

    public KSCMobileAdsProto521.Device.DeviceType getDeviceType(Activity activity) {
        KSCMobileAdsProto521.Device.DeviceType deviceType;
        if ((activity.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
            deviceType = KSCMobileAdsProto521.Device.DeviceType.TABLET;
        } else {
            deviceType = KSCMobileAdsProto521.Device.DeviceType.PHONE;
        }
        return deviceType;
    }

    public KSCMobileAdsProto521.UdId getUdId(Activity activity) {
        KSCMobileAdsProto521.UdId.Builder udIdBuilder = KSCMobileAdsProto521.UdId.newBuilder();
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

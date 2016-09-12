package com.ksc.client.ads.proto;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.ksc.client.ads.bean.KSCVideoAdBean;
import com.ksc.client.util.KSCDeviceUtils;
import com.ksc.client.util.KSCLocationUtils;
import com.ksc.client.util.KSCLog;
import com.ksc.client.util.KSCMD5Utils;
import com.ksc.client.util.KSCNetUtils;
import com.ksc.client.util.KSCPackageUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alamusi on 2016/8/22.
 */
public class KSCMobileAdProtoAPI {

    private long mErrorCode;
    private String mRequestId;

    public static KSCMobileAdProtoAPI getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 获得请求广告的请求
     *
     * @param activity  上下文
     * @param appId     AppId
     * @param adSlot_id 广告位ID
     * @return 请求
     */
    public KSCMobileAdsProto530.MobadsRequest getRequest(Activity activity, String appId, String adSlot_id) {
        KSCMobileAdsProto530.MobadsRequest.Builder requestBuilder = KSCMobileAdsProto530.MobadsRequest.newBuilder();
        try {
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            String requestId = appId + adSlot_id + System.currentTimeMillis();
            int length = requestId.length();
            if (length > 32) {
                requestId = requestId.substring(0, 32);
            } else if (length < 32) {
                for (int i = 0; i < 32 - length; i++) {
                    requestId = requestId + "0";
                }
            }
            requestBuilder.setRequestId(requestId);
            requestBuilder.setApiVersion(getApiVersion());
            requestBuilder.setAdslot(getAdSlot(adSlot_id, dm.widthPixels, dm.heightPixels));
            requestBuilder.setApp(getAppInfo(activity, appId));
            requestBuilder.setDevice(getDeviceInfo(activity));
            requestBuilder.setNetwork(getNetwork(activity));
            requestBuilder.setGps(getGps(activity));
            requestBuilder.setIsDebug(false);
        } catch (Exception e) {
            KSCLog.e("get ad request exception", e.getMessage());
        }
        return requestBuilder.build();
    }

    /**
     * 返回API版本号信息
     *
     * @return API版本号信息
     */
    private KSCMobileAdsProto530.Version.Builder getApiVersion() {
        KSCMobileAdsProto530.Version.Builder versionBuilder = KSCMobileAdsProto530.Version.newBuilder();
        versionBuilder.setMajor(5);
        versionBuilder.setMinor(3);
        versionBuilder.setMicro(0);
        return versionBuilder;
    }

    /**
     * 获得广告位信息
     *
     * @param adSlot_id 广告ID
     * @param width     广告宽度
     * @param height    广告高度
     * @return 广告位信息
     */
    private KSCMobileAdsProto530.AdSlot.Builder getAdSlot(String adSlot_id, int width, int height) {
        KSCMobileAdsProto530.AdSlot.Builder adSlotBuilder = KSCMobileAdsProto530.AdSlot.newBuilder();
        adSlotBuilder.setAdslotId(adSlot_id);
        adSlotBuilder.setAdslotSize(getAdSlotSize(width, height));
        adSlotBuilder.setVideo(getVideo("奖励视频", 15000, KSCMobileAdsProto530.Video.CopyRight.CR_EXIST));
        adSlotBuilder.setAdslotType(8);// 广告位类型
        adSlotBuilder.setAds(1);// 返回广告数量
        return adSlotBuilder;
    }

    /**
     * 获得广告位尺寸
     *
     * @param width  宽度
     * @param height 高度
     * @return 广告位尺寸
     */
    private KSCMobileAdsProto530.Size.Builder getAdSlotSize(int width, int height) {
        KSCMobileAdsProto530.Size.Builder sizeBuilder = KSCMobileAdsProto530.Size.newBuilder();
        sizeBuilder.setWidth(width);
        sizeBuilder.setHeight(height);
        return sizeBuilder;
    }

    /**
     * 获得广告视频信息
     *
     * @param title         视频Title
     * @param contentLength 视频长度
     * @param copyRight     版权信息
     * @return 视频信息
     */
    private KSCMobileAdsProto530.Video.Builder getVideo(String title, int contentLength, KSCMobileAdsProto530.Video.CopyRight copyRight) {
        KSCMobileAdsProto530.Video.Builder videoBuilder = KSCMobileAdsProto530.Video.newBuilder();
        videoBuilder.setTitle(ByteString.copyFromUtf8(title));
        videoBuilder.setContentLength(contentLength);
        videoBuilder.setCopyright(copyRight);
        return videoBuilder;
    }

    /**
     * 获得应用信息
     *
     * @param context 上下文
     * @param appId   应用ID，在Mobile SSP 注册
     * @return 应用信息
     */
    private KSCMobileAdsProto530.App.Builder getAppInfo(Context context, String appId) {
        KSCMobileAdsProto530.App.Builder appBuilder = KSCMobileAdsProto530.App.newBuilder();
        appBuilder.setAppId(appId);
        appBuilder.setChannelId("");
        appBuilder.setAppVersion(getAppVersion(context));
        appBuilder.setAppPackage(KSCPackageUtils.getPackageName(context));
        return appBuilder;
    }

    /**
     * 获得APP版本信息
     *
     * @param context 上下文
     * @return APP版本信息
     */
    private KSCMobileAdsProto530.Version.Builder getAppVersion(Context context) {
        KSCMobileAdsProto530.Version.Builder versionBuilder = KSCMobileAdsProto530.Version.newBuilder();
        String versionName = KSCPackageUtils.getVersionName(context);
        if (versionName == null || versionName.equals("")) {
            return versionBuilder;
        }
        String[] appVersion = versionName.split("\\.");
        for (int i = 0; i < appVersion.length; i++) {
            if (i == 0) {
                versionBuilder.setMajor(Integer.valueOf(appVersion[i]));
            } else if (i == 1) {
                versionBuilder.setMinor(Integer.valueOf(appVersion[i]));
            } else if (i == 2) {
                versionBuilder.setMicro(Integer.valueOf(appVersion[i]));
            }
        }
        return versionBuilder;
    }

    /**
     * 获得屏幕宽高信息
     *
     * @param activity 上下文
     * @return 屏幕信息
     */
    private KSCMobileAdsProto530.Size.Builder getScreenSize(Activity activity) {
        DisplayMetrics matrix = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(matrix);
        KSCMobileAdsProto530.Size.Builder sizeBuilder = KSCMobileAdsProto530.Size.newBuilder();
        sizeBuilder.setWidth(matrix.widthPixels);
        sizeBuilder.setHeight(matrix.heightPixels);
        return sizeBuilder;
    }

    /**
     * 获得设备信息
     *
     * @param activity 上下文
     * @return 设备信息
     */
    private KSCMobileAdsProto530.Device.Builder getDeviceInfo(Activity activity) {
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
        return deviceBuilder;
    }

    /**
     * 获得手机操作系统的版本信息
     *
     * @return 操作系统版本信息
     */
    private KSCMobileAdsProto530.Version.Builder getOsVersion() {
        KSCMobileAdsProto530.Version.Builder versionBuilder = KSCMobileAdsProto530.Version.newBuilder();
        String[] systemOsVersion = Build.VERSION.RELEASE.split("\\.");
        for (int i = 0; i < systemOsVersion.length; i++) {
            if (i == 0) {
                versionBuilder.setMajor(Integer.valueOf(systemOsVersion[i]));
            } else if (i == 1) {
                versionBuilder.setMinor(Integer.valueOf(systemOsVersion[i]));
            } else if (i == 2) {
                versionBuilder.setMicro(Integer.valueOf(systemOsVersion[i]));
            }
        }
        return versionBuilder;
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
    private KSCMobileAdsProto530.UdId.Builder getUdId(Activity activity) {
        String imei = KSCDeviceUtils.getImei(activity);
        String androidId = KSCDeviceUtils.getAndroidID(activity);
        KSCMobileAdsProto530.UdId.Builder udIdBuilder = KSCMobileAdsProto530.UdId.newBuilder();
        udIdBuilder.setImei(imei);
        udIdBuilder.setMac(KSCNetUtils.getMac(activity));
        udIdBuilder.setAndroidId(androidId);
        udIdBuilder.setImeiMd5(KSCMD5Utils.md5(imei));
        udIdBuilder.setAndroididMd5(KSCMD5Utils.md5(androidId));
        return udIdBuilder;
    }

    /**
     * 获得网络环境信息
     *
     * @return 网络环境信息
     */
    private KSCMobileAdsProto530.Network.Builder getNetwork(Activity activity) {
        KSCMobileAdsProto530.Network.Builder networkBuilder = KSCMobileAdsProto530.Network.newBuilder();
        networkBuilder.setIpv4(KSCNetUtils.getIp());
        int netType = KSCNetUtils.getNetType(activity);
        if (netType == KSCNetUtils.NETWORK_TYPE_INVALID) {
            networkBuilder.setConnectionType(KSCMobileAdsProto530.Network.ConnectionType.CONNECTION_UNKNOWN);
        } else if (netType == KSCNetUtils.NETWORK_TYPE_WIFI) {
            networkBuilder.setConnectionType(KSCMobileAdsProto530.Network.ConnectionType.WIFI);
        } else if (netType == KSCNetUtils.NETWORK_TYPE_ETHERNET) {
            networkBuilder.setConnectionType(KSCMobileAdsProto530.Network.ConnectionType.ETHERNET);
        } else if (netType == KSCNetUtils.NETWORK_TYPE_2G) {
            networkBuilder.setConnectionType(KSCMobileAdsProto530.Network.ConnectionType.CELL_2G);
        } else if (netType == KSCNetUtils.NETWORK_TYPE_3G) {
            networkBuilder.setConnectionType(KSCMobileAdsProto530.Network.ConnectionType.CELL_3G);
        } else if (netType == KSCNetUtils.NETWORK_TYPE_4G) {
            networkBuilder.setConnectionType(KSCMobileAdsProto530.Network.ConnectionType.CELL_4G);
        } else if (netType == KSCNetUtils.NETWORK_TYPE_UNKNOWN) {
            networkBuilder.setConnectionType(KSCMobileAdsProto530.Network.ConnectionType.CELL_UNKNOWN);
        } else {
            networkBuilder.setConnectionType(KSCMobileAdsProto530.Network.ConnectionType.NEW_TYPE);
        }
        switch (KSCNetUtils.getOperators(activity)) {
            case 0:
                networkBuilder.setOperatorType(KSCMobileAdsProto530.Network.OperatorType.UNKNOWN_OPERATOR);
                break;
            case 1:
                networkBuilder.setOperatorType(KSCMobileAdsProto530.Network.OperatorType.CHINA_MOBILE);
                break;
            case 2:
                networkBuilder.setOperatorType(KSCMobileAdsProto530.Network.OperatorType.CHINA_UNICOM);
                break;
            case 3:
                networkBuilder.setOperatorType(KSCMobileAdsProto530.Network.OperatorType.CHINA_TELECOM);
                break;
            default:
                networkBuilder.setOperatorType(KSCMobileAdsProto530.Network.OperatorType.OTHER_OPERATOR);
                break;
        }
        networkBuilder.setCellularId(String.valueOf(KSCNetUtils.getCellId(activity)));
        networkBuilder.addWifiAps(getWifiAp(activity));
        return networkBuilder;
    }

    /**
     * 获得WIFI热点信息
     *
     * @return WIFI热点信息
     */
    private KSCMobileAdsProto530.WiFiAp.Builder getWifiAp(Context context) {
        KSCMobileAdsProto530.WiFiAp.Builder wifiBuilder = KSCMobileAdsProto530.WiFiAp.newBuilder();
        wifiBuilder.setApMac(KSCNetUtils.getMac(context));
        wifiBuilder.setRssi(KSCNetUtils.getRssi(context));
        wifiBuilder.setApName(ByteString.copyFromUtf8(KSCNetUtils.getWifiName(context)));
        wifiBuilder.setIsConnected(KSCNetUtils.isWifiConnected(context));
        return wifiBuilder;
    }

    /**
     * 获得GPS信息
     *
     * @return GPS信息
     */
    private KSCMobileAdsProto530.Gps.Builder getGps(Activity context) {
        KSCMobileAdsProto530.Gps.Builder gpsBuilder = KSCMobileAdsProto530.Gps.newBuilder();
        gpsBuilder.setCoordinateType(KSCMobileAdsProto530.Gps.CoordinateType.WGS84);
        gpsBuilder.setLongitude(KSCLocationUtils.getLongitude(context));
        gpsBuilder.setLatitude(KSCLocationUtils.getLatitude(context));
        gpsBuilder.setTimestamp((int) (System.currentTimeMillis() / 1000));
        return gpsBuilder;
    }

    /**
     * 解析返回数据并返回可用广告视频列表
     *
     * @param response 服务器返回数据
     * @return 广告视频列表
     */
    public List<KSCVideoAdBean> getVideoList(byte[] response) {
        List<KSCVideoAdBean> mList = new ArrayList<>();
        KSCMobileAdsProto530.MobadsResponse adResponse;
        try {
            adResponse = KSCMobileAdsProto530.MobadsResponse.parseFrom(response);
        } catch (InvalidProtocolBufferException e) {
            KSCLog.e("parse response to Mobile ads response exception", e);
            adResponse = null;
        }
        if (adResponse == null) {
            return mList;
        }
        mErrorCode = adResponse.getErrorCode();
        mRequestId = adResponse.getRequestId();
        List<KSCMobileAdsProto530.Ad> adList = adResponse.getAdsList();
        if (adList == null || adList.size() == 0) {
            return mList;
        }
        for (KSCMobileAdsProto530.Ad ad : adList) {
            List<KSCMobileAdsProto530.MaterialMeta> metaList = ad.getMetaGroupList();
            if (metaList == null || metaList.size() == 0) {
                continue;
            }
            List<KSCMobileAdsProto530.Tracking> trackList = ad.getAdTrackingList();
            Map<Integer, List<String>> trackMap = new HashMap<>();
            if (trackList != null && trackList.size() != 0) {
                for (KSCMobileAdsProto530.Tracking tracking : trackList) {
                    List<String> urlList = new ArrayList<>();
                    for (String url : tracking.getTrackingUrlList()) {
                        if (url == null || url.equals("")) {
                            continue;
                        }
                        urlList.add(url);
                    }
                    trackMap.put(tracking.getTrackingEvent().getNumber(), urlList);
                }
            }

            for (KSCMobileAdsProto530.MaterialMeta meta : metaList) {
                KSCVideoAdBean bean = new KSCVideoAdBean();
                bean.setHtml(ad.getHtmlSnippet().toStringUtf8());
                bean.setVideoUrl(meta.getVideoUrl());
                bean.setClickUrl(meta.getClickUrl());
                bean.setBrandName(meta.getBrandName());
                List<String> mLandingPageTrack = meta.getWinNoticeUrlList();
                for (String url : mLandingPageTrack) {
                    if (url == null || url.equals("")) {
                        mLandingPageTrack.remove(url);
                    }
                }
                trackMap.put(KSCMobileAdsProto530.Tracking.TrackingEvent.AD_EXPOSURE_VALUE, mLandingPageTrack);
                bean.setTrackingUrl(trackMap);
                mList.add(bean);
            }
        }
        return mList;
    }

    /**
     * 获得请求ID
     *
     * @return RequestId
     */
    public String getRequestId() {
        return mRequestId;
    }

    /**
     * 获得返回码
     *
     * @return 返回码
     */
    public long getErrorCode() {
        return mErrorCode;
    }

    private static class SingletonHolder {
        public static final KSCMobileAdProtoAPI INSTANCE = new KSCMobileAdProtoAPI();
    }

}

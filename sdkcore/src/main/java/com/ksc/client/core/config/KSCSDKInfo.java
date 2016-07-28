package com.ksc.client.core.config;

import android.content.Context;
import android.text.TextUtils;

import com.ksc.client.core.base.entity.AppInfo;
import com.ksc.client.util.KSCLog;
import com.ksc.client.util.KSCStorageUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Alamusi on 2016/6/22.
 */
public class KSCSDKInfo {
    private static final String SDK_CONFIG_KEY_BUILD_VERSION = "KSCBuildVersion";
    private static final String SDK_CONFIG_KEY_KSC_VERSION = "KSCVersion";
    private static final String SDK_CONFIG_KEY_CHANNEL_PARAM = "KSCChannelParam";
    private static final String SDK_CONFIG_KEY_CHANNEL = "KSCChannelId";
    private static final String SDK_CONFIG_KEY_CHANNEL_VERSION = "KSCChannelVersion";
    private static final String SDK_CONFIG_KEY_INIT_URL = "KSCInitUrl";
    private static final String SDK_CONFIG_KEY_GET_ORDER_URL = "KSCGetOrderUrl";
    private static final String SDK_DEFAULT_INIT_URL = "";
    private static final String SDK_DEFAULT_ORDER_URL = "";

    private static Properties mConfigProperties = new Properties();

    private static AppInfo mAppInfo = null;
    private static String mAppVersionName = "undefined";
    private static String mAppVersionCode = "undefined";
    private static String mPackageName = "undefined";

    public static String getValue(String key) {
        synchronized ("getValue") {
            if (key == null) {
                return null;
            }
            return mConfigProperties.getProperty(key);
        }
    }

    public static String getValue(String key, String defaultValue) {
        synchronized ("getValue") {
            if (key == null) {
                return null;
            }
            return mConfigProperties.getProperty(key, defaultValue);
        }
    }

    public static String getAppVersionName() {
        return mAppVersionName;
    }

    public static void setAppVersionName(String appVersionName) {
        KSCSDKInfo.mAppVersionName = appVersionName;
    }

    public static String getAppVersionCode() {
        return mAppVersionCode;
    }

    public static void setAppVersionCode(String appVersionCode) {
        KSCSDKInfo.mAppVersionCode = appVersionCode;
    }

    public static String getPackageName() {
        return mPackageName;
    }

    public static void setPackageName(String packageName) {
        KSCSDKInfo.mPackageName = packageName;
    }

    public static void setAppInfo(AppInfo appInfo) {
        KSCSDKInfo.mAppInfo = appInfo;
    }

    /**
     * 从本地目录加载配置文件
     *
     * @param context
     */
    public static void loadLocalConfig(Context context) {
        try {
            File file = KSCStorageUtils.getFile(context, KSCSDKConstant.SDK_DIR, KSCSDKConstant.CONFIG_FILE_NAME);
            if (!file.exists()) {
                KSCStorageUtils.copyFile(context, KSCSDKConstant.CONFIG_FILE_NAME, file);
            }
            mConfigProperties.clear();
            mConfigProperties.load(KSCStorageUtils.getInputStream(file));
        } catch (IOException e) {
            KSCLog.e("failed to load config properties from " + KSCSDKConstant.CONFIG_FILE_NAME, e);
        }
    }

    /**
     * 从Assets 加载配置文件
     *
     * @param context
     */
    public static void loadAssetsConfig(Context context) {
        try {
            mConfigProperties.clear();
            mConfigProperties.load(context.getAssets().open(KSCSDKConstant.CONFIG_FILE_NAME));
        } catch (IOException e) {
            KSCLog.e("failed to load config properties from " + KSCSDKConstant.CONFIG_FILE_NAME, e);
        }
    }

    /**
     * 从Assets 配置文件获得build Version
     *
     * @param context
     * @return
     */
    public static String getBuildVersionFromAssetsConfig(Context context) {
        String buildVersion = null;
        try {
            Properties assertProperties = new Properties();
            assertProperties.load(context.getAssets().open(KSCSDKConstant.CONFIG_FILE_NAME));
            buildVersion = assertProperties.getProperty(SDK_CONFIG_KEY_BUILD_VERSION);
        } catch (IOException e) {
            KSCLog.e("failed to load config properties from " + KSCSDKConstant.CONFIG_FILE_NAME, e);
        }
        return buildVersion;
    }

    /**
     * 判断是不是横竖屏
     *
     * @return 默认竖屏
     */
    public static boolean isLandscape() {
        if (mAppInfo != null) {
            int orientation = mAppInfo.getScreenOrientation();
            return orientation == KSCSDKConstant.ORIENTATION_LANDSCAPE;
        } else {
            return false;
        }
    }

    /**
     * 获得APPID
     *
     * @return
     */
    public static String getAppId() {
        if (mAppInfo != null) {
            return mAppInfo.getAppId();
        } else {
            return null;
        }

    }

    /**
     * 获得APPKEY
     *
     * @return
     */
    public static String getAppKey() {
        if (mAppInfo == null) {
            return null;
        } else {
            return mAppInfo.getAppKey();
        }
    }

    /**
     * 获得编译版本号
     *
     * @return
     */
    public static String getBuildVersion() {
        return getValue(SDK_CONFIG_KEY_BUILD_VERSION);
    }

    /**
     * 获得KSC SDK版本号
     *
     * @return
     */
    public static String getKSCVersion() {
        return getValue(SDK_CONFIG_KEY_KSC_VERSION);
    }

    /**
     * 获得本地的渠道参数
     *
     * @return
     */
    public static String getChannelParam() {
        return getValue(SDK_CONFIG_KEY_CHANNEL_PARAM);
    }

    /**
     * 获得渠道ID
     *
     * @return
     */
    public static String getChannelId() {
        return getValue(SDK_CONFIG_KEY_CHANNEL);
    }

    /**
     * 获得渠道版本号
     *
     * @return
     */
    public static String getChannelVersion() {
        return getValue(SDK_CONFIG_KEY_CHANNEL_VERSION);
    }

    /**
     * 获得获取初始化参数的地址
     *
     * @return
     */
    public static String getInitUrl() {
        String url = getValue(SDK_CONFIG_KEY_INIT_URL);
        if (TextUtils.isEmpty(url)) {
            url = SDK_DEFAULT_INIT_URL;
        }
        return url;
    }

    /**
     * 获得获取订单的地址
     *
     * @return
     */
    public static String getCreateOrderUrl() {
        String url = getValue(SDK_CONFIG_KEY_GET_ORDER_URL);
        if (TextUtils.isEmpty(url)) {
            url = SDK_DEFAULT_ORDER_URL;
        }
        return url;
    }
}

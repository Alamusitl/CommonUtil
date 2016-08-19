package com.sensorsdata.analytics.android.sdk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException;
import com.sensorsdata.analytics.android.sdk.util.SensorsDataUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

/**
 * Sensors Analytics SDK
 */
public class SensorsDataAPI {

    // 可视化埋点功能最低API版本
    static final int VTRACK_SUPPORTED_MIN_API = 16;
    // SDK版本
    static final String VERSION = "1.5.3";
    private static final Pattern KEY_PATTERN = Pattern.compile(
            "^((?!^distinct_id$|^original_id$|^time$|^properties$|^id$|^first_id$|^second_id$|^users$|^events$|^event$|^user_id$|^date$|^datetime$)[a-zA-Z_$][a-zA-Z\\d_$]{0,99})$",
            Pattern.CASE_INSENSITIVE);
    // Maps each token to a singleton SensorsDataAPI instance
    private static final Map<Context, SensorsDataAPI> sInstanceMap = new HashMap<Context, SensorsDataAPI>();
    private static final SharedPreferencesLoader sPrefsLoader = new SharedPreferencesLoader();
    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"
            + ".SSS");
    private static final String LOGTAG = "SA.SensorsDataAPI";
    // Configures
  /* SensorsAnalytics 地址 */
    private final String mServerUrl;
    /* 可视化埋点配置地址 */
    private final String mConfigureUrl;
    /* 可视化埋点WebServer地址 */
    private final String mVTrackServerUrl;
    /* Debug模式选项 */
    private final DebugMode mDebugMode;
    private final Context mContext;
    private final AnalyticsMessages mMessages;
    private final PersistentIdentity mPersistentIdentity;
    private final Map<String, Object> mDeviceInfo;
    private final Map<String, Long> mTrackTimer;
    private final VTrack mVTrack;
    /* Flush时间间隔 */
    private int mFlushInterval;
    /* Flush数据量阈值 */
    private int mFlushBulkSize;

    SensorsDataAPI(Context context, String serverURL, String configureURL, String vtrackServerURL,
                   DebugMode debugMode) {
        mContext = context;

        final String packageName = context.getApplicationContext().getPackageName();

        try {
            final ApplicationInfo appInfo = context.getApplicationContext().getPackageManager()
                    .getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            Bundle configBundle = appInfo.metaData;
            if (null == configBundle) {
                configBundle = new Bundle();
            }

            if (debugMode.isDebugMode()) {
                Uri serverURI = Uri.parse(serverURL);

                int pathPrefix = serverURI.getPath().lastIndexOf('/');
                String newPath = serverURI.getPath().substring(0, pathPrefix) + "/debug";

                // 将 URI Path 中末尾的部分替换成 '/debug'
                mServerUrl = serverURI.buildUpon().path(newPath).build().toString();
            } else {
                mServerUrl = serverURL;
            }

            // 若 Configure Url 为 'api/vtrack/config' ，则补齐 SDK 类型
            Uri configureURI = Uri.parse(configureURL);
            if (configureURI.getPath().equals("/api/vtrack/config") || configureURI.getPath().equals
                    ("/api/vtrack/config/")) {
                mConfigureUrl = configureURI.buildUpon().appendPath("Android.conf").build().toString();
            } else {
                mConfigureUrl = configureURL;
            }

            if (vtrackServerURL == null) {
                // 根据 Configure Url 自动配置 VTrack Server Url
                mVTrackServerUrl = configureURI.buildUpon().path("/api/ws").scheme("ws").build().toString();
            } else {
                mVTrackServerUrl = vtrackServerURL;
            }

            mDebugMode = debugMode;

            mFlushInterval = configBundle.getInt("com.sensorsdata.analytics.android.FlushInterval", 60000);
            mFlushBulkSize = configBundle.getInt("com.sensorsdata.analytics.android.FlushBulkSize",
                    100);

            if (Build.VERSION.SDK_INT >= VTRACK_SUPPORTED_MIN_API
                    && configBundle.getBoolean("com.sensorsdata.analytics.android.VTrack", true)) {
                String resourcePackageName =
                        configBundle.getString("com.sensorsdata.analytics.android.ResourcePackageName");
                if (null == resourcePackageName) {
                    resourcePackageName = context.getPackageName();
                }

                mVTrack = new ViewCrawler(mContext, resourcePackageName);
            } else {
                Log.i(LOGTAG, "VTrack is not supported on this Android OS Version");
                mVTrack = new VTrackUnsupported();
            }
        } catch (final PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Can't configure SensorsDataAPI with package name " + packageName,
                    e);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            final Application app = (Application) context.getApplicationContext();
            app.registerActivityLifecycleCallbacks(new LifecycleCallbacks());
        }

        Log.v(LOGTAG, String.format("Initializing the instance of Sensors Analytics SDK with server"
                        + " url '%s', configure url '%s', vtrack server url '%s', flush interval %d ms", mServerUrl,
                mConfigureUrl, mVTrackServerUrl, mFlushInterval));

        final Map<String, Object> deviceInfo = new HashMap<String, Object>();

        {
            deviceInfo.put("$lib", "Android");
            deviceInfo.put("$lib_version", VERSION);
            deviceInfo.put("$os", "Android");
            deviceInfo.put("$os_version",
                    Build.VERSION.RELEASE == null ? "UNKNOWN" : Build.VERSION.RELEASE);
            deviceInfo
                    .put("$manufacturer", Build.MANUFACTURER == null ? "UNKNOWN" : Build.MANUFACTURER);
            deviceInfo.put("$model", Build.MODEL == null ? "UNKNOWN" : Build.MODEL);
            try {
                final PackageManager manager = mContext.getPackageManager();
                final PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);
                deviceInfo.put("$app_version", info.versionName);
            } catch (final PackageManager.NameNotFoundException e) {
                Log.e(LOGTAG, "Exception getting app version name", e);
            }
            final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            deviceInfo.put("$screen_height", displayMetrics.heightPixels);
            deviceInfo.put("$screen_width", displayMetrics.widthPixels);

            TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context
                    .TELEPHONY_SERVICE);
            String operatorString = telephonyManager.getSimOperator();

            if (operatorString == null) {
                // DO NOTHING
            } else if (operatorString.equals("46000") || operatorString.equals("46002")) {
                deviceInfo.put("$carrier", "中国移动");
            } else if (operatorString.equals("46001")) {
                deviceInfo.put("$carrier", "中国联通");
            } else if (operatorString.equals("46003")) {
                deviceInfo.put("$carrier", "中国电信");
            } else {
                deviceInfo.put("$carrier", "其他");
            }
        }

        mDeviceInfo = Collections.unmodifiableMap(deviceInfo);
        mTrackTimer = new HashMap<String, Long>();

        mPersistentIdentity = getPersistentIdentity(context);

        mMessages = AnalyticsMessages.getInstance(mContext, packageName);

        mVTrack.startUpdates();
        mMessages.checkConfigure(new DecideMessages(mVTrack));
    }

    /**
     * 获取SensorsDataAPI单例
     *
     * @param context App的Context
     * @return SensorsDataAPI单例
     */
    public static SensorsDataAPI sharedInstance(Context context) {
        if (null == context) {
            return null;
        }
        synchronized (sInstanceMap) {
            final Context appContext = context.getApplicationContext();
            SensorsDataAPI instance = sInstanceMap.get(appContext);

            if (null == instance) {
                Log.w(LOGTAG, "The static method sharedInstance(context, serverURL, configureURL, "
                        + "vtrackServerURL, debugMode) should be called before calling sharedInstance()");
            }
            return instance;
        }
    }

    /**
     * 初始化并获取SensorsDataAPI单例
     *
     * @param context      App 的 Context
     * @param serverURL    用于收集事件的服务地址
     * @param configureUrl 用于获取SDK配置的服务地址
     * @param debugMode    Debug模式,
     *                     {@link com.sensorsdata.analytics.android.sdk.SensorsDataAPI.DebugMode}
     * @return SensorsDataAPI单例
     */
    public static SensorsDataAPI sharedInstance(Context context, String serverURL, String
            configureUrl, DebugMode debugMode) {
        if (null == context) {
            return null;
        }

        synchronized (sInstanceMap) {
            final Context appContext = context.getApplicationContext();

            SensorsDataAPI instance = sInstanceMap.get(appContext);
            if (null == instance && ConfigurationChecker.checkBasicConfiguration(appContext)) {
                instance = new SensorsDataAPI(appContext, serverURL, configureUrl, null, debugMode);
                sInstanceMap.put(appContext, instance);
            }

            return instance;
        }
    }

    /**
     * 初始化并获取SensorsDataAPI单例（打开可视化埋点功能）
     *
     * @param context         App的Context
     * @param serverURL       用于收集事件的服务地址
     * @param configureURL    用于获取SDK配置的服务地址
     * @param vtrackServerURL 可视化埋点的WebServer地址
     * @param debugMode       Debug模式,
     *                        {@link com.sensorsdata.analytics.android.sdk.SensorsDataAPI.DebugMode}
     * @return SensorsDataAPI单例
     */
    public static SensorsDataAPI sharedInstance(Context context, String serverURL,
                                                String configureURL, String vtrackServerURL, DebugMode debugMode) {
        if (null == context) {
            return null;
        }

        synchronized (sInstanceMap) {
            final Context appContext = context.getApplicationContext();

            SensorsDataAPI instance = sInstanceMap.get(appContext);
            if (null == instance && ConfigurationChecker.checkBasicConfiguration(appContext)) {
                instance = new SensorsDataAPI(appContext, serverURL, configureURL, vtrackServerURL,
                        debugMode);
                sInstanceMap.put(appContext, instance);
            }

            return instance;
        }
    }

    /**
     * 两次数据发送的最小时间间隔，单位毫秒
     * <p/>
     * 默认值为60 * 1000毫秒
     * 在每次调用track、signUp以及profileSet等接口的时候，都会检查如下条件，以判断是否向服务器上传数据:
     * <p/>
     * 1. 是否是WIFI/3G/4G网络条件
     * 2. 是否满足发送条件之一:
     * 1) 与上次发送的时间间隔是否大于 flushInterval
     * 2) 本地缓存日志数目是否大于 flushBulkSize
     * <p/>
     * 如果满足这两个条件，则向服务器发送一次数据；如果不满足，则把数据加入到队列中，等待下次检查时把整个队列的内
     * 容一并发送。需要注意的是，为了避免占用过多存储，队列最多只缓存20MB数据。
     *
     * @return 返回时间间隔，单位毫秒
     */
    public int getFlushInterval() {
        return mFlushInterval;
    }

    /**
     * 设置两次数据发送的最小时间间隔
     *
     * @param flushInterval 时间间隔，单位毫秒
     */
    public void setFlushInterval(int flushInterval) {
        mFlushInterval = flushInterval;
    }

    /**
     * 返回本地缓存日志的最大条目数
     * <p/>
     * 默认值为60 * 1000毫秒
     * 在每次调用track、signUp以及profileSet等接口的时候，都会检查如下条件，以判断是否向服务器上传数据:
     * <p/>
     * 1. 是否是WIFI/3G/4G网络条件
     * 2. 是否满足发送条件之一:
     * 1) 与上次发送的时间间隔是否大于 flushInterval
     * 2) 本地缓存日志数目是否大于 flushBulkSize
     * <p/>
     * 如果满足这两个条件，则向服务器发送一次数据；如果不满足，则把数据加入到队列中，等待下次检查时把整个队列的内
     * 容一并发送。需要注意的是，为了避免占用过多存储，队列最多只缓存20MB数据。
     *
     * @return 返回本地缓存日志的最大条目数
     */
    public int getFlushBulkSize() {
        return mFlushBulkSize;
    }

    /**
     * 设置本地缓存日志的最大条目数
     *
     * @param flushBulkSize 缓存数目
     */
    public void setFlushBulkSize(int flushBulkSize) {
        mFlushBulkSize = flushBulkSize;
    }

    /**
     * 获取当前用户的distinctId
     * <p/>
     * 若调用前未调用 {@link #identify(String)} 设置用户的 distinctId，SDK 会调用 {@link java.util.UUID} 随机生成
     * UUID，作为用户的 distinctId
     *
     * @return 当前用户的distinctId
     */
    public String getDistinctId() {
        return new String(mPersistentIdentity.getDistinctId());
    }

    /**
     * 设置当前用户的distinctId。一般情况下，如果是一个注册用户，则应该使用注册系统内
     * 的user_id，如果是个未注册用户，则可以选择一个不会重复的匿名ID，如设备ID等，如果
     * 客户没有设置indentify，则使用SDK自动生成的匿名ID
     *
     * @param distinctId 当前用户的distinctId，仅接受数字、下划线和大小写字母
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当 distinctId
     *                                                                               不符合规范时抛出异常
     */
    public void identify(String distinctId) throws InvalidDataException {
        assertDistinctId(distinctId);

        synchronized (mPersistentIdentity) {
            mPersistentIdentity.setDistinctId(distinctId);
        }
    }

    /**
     * 记录第一次登录行为
     * <p/>
     * 这个接口是一个较为复杂的功能，请在使用前先阅读相关说明:
     * http://www.sensorsdata.cn/manual/track_signup.html
     * 并在必要时联系我们的技术支持人员。
     *
     * @param newDistinctId 用户完成注册后生成的注册ID
     * @param properties    事件的属性
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当 distinctId
     *                                                                               不符合规范或事件属性不符合规范时抛出异常
     */
    public void trackSignUp(String newDistinctId, JSONObject properties) throws InvalidDataException {
        String originalDistinctId = getDistinctId();
        identify(newDistinctId);

        trackEvent(EventType.TRACK_SIGNUP, "$SignUp", properties, originalDistinctId);
    }

    /**
     * 与 {@link #trackSignUp(String, org.json.JSONObject)} 类似，无事件属性
     * <p/>
     * 这个接口是一个较为复杂的功能，请在使用前先阅读相关说明:
     * http://www.sensorsdata.cn/manual/track_signup.html，
     * 并在必要时联系我们的技术支持人员。
     *
     * @param newDistinctId 用户完成注册后生成的注册ID
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当 distinctId
     *                                                                               不符合规范时抛出异常
     */
    public void trackSignUp(String newDistinctId) throws InvalidDataException {
        String originalDistinctId = getDistinctId();
        identify(newDistinctId);

        trackEvent(EventType.TRACK_SIGNUP, "$SignUp", null, originalDistinctId);
    }

    /**
     * 用于在 App 首次启动时追踪渠道来源，并设置追踪渠道事件的属性。
     * <p/>
     * 这是 Sensors Analytics 进阶功能，请参考文档 https://sensorsdata.cn/manual/track_installation.html
     *
     * @param eventName  渠道追踪事件的名称
     * @param properties 渠道追踪事件的属性
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当事件名称或属性
     *                                                                               不符合规范时抛出异常
     */
    public void trackInstallation(String eventName, JSONObject properties)
            throws InvalidDataException {
        // 先发送 track
        trackEvent(EventType.TRACK, eventName, properties, null);

        // 再发送 profile_set_once
        trackEvent(EventType.PROFILE_SET_ONCE, null, properties, null);
    }

    /**
     * 调用track接口，追踪一个带有属性的事件
     *
     * @param eventName  事件的名称
     * @param properties 事件的属性
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当事件名称或属性
     *                                                                               不符合规范时抛出异常
     */
    public void track(String eventName, JSONObject properties) throws InvalidDataException {
        trackEvent(EventType.TRACK, eventName, properties, null);
    }

    /**
     * 与 {@link #track(String, org.json.JSONObject)} 类似，无事件属性
     *
     * @param eventName 事件的名称
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当事件名称
     *                                                                               不符合规范时抛出异常
     */
    public void track(String eventName) throws InvalidDataException {
        trackEvent(EventType.TRACK, eventName, null, null);
    }

    /**
     * 初始化事件的计时器。
     * <p/>
     * 若需要统计某个事件的持续时间，先在事件开始时调用 trackTimer("Event") 记录事件开始时间，该方法并不会真正发
     * 送事件；随后在事件结束时，调用 track("Event", properties)，SDK 会追踪 "Event" 事件，并自动将事件持续时
     * 间记录在事件属性 "event_duration" 中。
     * <p/>
     * 多次调用 trackTimer("Event") 时，事件 "Event" 的开始时间以最后一次调用时为准。
     *
     * @param eventName 事件的名称
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当事件名称
     *                                                                               不符合规范时抛出异常
     */
    public void trackTimer(final String eventName) throws InvalidDataException {
        assertKey(eventName);
        final long eventBegin = System.currentTimeMillis();
        synchronized (mTrackTimer) {
            mTrackTimer.put(eventName, eventBegin);
        }
    }

    /**
     * 清除所有事件计时器
     */
    public void clearTrackTimer() {
        synchronized (mTrackTimer) {
            mTrackTimer.clear();
        }
    }

    /**
     * 将所有本地缓存的日志发送到 Sensors Analytics.
     */
    public void flush() {
        mMessages.sendData();
    }

    /**
     * 获取事件公共属性
     *
     * @return 当前所有Super属性
     */
    public JSONObject getSuperProperties() {
        JSONObject ret = new JSONObject();
        mPersistentIdentity.addSuperPropertiesToObject(ret);
        return ret;
    }

    /**
     * 注册所有事件都有的公共属性
     *
     * @param superProperties 事件公共属性
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当公共属性不符合规范时抛出异常
     */
    public void registerSuperProperties(JSONObject superProperties) throws InvalidDataException {
        assertPropertyTypes(EventType.REGISTER_SUPER_PROPERTIES, superProperties);
        synchronized (mPersistentIdentity) {
            mPersistentIdentity.registerSuperProperties(superProperties);
        }
    }

    // Conveniences for testing.

    /**
     * 删除事件公共属性
     *
     * @param superPropertyName 事件属性名称
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当属性名称不符合规范时抛出异常
     */
    public void unregisterSuperProperty(String superPropertyName) throws InvalidDataException {
        assertKey(superPropertyName);
        synchronized (mPersistentIdentity) {
            mPersistentIdentity.unregisterSuperProperty(superPropertyName);
        }
    }

    /**
     * 删除所有事件公共属性
     */
    public void clearSuperProperties() {
        synchronized (mPersistentIdentity) {
            mPersistentIdentity.clearSuperProperties();
        }
    }

    /**
     * 设置用户的一个或多个Profile。
     * Profile如果存在，则覆盖；否则，新创建。
     *
     * @param properties 属性列表
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当属性名称或属性值不符合规范时抛出异常
     */
    public void profileSet(JSONObject properties) throws InvalidDataException {
        trackEvent(EventType.PROFILE_SET, null, properties, null);
    }

    /**
     * 设置用户的一个Profile，如果之前存在，则覆盖，否则，新创建
     *
     * @param property 属性名称
     * @param value    属性的值，值的类型只允许为
     *                 {@link java.lang.String}, {@link java.lang.Number}, {@link java.util.Date}, {@link java.util.List}
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当属性名称或属性值不符合规范时抛出异常
     */
    public void profileSet(String property, Object value) throws InvalidDataException {
        try {
            trackEvent(EventType.PROFILE_SET, null, new JSONObject().put(property, value), null);
        } catch (JSONException e) {
            throw new InvalidDataException("Unexpected property name or value.");
        }
    }

    /**
     * 首次设置用户的一个或多个Profile。
     * 与profileSet接口不同的是，Profile如果存在，则覆盖；否则，新创建。
     *
     * @param properties 属性列表
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当属性名称或属性值不符合规范时抛出异常
     */
    public void profileSetOnce(JSONObject properties) throws InvalidDataException {
        trackEvent(EventType.PROFILE_SET_ONCE, null, properties, null);
    }

    /**
     * 首次设置用户的一个Profile
     * 与profileSet接口不同的是，如果之前存在，则忽略，否则，新创建
     *
     * @param property 属性名称
     * @param value    属性的值，值的类型只允许为
     *                 {@link java.lang.String}, {@link java.lang.Number}, {@link java.util.Date}, {@link java.util.List}
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当属性名称或属性值不符合规范时抛出异常
     */
    public void profileSetOnce(String property, Object value) throws InvalidDataException {
        try {
            trackEvent(EventType.PROFILE_SET_ONCE, null, new JSONObject().put(property, value), null);
        } catch (JSONException e) {
            throw new InvalidDataException("Unexpected property name or value.");
        }
    }

    /**
     * 给一个或多个数值类型的Profile增加一个数值。只能对数值型属性进行操作，若该属性
     * 未设置，则添加属性并设置默认值为0
     *
     * @param properties 一个或多个属性集合
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当属性名称或属性值不符合规范时抛出异常
     */
    public void profileIncrement(Map<String, ? extends Number> properties)
            throws InvalidDataException {
        trackEvent(EventType.PROFILE_INCREMENT, null, new JSONObject(properties), null);
    }

    /**
     * 给一个数值类型的Profile增加一个数值。只能对数值型属性进行操作，若该属性
     * 未设置，则添加属性并设置默认值为0
     *
     * @param property 属性名称
     * @param value    属性的值，值的类型只允许为 {@link java.lang.Number}
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当属性名称或属性值不符合规范时抛出异常
     */
    public void profileIncrement(String property, Number value) throws InvalidDataException {
        try {
            trackEvent(EventType.PROFILE_INCREMENT, null, new JSONObject().put(property, value), null);
        } catch (JSONException e) {
            throw new InvalidDataException("Unexpected property name or value.");
        }
    }

    /**
     * 给一个列表类型的Profile增加一个元素
     *
     * @param property 属性名称
     * @param value    新增的元素
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当属性名称或属性值不符合规范时抛出异常
     */
    public void profileAppend(String property, String value) throws InvalidDataException {
        try {
            final JSONArray append_values = new JSONArray();
            append_values.put(value);
            final JSONObject properties = new JSONObject();
            properties.put(property, append_values);
            trackEvent(EventType.PROFILE_APPEND, null, properties, null);
        } catch (final JSONException e) {
            throw new InvalidDataException("Unexpected property name or value");
        }
    }

    /**
     * 给一个列表类型的Profile增加一个或多个元素
     *
     * @param property 属性名称
     * @param values   新增的元素集合
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当属性名称或属性值不符合规范时抛出异常
     */
    public void profileAppend(String property, Set<String> values) throws InvalidDataException {
        try {
            final JSONArray append_values = new JSONArray();
            for (String value : values) {
                append_values.put(value);
            }
            final JSONObject properties = new JSONObject();
            properties.put(property, append_values);
            trackEvent(EventType.PROFILE_APPEND, null, properties, null);
        } catch (final JSONException e) {
            throw new InvalidDataException("Unexpected property name or value");
        }
    }

    /**
     * 删除用户的一个Profile
     *
     * @param property 属性名称
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当属性名称不符合规范时抛出异常
     */
    public void profileUnset(String property) throws InvalidDataException {
        try {
            trackEvent(EventType.PROFILE_UNSET, null, new JSONObject().put(property, true), null);
        } catch (final JSONException e) {
            throw new InvalidDataException("Unexpected property name");
        }
    }

    /**
     * 删除用户所有Profile
     *
     * @throws com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException 当属性名称不符合规范时抛出异常
     */
    public void profileDelete() throws InvalidDataException {
        trackEvent(EventType.PROFILE_DELETE, null, null, null);
    }

    /**
     * 清除本地所有用户、事件相关信息
     */
    public void reset() {
        mPersistentIdentity.clearPreferences();
    }

    boolean isDebugMode() {
        return mDebugMode.isDebugMode();
    }

    boolean isDebugWriteData() {
        return mDebugMode.isDebugWriteData();
    }

    String getServerUrl() {
        return mServerUrl;
    }

    String getConfigureUrl() {
        return mConfigureUrl;
    }

    String getVTrackServerUrl() {
        return mVTrackServerUrl;
    }

    PersistentIdentity getPersistentIdentity(final Context context) {
        final SharedPreferencesLoader.OnPrefsLoadedListener listener =
                new SharedPreferencesLoader.OnPrefsLoadedListener() {
                    @Override
                    public void onPrefsLoaded(SharedPreferences preferences) {
                    }
                };

        final String prefsName = "com.sensorsdata.analytics.android.sdk.SensorsDataAPI";
        final Future<SharedPreferences> storedPreferences =
                sPrefsLoader.loadPreferences(context, prefsName, listener);
        return new PersistentIdentity(storedPreferences);
    }

    private void trackEvent(EventType eventType, String eventName, JSONObject properties, String
            originalDistinctId) throws InvalidDataException {
        if (eventType.isTrack()) {
            assertKey(eventName);
        }
        assertPropertyTypes(eventType, properties);

        final long now = System.currentTimeMillis();

        final Long eventBegin;
        if (eventName != null) {
            synchronized (mTrackTimer) {
                eventBegin = mTrackTimer.get(eventName);
                mTrackTimer.remove(eventName);
            }
        } else {
            eventBegin = null;
        }

        synchronized (mPersistentIdentity) {
            try {
                JSONObject sendProperties = null;

                if (eventType.isTrack()) {
                    sendProperties = new JSONObject(mDeviceInfo);
                    mPersistentIdentity.addSuperPropertiesToObject(sendProperties);
                    // 当前网络状况
                    String networkType = SensorsDataUtils.networkType(mContext);
                    sendProperties.put("$wifi", networkType.equals("WIFI"));
                    sendProperties.put("$network_type", networkType);
                } else if (eventType.isProfile()) {
                    sendProperties = new JSONObject();
                } else {
                    return;
                }

                if (null != properties) {
                    final Iterator<?> propIter = properties.keys();
                    while (propIter.hasNext()) {
                        final String key = (String) propIter.next();
                        final Object value = properties.get(key);

                        if (value instanceof Date) {
                            sendProperties.put(key, mDateFormat.format((Date) value));
                        } else {
                            sendProperties.put(key, value);
                        }
                    }
                }

                if (null != eventBegin) {
                    sendProperties.put("event_duration", now - eventBegin);
                }

                JSONObject libProperties = new JSONObject();
                libProperties.put("$lib", "Android");
                libProperties.put("$lib_version", VERSION);

                if (mDeviceInfo.containsKey("$app_version")) {
                    libProperties.put("$app_version", mDeviceInfo.get("$app_version"));
                }

                final JSONObject dataObj = new JSONObject();

                dataObj.put("time", now);
                dataObj.put("type", eventType.getEventType());
                dataObj.put("properties", sendProperties);
                dataObj.put("distinct_id", mPersistentIdentity.getDistinctId());
                dataObj.put("lib", libProperties);

                if (eventType == EventType.TRACK) {
                    dataObj.put("event", eventName);
                } else if (eventType == EventType.TRACK_SIGNUP) {
                    dataObj.put("event", eventName);
                    dataObj.put("original_id", originalDistinctId);
                }

                // $binding_depolyed为true或者无该属性时，isDepolyed为true
                final boolean isDepolyed = sendProperties.optBoolean("$binding_depolyed", true);

                // 若$binding_depolyed为true，则删除这些属性
                if (sendProperties.has("$binding_depolyed")) {
                    libProperties.put("$lib_method", "vtrack");
                    libProperties.put("$lib_detail", sendProperties.get("$binding_trigger_id").toString());

                    // 可视化埋点的事件
                    if (mVTrack instanceof DebugTracking) {
                        // Deep clone the event
                        JSONObject debugDataObj = new JSONObject(dataObj.toString());
                        ((DebugTracking) mVTrack).reportTrack(debugDataObj);
                    }

                    sendProperties.remove("$binding_path");
                    sendProperties.remove("$binding_depolyed");
                    sendProperties.remove("$binding_trigger_id");
                } else {
                    libProperties.put("$lib_method", "code");

                    StackTraceElement[] trace = (new Exception()).getStackTrace();
                    if (trace.length > 2) {
                        StackTraceElement traceElement = trace[2];
                        libProperties.put("$lib_detail", String.format("%s##%s##%s##%s", traceElement
                                        .getClassName(), traceElement.getMethodName(), traceElement.getFileName(),
                                traceElement.getLineNumber()));
                    }
                }

                if (isDepolyed) {
                    mMessages.enqueueEventMessage(eventType.getEventType(), dataObj);
                }
            } catch (JSONException e) {
                throw new InvalidDataException("Unexpteced property");
            }
        }
    }

    private void assertPropertyTypes(EventType eventType, JSONObject properties) throws
            InvalidDataException {
        if (properties == null) {
            return;
        }

        for (Iterator iterator = properties.keys(); iterator.hasNext(); ) {
            String key = (String) iterator.next();

            // Check Keys
            assertKey(key);

            try {
                Object value = properties.get(key);

                if (!(value instanceof String || value instanceof Number || value
                        instanceof JSONArray || value instanceof Boolean || value instanceof Date)) {
                    throw new InvalidDataException("The property value must be an instance of "
                            + "String/Number/Boolean/JSONArray. [key='" + key + "', value='" + value.toString()
                            + "']");
                }

                if (value instanceof String && !key.startsWith("$") && ((String) value).length() > 255) {
                    throw new InvalidDataException("The property value is too long. [key='" + key
                            + "', value='" + value.toString() + "']");
                }
            } catch (JSONException e) {
                throw new InvalidDataException("Unexpected property key. [key='" + key + "']");
            }
        }
    }

    private void assertKey(String key) throws InvalidDataException {
        if (null == key || key.length() < 1) {
            throw new InvalidDataException("The key is empty.");
        }
        if (!(KEY_PATTERN.matcher(key).matches())) {
            throw new InvalidDataException("The key '" + key + "' is invalid.");
        }
    }

    private void assertDistinctId(String key) throws InvalidDataException {
        if (key == null || key.length() < 1) {
            throw new InvalidDataException("The distinct_id or original_id is empty.");
        }
        if (key.length() > 255) {
            throw new InvalidDataException("The max length of distinct_id or original_id is 255.");
        }
    }

    /**
     * Debug 模式，用于检验数据导入是否正确。该模式下，事件会逐条实时发送到 Sensors Analytics，并根据返回值检查
     * 数据导入是否正确。
     * <p/>
     * Debug 模式的具体使用方式，请参考:
     * http://www.sensorsdata.cn/manual/debug_mode.html
     * <p/>
     * Debug 模式有三种：
     * DEBUG_OFF - 关闭DEBUG模式
     * DEBUG_ONLY - 打开DEBUG模式，但该模式下发送的数据仅用于调试，不进行数据导入
     * DEBUG_AND_TRACK - 打开DEBUG模式，并将数据导入到SensorsAnalytics中
     */
    public enum DebugMode {
        DEBUG_OFF(false, false),
        DEBUG_ONLY(true, false),
        DEBUG_AND_TRACK(true, true);

        private final boolean mDebugMode;
        private final boolean mDebugWriteData;

        DebugMode(boolean debugMode, boolean debugWriteData) {
            mDebugMode = debugMode;
            mDebugWriteData = debugWriteData;
        }

        boolean isDebugMode() {
            return mDebugMode;
        }

        boolean isDebugWriteData() {
            return mDebugWriteData;
        }
    }

    private enum EventType {
        TRACK("track", true, false),
        TRACK_SIGNUP("track_signup", true, false),
        PROFILE_SET("profile_set", false, true),
        PROFILE_SET_ONCE("profile_set_once", false, true),
        PROFILE_UNSET("profile_unset", false, true),
        PROFILE_INCREMENT("profile_increment", false, true),
        PROFILE_APPEND("profile_append", false, true),
        PROFILE_DELETE("profile_delete", false, true),
        REGISTER_SUPER_PROPERTIES("register_super_properties", false, false);

        private String eventType;
        private boolean track;
        private boolean profile;

        EventType(String eventType, boolean isTrack, boolean isProfile) {
            this.eventType = eventType;
            this.track = isTrack;
            this.profile = isProfile;
        }

        public String getEventType() {
            return eventType;
        }

        public boolean isTrack() {
            return track;
        }

        public boolean isProfile() {
            return profile;
        }
    }

    private class VTrackUnsupported implements VTrack, DebugTracking {

        public VTrackUnsupported() {
        }

        @Override
        public void startUpdates() {
            // do NOTHING
        }

        @Override
        public void setEventBindings(JSONArray bindings) {
            // do NOTHING
        }

        @Override
        public void reportTrack(JSONObject eventJson) {
            // do NOTHING
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private class LifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

        public LifecycleCallbacks() {
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
            Log.i(LOGTAG, "Flush before activity being stopped.");
            mMessages.flush();
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }

    }
}

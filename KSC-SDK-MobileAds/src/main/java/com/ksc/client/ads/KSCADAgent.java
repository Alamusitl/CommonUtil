package com.ksc.client.ads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.ksc.client.ads.bean.KSCVideoAdBean;
import com.ksc.client.ads.callback.KSCAdEventListener;
import com.ksc.client.ads.config.KSCMobileAdKeyCode;
import com.ksc.client.ads.proto.KSCMobileAdProtoAPI;
import com.ksc.client.ads.proto.KSCMobileAdsProto530;
import com.ksc.client.ads.view.KSCMobileAdActivity;
import com.ksc.client.toolbox.HttpError;
import com.ksc.client.toolbox.HttpErrorListener;
import com.ksc.client.toolbox.HttpListener;
import com.ksc.client.toolbox.HttpRequestManager;
import com.ksc.client.toolbox.HttpRequestParam;
import com.ksc.client.toolbox.HttpResponse;
import com.ksc.client.util.KSCLog;
import com.ksc.client.util.KSCNetUtils;
import com.ksc.client.util.KSCPermissionUtils;
import com.ksc.client.util.KSCStorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alamusi on 2016/8/16.
 */
public class KSCADAgent {

    /**
     * 广告事件监听器
     */
    private KSCAdEventListener mEventListener;
    /**
     * 缓存广告路径，优先SD卡
     */
    private String mCacheVideoPath;

    private String mAppId;
    private String mAdSlotId;
    /**
     * 是否存在广告
     */
    private boolean mAdExist = true;

    private boolean mRequestingAd = false;

    private List<KSCVideoAdBean> mVideoList = new ArrayList<>();

    private Context mContext;

    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (mEventListener == null) {
                return;
            }
            switch (msg.what) {
                case KSCMobileAdKeyCode.KEY_VIDEO_START:
                    mEventListener.onVideoStart();
                    pushAdEvent(KSCMobileAdsProto530.Tracking.TrackingEvent.VIDEO_AD_START_VALUE, msg.arg2);
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_CLOSE:
                    mEventListener.onVideoClose(msg.arg1);
                    pushAdEvent(KSCMobileAdsProto530.Tracking.TrackingEvent.VIDEO_AD_END_VALUE, msg.arg1);
                    clearCache(false);
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_COMPLETION:
                    mEventListener.onVideoCompletion();
                    pushAdEvent(KSCMobileAdsProto530.Tracking.TrackingEvent.VIDEO_AD_END_VALUE, msg.arg1);
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_ERROR:
                    mEventListener.onVideoError((String) msg.obj);
                    pushAdEvent(KSCMobileAdsProto530.Tracking.TrackingEvent.VIDEO_AD_END_VALUE, 0);
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_H5_SHOW:
                    pushAdEvent(KSCMobileAdsProto530.Tracking.TrackingEvent.AD_EXPOSURE_VALUE, 0);
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_H5_CLICK:
                    pushAdEvent(KSCMobileAdsProto530.Tracking.TrackingEvent.AD_CLICK_VALUE, 0);
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_H5_CLOSE:
                    mEventListener.onLandingPageClose(false);
                    clearCache(false);
                    break;
                case KSCMobileAdKeyCode.KEY_DOWNLOAD_START:
                    mEventListener.onLandingPageClose(true);
                    startDownloadApk();
                    clearCache(false);
                    break;
            }
        }
    };

    public static KSCADAgent getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 初始化SDK
     *
     * @param activity      上下文
     * @param appId         MSSP分配的APPID
     * @param adSlotId      MSSP分配的AdSlotId
     * @param eventListener 广告事件监听
     */
    public void init(Activity activity, String appId, String adSlotId, KSCAdEventListener eventListener) {
        if (activity == null) {
            KSCLog.e("init sdk activity is null, please check!");
            return;
        }
        if (TextUtils.isEmpty(adSlotId) || TextUtils.isEmpty(appId)) {
            KSCLog.e("appId or adSlotId is empty, please check!");
            return;
        }
        if (eventListener == null) {
            KSCLog.e("ad sdk listener is null, please check!");
            return;
        }
        mContext = activity.getApplicationContext();
        mAppId = appId;
        mAdSlotId = adSlotId;
        mEventListener = eventListener;
        KSCBlackBoard.setTransformHandler(mHandler);
        HttpRequestManager.init();
        KSCPermissionUtils.requestNeedPermission(activity);
        mCacheVideoPath = activity.getDir("ad", Context.MODE_PRIVATE).getAbsolutePath();
        KSCLog.d("cache ad dir " + mCacheVideoPath);
        checkAppHasAd(activity, 0, true);
    }

    /**
     * 设置DebugMode，目前只作用在log
     *
     * @param debug 是否debug
     */
    public void setDebug(boolean debug) {
        KSCLog.mIsDebug = debug;
    }

    /**
     * 生命周期方法onResume
     */
    public void onResume() {
        KSCLog.d("KSCADAgent onResume");
    }

    /**
     * 生命周期方法onPause
     */
    public void onPause() {
        KSCLog.d("KSCADAgent onPause");
    }

    /**
     * 生命周期方法onDestroy
     */
    public void onDestroy() {
        KSCLog.d("KSCADAgent onDestroy");
        clearCache(true);
        HttpRequestManager.destroy();
    }

    /**
     * 授权结果回调
     *
     * @param requestCode  授权请求码，1000
     * @param permissions  需要授权权限的集合
     * @param grantResults 授权信息的集合
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        KSCPermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 播放广告视频
     *
     * @param activity 上下文
     */
    public synchronized void showAdVideo(Activity activity) {
        KSCLog.d("KSCADAgent showAdVideo");
        if (!mAdExist) {
            return;
        }
        if (mVideoList.size() == 0) {// 本地没有视频信息
            checkAppHasAd(activity, 0, false);
        } else if (mVideoList.size() > 0) {
            KSCVideoAdBean videoAdBean = mVideoList.get(0);
            String playUri;
            if (!videoAdBean.getIsCached()) {
                if (!KSCNetUtils.isNetworkAvailable(activity)) {
                    mEventListener.onNetRequestError("网络错误[网络不可用]");
                    return;
                }
                playUri = videoAdBean.getVideoUrl();
                Intent intent = new Intent(activity, KSCMobileAdActivity.class);
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_TYPE, KSCMobileAdKeyCode.VIDEO_IN_STREAM);
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_PATH, videoAdBean.getVideoUrl());
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_H5_PATH, videoAdBean.getHtml());
                activity.startActivity(intent);
            } else {
                playUri = videoAdBean.getDownloadPath();
                Intent intent = new Intent(activity, KSCMobileAdActivity.class);
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_TYPE, KSCMobileAdKeyCode.VIDEO_IN_CACHE);
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_PATH, videoAdBean.getDownloadPath());
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_H5_PATH, videoAdBean.getHtml());
                activity.startActivity(intent);
                if (mVideoList.size() == 1) {
                    checkAppHasAd(activity, 1, true);
                } else {
                    cacheAdVideo(1);
                }
            }
            KSCLog.d("current play uri=" + playUri);
        }
    }

    /**
     * 请求广告
     *
     * @param activity 上下文
     * @param index    缓存索引
     * @param isCache  是否缓存
     */
    private void checkAppHasAd(final Activity activity, final int index, final boolean isCache) {
        KSCLog.d("KSCADAgent checkAppHasAd, isCache:" + isCache);
        if (mRequestingAd) {
            KSCLog.d("sdk still requesting ad, try again later!");
            return;
        }
        mRequestingAd = true;
        HttpRequestParam requestParam = new HttpRequestParam(KSCMobileAdKeyCode.MOBILE_AD_URL, HttpRequestParam.METHOD_POST);
        requestParam.setContentType("application/x-protobuf");
        KSCMobileAdsProto530.MobadsRequest request = KSCMobileAdProtoAPI.getInstance().getRequest(activity, mAppId, mAdSlotId);
        requestParam.setBody(request.toByteArray());
        HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(HttpResponse response) {
                mRequestingAd = false;
                disposeAdResponse(activity, response, index, isCache);
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                KSCLog.e("check App Ad failed, error:" + error.getMessage());
                mRequestingAd = false;
                mEventListener.onNetRequestError("网络错误[请求广告错误]");
                mEventListener.onAdExist(false, -1);
            }
        });
    }

    /**
     * 处理请求广告的返回
     * 加入缓存列表
     * 缓存信息或播放视频
     *
     * @param activity 上下文
     * @param response 返回信息
     * @param index    缓存索引
     * @param isCache  是否缓存
     */
    private void disposeAdResponse(Activity activity, HttpResponse response, int index, boolean isCache) {
        if (response.getCode() != 200) {// 存在广告
            KSCLog.e("http response error, code=[" + response.getCode() + "]");
            mAdExist = false;
            mEventListener.onAdExist(false, -1);
            return;
        }
        List<KSCVideoAdBean> newList = KSCMobileAdProtoAPI.getInstance().getVideoList(response.getBody());
        if (newList.size() == 0) {
            KSCLog.d("server response ad list size is 0, step!");
            return;
        }
        for (KSCVideoAdBean bean : newList) {
            bean.setDownloadPath(mCacheVideoPath + File.separator + System.currentTimeMillis() + ".mp4");
            mVideoList.add(bean);
        }
        long errorCode = KSCMobileAdProtoAPI.getInstance().getErrorCode();
        if (errorCode == 0) {
            if (mVideoList.size() == 0) {
                KSCLog.d("current ad list size is 0, step!");
                mEventListener.onAdExist(false, errorCode);
                return;
            }
            mEventListener.onAdExist(true, errorCode);
            KSCVideoAdBean videoAdBean = mVideoList.get(index);
            String url = videoAdBean.getVideoUrl();
            if (url != null) {
                int netType = KSCNetUtils.getNetType(activity);
                if (isCache && netType != KSCNetUtils.NETWORK_TYPE_2G) {// 可缓存，是缓存，不是2G网络的时候缓存
                    cacheAdVideo(index);
                } else if (!isCache) {// 不缓存的时候播放流媒体
                    showAdVideo(activity);
                }
            } else {
                KSCLog.d("materialMeta video url is null, step!");
            }
        } else {
            KSCLog.e("get ad error, error code = [" + errorCode + "]");
            mEventListener.onAdExist(false, errorCode);
        }
    }

    /**
     * 缓存视频
     *
     * @param index 广告信息index索引
     */
    private void cacheAdVideo(final int index) {
        final KSCVideoAdBean adBean = mVideoList.get(index);
        KSCLog.d("KSCADAgent cacheAdVideo, url:" + adBean.getVideoUrl());
        HttpRequestParam requestParam = new HttpRequestParam(adBean.getVideoUrl());
        requestParam.setDownloadPath(mVideoList.get(index).getDownloadPath());
        HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(HttpResponse response) {
                KSCLog.d("cached video success, video path=" + adBean.getDownloadPath());
                if (mVideoList.size() > index) {
                    mVideoList.get(index).setIsCached(true);
                }
                mEventListener.onVideoCached(true);
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                KSCLog.d("cached video fail, video path=" + adBean.getDownloadPath() + ", error=" + error.getMessage());
                if (mVideoList.size() > index) {
                    mVideoList.get(index).setIsCached(false);
                }
                mEventListener.onNetRequestError("网络错误[缓存广告错误]");
                mEventListener.onVideoCached(false);
            }
        }, new Handler(Looper.getMainLooper()));
    }

    /**
     * 上报广告事件
     *
     * @param value    事件ID
     * @param position 当前视频播放进度
     */
    private synchronized void pushAdEvent(int value, int position) {
        if (mVideoList.size() == 0) {
            return;
        }
        List<String> urlList;
        if (value == KSCMobileAdsProto530.Tracking.TrackingEvent.VIDEO_AD_START_VALUE || value == KSCMobileAdsProto530.Tracking.TrackingEvent.VIDEO_AD_END_VALUE) {
            urlList = mVideoList.get(0).getTrackingUrl().get(value);
            List<String> formatUrlList = new ArrayList<>();
            for (String url : urlList) {
                String newUrl = url;
                if (url.contains("${PROGRESS}")) {
                    newUrl = url.replace("${PROGRESS}", String.valueOf(position / 1000));
                }
                formatUrlList.add(newUrl);
            }
            urlList = formatUrlList;
        } else if (value == KSCMobileAdsProto530.Tracking.TrackingEvent.AD_EXPOSURE_VALUE || value == KSCMobileAdsProto530.Tracking.TrackingEvent.AD_CLICK_VALUE) {
            urlList = mVideoList.get(0).getTrackingUrl().get(value);
        } else {
            urlList = new ArrayList<>();
        }
        for (String url : urlList) {
            HttpRequestParam request = new HttpRequestParam(url);
            HttpRequestManager.execute(request, null, null);
        }
    }

    /**
     * 清除缓存信息和缓存视频
     *
     * @param isClearAll 是否全部清理，true为全部清理，false为只清除第0个
     */
    private void clearCache(boolean isClearAll) {
        KSCLog.d("clear cached start");
        do {
            if (mVideoList.size() > 0) {
                String cachePath = mVideoList.get(0).getDownloadPath();
                mVideoList.remove(0);
                if (cachePath == null || cachePath.equals("")) {
                    continue;
                }
                File cacheFile = new File(cachePath);
                if (cacheFile.exists()) {
                    boolean result = cacheFile.delete();
                    if (result) {
                        KSCLog.d("delete video success, path=" + cachePath);
                    } else {
                        KSCLog.d("delete video fail, path=" + cachePath);
                    }
                } else {
                    KSCLog.d("cached video not exist, path=" + cachePath);
                }
            } else {
                break;
            }
        } while (isClearAll);
        if (isClearAll && mCacheVideoPath != null) {
            File file = new File(mCacheVideoPath);
            if (file.exists()) {
                KSCStorageUtils.deleteDir(file);
            }
        }
        KSCLog.d("clear cached end");
    }

    /**
     * 开启下载Service
     */
    private void startDownloadApk() {
        if (mVideoList.size() == 0) {
            return;
        }
        String url = mVideoList.get(0).getClickUrl();
        String downloadAppName = mVideoList.get(0).getBrandName() == null ? "" : mVideoList.get(0).getBrandName();
        String downloadPath;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            downloadPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Environment.DIRECTORY_DOWNLOADS;
        } else {
            downloadPath = Environment.getDataDirectory().getAbsolutePath() + File.separator + Environment.DIRECTORY_DOWNLOADS;
        }
        File file = new File(downloadPath);
        if (!file.exists()) {
            boolean result = file.mkdirs();
            KSCLog.d("mkdirs download dir " + downloadPath + "result = " + result);
        }
        downloadPath += File.separator + mVideoList.get(0).getPackageName() + ".apk";
        file = new File(downloadPath);
        if (file.exists()) {
            boolean result = file.delete();
            KSCLog.d("delete exist apk file " + downloadPath + " result = " + result);
        }
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(DownloadService.EXTRA_DOWNLOAD_URL, url);
        intent.putExtra(DownloadService.EXTRA_DOWNLOAD_PATH, downloadPath);
        intent.putExtra(DownloadService.EXTRA_DOWNLOAD_APP_NAME, downloadAppName);
        mContext.startService(intent);
        Toast.makeText(mContext, "开始下载", Toast.LENGTH_SHORT).show();
    }

    private static class SingletonHolder {
        public static final KSCADAgent INSTANCE = new KSCADAgent();
    }

}

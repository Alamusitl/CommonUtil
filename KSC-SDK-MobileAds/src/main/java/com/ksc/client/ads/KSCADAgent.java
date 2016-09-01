package com.ksc.client.ads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

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

import java.io.File;

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
    /**
     * 流媒体播放链接
     */
    private String mAdVideoUrl;
    private String mAppId;
    private String mAdSlotId;
    /**
     * 是否可以缓存，网络为2G或本地存储无效的时候为不可缓存
     */
    private boolean mCanCached = true;
    /**
     * 是否已经缓存了广告视频
     */
    private boolean mHasCached = false;
    /**
     * 是否存在广告
     */
    private boolean mAdExist = true;
    /**
     * 已缓存的视频路径List
     */
    private SparseArray<String> mCachedVideoList = new SparseArray<>();

    private String mDownloadApkUrl;
    private Context mContext;

    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (mEventListener == null) {
                return;
            }
            switch (msg.what) {
                case KSCMobileAdKeyCode.KEY_VIDEO_PREPARED:
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_PLAYING:
                    mEventListener.onVideoStart();
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_PAUSE:
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_RESUME:
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_CLOSE:
                    mEventListener.onVideoClose(msg.arg1);
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_COMPLETION:
                    mEventListener.onVideoCompletion();
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_MUTE:
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_ERROR:
                    mEventListener.onVideoError((String) msg.obj);
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_VIDEO_CLOSE:
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_SHOW_CLOSE_CONFIRM:
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_H5_CLOSE:
                    mEventListener.onLandingPageClose(false);
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_H5_CLICK:
                    mEventListener.onLandingPageClose(true);
                    deleteCachedVideo();
                    mDownloadApkUrl = (String) msg.obj;
                    break;
                case KSCMobileAdKeyCode.KEY_DOWNLOAD_START:
                    startDownloadApk(mDownloadApkUrl);
                    break;
                case KSCMobileAdKeyCode.KEY_DOWNLOAD_SUCCESS:
                    break;
                case KSCMobileAdKeyCode.KEY_DOWNLOAD_FAIL:
                    break;
            }
        }
    };

    public static KSCADAgent getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void init(Activity activity, String appId, String adSlotId, KSCAdEventListener eventListener) {
        mContext = activity.getApplicationContext();
        mAppId = appId;
        mAdSlotId = adSlotId;
        mEventListener = eventListener;
        KSCBlackBoard.setTransformHandler(mHandler);
        setCachePath(activity);
        checkAppHasAd(activity, true);
    }

    public void setDebug(boolean debug) {
        KSCLog.mIsDebug = debug;
    }

    public void onResume() {
        KSCLog.d("KSCADAgent onResume");
    }

    public void onPause() {
        KSCLog.d("KSCADAgent onPause");
    }

    public void onDestroy() {
        KSCLog.d("KSCADAgent onDestroy");
    }

    public void showAdVideo(Activity activity) {
        KSCLog.d("KSCADAgent showAdVideo");
        if (!mAdExist) {
            return;
        }
        if (!mHasCached && mAdVideoUrl == null) {// 没有缓存，没有得到视频链接
            checkAppHasAd(activity, false);
        }
        if (!mHasCached && mAdVideoUrl != null) {// 没有缓存，有视频链接
            if (!KSCNetUtils.isNetworkAvailable(activity)) {
                mEventListener.onNetRequestError("network is not available");
                return;
            }
            Intent intent = new Intent(activity, KSCMobileAdActivity.class);
            intent.putExtra(KSCMobileAdKeyCode.VIDEO_TYPE, KSCMobileAdKeyCode.VIDEO_IN_STREAM);
            intent.putExtra(KSCMobileAdKeyCode.VIDEO_PATH, mAdVideoUrl);
            activity.startActivity(intent);
        }
        if (mHasCached && mCachedVideoList.size() > 0) {// 有缓存
            Intent intent = new Intent(activity, KSCMobileAdActivity.class);
            intent.putExtra(KSCMobileAdKeyCode.VIDEO_TYPE, KSCMobileAdKeyCode.VIDEO_IN_CACHE);
            intent.putExtra(KSCMobileAdKeyCode.VIDEO_PATH, mCachedVideoList.get(0));
            activity.startActivity(intent);
            checkAppHasAd(activity, true);
        }
    }

    private void checkAppHasAd(final Activity activity, final boolean isCache) {
        KSCLog.d("KSCADAgent checkAppHasAd, isCache:" + isCache);
        HttpRequestParam requestParam = new HttpRequestParam("http://123.59.14.199:8084/api/test/9", HttpRequestParam.METHOD_POST);
        requestParam.setContentType("application/x-protobuf");
        requestParam.setBody(new String(KSCMobileAdProtoAPI.getInstance().getRequest(activity, mAppId, mAdSlotId).toByteArray()));
        HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(HttpResponse response) {
                disposeAdResponse(activity, response, isCache);
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                KSCLog.e("check App Ad failed, error:" + error.getMessage());
                mEventListener.onNetRequestError(error.getMessage());
            }
        });
    }

    private void cacheAdVideo(String url) {
        KSCLog.d("KSCADAgent cacheAdVideo, url:" + url);
        final String localVideoPath = mCacheVideoPath + File.separator + System.currentTimeMillis() + ".mp4";
        HttpRequestParam requestParam = new HttpRequestParam(url);
        requestParam.setDownloadPath(localVideoPath);
        HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(HttpResponse response) {
                mHasCached = true;
                mCachedVideoList.put(mCachedVideoList.size() + 1, localVideoPath);
                mEventListener.onVideoCached(true);
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                mEventListener.onNetRequestError(error.getMessage());
                mHasCached = false;
                mEventListener.onVideoCached(false);
            }
        }, new Handler(Looper.getMainLooper()));
    }

    private void pushAdEvent() {

    }

    private void deleteCachedVideo() {
        if (mCachedVideoList.size() > 0) {
            String cachePath = mCachedVideoList.get(0);
            File cacheFile = new File(cachePath);
            if (cacheFile.exists()) {
                boolean result = cacheFile.delete();
                if (result) {
                    mCachedVideoList.remove(0);
                }
                if (mCachedVideoList.size() == 0) {
                    mHasCached = false;
                }
            }
        }
    }

    private void disposeAdResponse(Activity activity, HttpResponse response, boolean isCache) {
        if (response.getCode() != 200) {// 存在广告
            KSCLog.e("http response error, code=" + response.getCode());
            mAdExist = false;
            mEventListener.onAdExist(false, -1);
            return;
        }
        byte[] result = response.getBody();
        KSCMobileAdProtoAPI.getInstance().setAdResponse(response.getBody());
        KSCMobileAdsProto530.MobadsResponse mobadsResponse = KSCMobileAdProtoAPI.getInstance().getAdResponse();
        String requestId = KSCMobileAdProtoAPI.getInstance().getRequestId();
        long errorCode = KSCMobileAdProtoAPI.getInstance().getErrorCode();
        if (errorCode == 0) {
            mEventListener.onAdExist(true, errorCode);
            int netType = KSCNetUtils.getNetType(activity);
            if (mCanCached && isCache && netType != KSCNetUtils.NETWORK_TYPE_2G) {// 可缓存，是缓存，不是2G网络的时候缓存
                cacheAdVideo("");
            }
            if (!isCache) {// 不缓存的时候播放流媒体
                mAdVideoUrl = "";
                showAdVideo(activity);
            }
        } else {
            KSCLog.e("get ad error, error code = " + errorCode);
            mEventListener.onAdExist(false, errorCode);
        }
    }

    private void startDownloadApk(String url) {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(DownloadService.EXTRA_DOWNLOAD_URL, url);
        intent.putExtra(DownloadService.EXTRA_DOWNLOAD_PATH, "");
        intent.putExtra(DownloadService.EXTRA_SHOW_NOTIFY, false);
        mContext.startService(intent);
    }

    /**
     * 设置缓存视频路径
     *
     * @param activity 当前的Activity
     */
    private void setCachePath(Activity activity) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            getExternalCachePath(activity);
        } else {
            getInnerCachePath(activity);
        }
    }

    /**
     * 设置SD卡的目录为缓存路径
     *
     * @param activity 当前的Activity
     */
    private void getExternalCachePath(Activity activity) {
        File cachePath = activity.getExternalCacheDir();
        if (cachePath == null) {
            getInnerCachePath(activity);
        } else {
            if (!cachePath.exists()) {
                boolean mkResult = cachePath.mkdirs();
                if (mkResult) {
                    mCacheVideoPath = cachePath.getAbsolutePath();
                } else {
                    getInnerCachePath(activity);
                }
            } else {
                mCacheVideoPath = cachePath.getAbsolutePath();
            }
        }
    }

    /**
     * 设置机器内存为缓存目录
     *
     * @param activity 当前的Activity
     */
    private void getInnerCachePath(Activity activity) {
        File cachePath = activity.getCacheDir();
        if (cachePath == null) {
            mCanCached = false;
        } else {
            if (!cachePath.exists()) {
                boolean mkResult = cachePath.mkdirs();
                if (mkResult) {
                    mCacheVideoPath = cachePath.getAbsolutePath();
                } else {
                    mCanCached = false;
                }
            } else {
                mCacheVideoPath = cachePath.getAbsolutePath();
            }
        }
    }

    private static class SingletonHolder {
        public static final KSCADAgent INSTANCE = new KSCADAgent();
    }

}

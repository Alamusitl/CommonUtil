package com.ksc.client.ads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

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

import java.io.File;
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
     * 是否可以缓存，网络为2G或本地存储无效的时候为不可缓存
     */
    private boolean mCanCached = true;
    /**
     * 是否存在广告
     */
    private boolean mAdExist = true;

    private List<KSCVideoAdBean> mVideoList;

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
                    pushAdEvent(KSCMobileAdsProto530.Tracking.TrackingEvent.VIDEO_AD_START_VALUE, msg.arg2);
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_PAUSE:
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_RESUME:
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_CLOSE:
                    mEventListener.onVideoClose(msg.arg1);
                    pushAdEvent(KSCMobileAdsProto530.Tracking.TrackingEvent.VIDEO_AD_END_VALUE, msg.arg1);
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_COMPLETION:
                    mEventListener.onVideoCompletion();
                    pushAdEvent(KSCMobileAdsProto530.Tracking.TrackingEvent.VIDEO_AD_END_VALUE, msg.arg1);
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_MUTE:
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_ERROR:
                    mEventListener.onVideoError((String) msg.obj);
                    pushAdEvent(KSCMobileAdsProto530.Tracking.TrackingEvent.VIDEO_AD_END_VALUE, 0);
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_SHOW_VIDEO_CLOSE:
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_SHOW_CLOSE_CONFIRM:
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_H5_CLOSE:
                    mEventListener.onLandingPageClose(false);
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_H5_CLICK:
                    break;
                case KSCMobileAdKeyCode.KEY_DOWNLOAD_START:
                    mEventListener.onLandingPageClose(true);
                    startDownloadApk();
                    deleteCachedVideo();
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
        if (mVideoList.size() == 0) {// 本地没有视频信息
            checkAppHasAd(activity, false);
        } else if (mVideoList.size() > 0) {
            KSCVideoAdBean videoAdBean = mVideoList.get(0);
            if (videoAdBean.getDownloadPath() == null || videoAdBean.getDownloadPath().equals("")) {// 没有缓存，有视频链接
                if (!KSCNetUtils.isNetworkAvailable(activity)) {
                    mEventListener.onNetRequestError("network is not available");
                    return;
                }
                Intent intent = new Intent(activity, KSCMobileAdActivity.class);
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_TYPE, KSCMobileAdKeyCode.VIDEO_IN_STREAM);
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_PATH, videoAdBean.getVideoUrl());
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_H5_PATH, videoAdBean.getHtml());
                activity.startActivity(intent);
            } else {// 有缓存
                Intent intent = new Intent(activity, KSCMobileAdActivity.class);
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_TYPE, KSCMobileAdKeyCode.VIDEO_IN_CACHE);
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_PATH, videoAdBean.getDownloadPath());
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_H5_PATH, videoAdBean.getHtml());
                activity.startActivity(intent);
                checkAppHasAd(activity, true);
            }
        }
    }

    private void checkAppHasAd(final Activity activity, final boolean isCache) {
        KSCLog.d("KSCADAgent checkAppHasAd, isCache:" + isCache);
        HttpRequestParam requestParam = new HttpRequestParam("http://123.59.14.199:8084/api/test/9", HttpRequestParam.METHOD_POST);
        requestParam.setContentType("application/x-protobuf");
        KSCMobileAdsProto530.MobadsRequest request = KSCMobileAdProtoAPI.getInstance().getRequest(activity, mAppId, mAdSlotId);
        requestParam.setBody(request.toByteArray());
        HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(HttpResponse response) {
                disposeAdResponse(activity, response, isCache);
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                KSCLog.e("check App Ad failed, error:" + error.getMessage());
                mEventListener.onNetRequestError("网络错误[" + error.getMessage() + "]");
            }
        });
    }

    private void disposeAdResponse(Activity activity, HttpResponse response, boolean isCache) {
        if (response.getCode() != 200) {// 存在广告
            KSCLog.e("http response error, code=[" + response.getCode() + "]");
            mAdExist = false;
            mEventListener.onAdExist(false, -1);
            return;
        }
        mVideoList = KSCMobileAdProtoAPI.getInstance().getVideoList(response.getBody());
        long errorCode = KSCMobileAdProtoAPI.getInstance().getErrorCode();
        if (errorCode == 0) {
            mEventListener.onAdExist(true, errorCode);
            if (mVideoList.size() == 0) {
                return;
            }
            KSCVideoAdBean videoAdBean = mVideoList.get(0);
            String url = videoAdBean.getVideoUrl();
            if (url != null) {
                int netType = KSCNetUtils.getNetType(activity);
                if (mCanCached && isCache && netType != KSCNetUtils.NETWORK_TYPE_2G) {// 可缓存，是缓存，不是2G网络的时候缓存
                    cacheAdVideo(url);
                } else if (!isCache) {// 不缓存的时候播放流媒体
                    showAdVideo(activity);
                }
            }
        } else {
            KSCLog.e("get ad error, error code = [" + errorCode + "]");
            mEventListener.onAdExist(false, errorCode);
        }
    }

    private void cacheAdVideo(String url) {
        KSCLog.d("KSCADAgent cacheAdVideo, url:" + url);
        final String localVideoPath = mCacheVideoPath + File.separator + System.currentTimeMillis() + ".mp4";
        HttpRequestParam requestParam = new HttpRequestParam(url);
        requestParam.setDownloadPath(localVideoPath);
        HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(HttpResponse response) {
                mVideoList.get(0).setDownloadPath(localVideoPath);
                mEventListener.onVideoCached(true);
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                mEventListener.onNetRequestError("网络错误[" + error.getMessage() + "]");
                mEventListener.onVideoCached(false);
            }
        }, new Handler(Looper.getMainLooper()));
    }

    private void pushAdEvent(int value, int position) {
        List<String> urlList;
        if (value == KSCMobileAdsProto530.Tracking.TrackingEvent.VIDEO_AD_START_VALUE) {
            urlList = mVideoList.get(0).getTrackingUrl().get(KSCMobileAdsProto530.Tracking.TrackingEvent.VIDEO_AD_START);
        } else if (value == KSCMobileAdsProto530.Tracking.TrackingEvent.VIDEO_AD_END_VALUE) {
            urlList = mVideoList.get(0).getTrackingUrl().get(KSCMobileAdsProto530.Tracking.TrackingEvent.VIDEO_AD_END);
        } else {
            urlList = mVideoList.get(0).getTrackingUrl().get(KSCMobileAdsProto530.Tracking.TrackingEvent.VIDEO_AD_FULL_SCREEN);
        }
        for (String url : urlList) {
            url = url.replace("${PROGRESS}", String.valueOf(position));
            HttpRequestParam request = new HttpRequestParam(url);
            HttpRequestManager.execute(request, null, null);
        }
    }

    private void deleteCachedVideo() {
        if (mVideoList.size() > 0) {
            String cachePath = mVideoList.get(0).getDownloadPath();
            File cacheFile = new File(cachePath);
            if (cacheFile.exists()) {
                boolean result = cacheFile.delete();
                if (result) {
                    System.out.println("delete video");
                    mVideoList.remove(0);
                }
            }
        }
    }

    private void startDownloadApk() {
        if (mVideoList.size() == 0) {
            return;
        }
        String url = mVideoList.get(0).getClickUrl();
        String downloadPath;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            downloadPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Environment.DIRECTORY_DOWNLOADS;
        } else {
            downloadPath = Environment.getDataDirectory().getAbsolutePath() + File.separator + Environment.DIRECTORY_DOWNLOADS;
        }
        downloadPath += File.separator + "download.apk";
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(DownloadService.EXTRA_DOWNLOAD_URL, url);
        intent.putExtra(DownloadService.EXTRA_DOWNLOAD_PATH, downloadPath);
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

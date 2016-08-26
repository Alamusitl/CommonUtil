package com.ksc.client.ads;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import com.ksc.client.ads.callback.KSCAdEventListener;
import com.ksc.client.ads.config.KSCMobileAdKeyCode;
import com.ksc.client.ads.proto.KSCMobileAdProtoAPI;
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
    private String mChannelId;
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
                    mEventListener.onLoadingPageClose();
                    break;
            }
        }
    };

    public static KSCADAgent getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void init(Activity activity, String appId, String channelId, String adSlotId, KSCAdEventListener eventListener) {
        mAppId = appId;
        mChannelId = channelId;
        mAdSlotId = adSlotId;
        mEventListener = eventListener;
        KSCBlackBoard.setTransformHandler(mHandler);
        setCachePath(activity);
//        checkAppHasAd(activity, true);
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        KSCLog.d("KSCADAgent onActivityResult");
        if (requestCode == KSCMobileAdKeyCode.KEY_ACTIVITY_REQUEST) {
            if (resultCode == Activity.RESULT_OK && mCachedVideoList.size() > 0) {
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
            Intent intent = new Intent(activity, KSCMobileAdActivity.class);
            intent.putExtra(KSCMobileAdKeyCode.VIDEO_TYPE, KSCMobileAdKeyCode.VIDEO_IN_STREAM);
            intent.putExtra(KSCMobileAdKeyCode.VIDEO_PATH, mAdVideoUrl);
            activity.startActivityForResult(intent, KSCMobileAdKeyCode.KEY_ACTIVITY_REQUEST);
        }
        if (mHasCached && mCachedVideoList.size() > 0) {// 有缓存
            Intent intent = new Intent(activity, KSCMobileAdActivity.class);
            intent.putExtra(KSCMobileAdKeyCode.VIDEO_TYPE, KSCMobileAdKeyCode.VIDEO_IN_CACHE);
            intent.putExtra(KSCMobileAdKeyCode.VIDEO_PATH, mCachedVideoList.get(0));
            activity.startActivityForResult(intent, KSCMobileAdKeyCode.KEY_ACTIVITY_REQUEST);
            checkAppHasAd(activity, true);
        }
    }

    private void checkAppHasAd(final Activity activity, final boolean isCache) {
        KSCLog.d("KSCADAgent checkAppHasAd, isCache:" + isCache);
        HttpRequestParam requestParam = new HttpRequestParam("http://120.92.9.140:8080/api/def", HttpRequestParam.METHOD_POST);
        requestParam.setContentType("application/x-protobuf");
        requestParam.setBody(KSCMobileAdProtoAPI.getInstance().getRequest(activity, mAppId, mChannelId).toString());
        HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(HttpResponse response) {
                if (response.getCode() == 200) {// 存在广告
                    mEventListener.onAdExist(true);
                    String url = "http://v1.mukewang.com/a45016f4-08d6-4277-abe6-bcfd5244c201/L.mp4";
                    String netType = KSCNetUtils.getNetType(activity);
                    if (netType == null) {
                        netType = "2G";
                    }
                    if (mCanCached && isCache && !netType.equals("2G")) {// 可缓存，是缓存，不是2G网络的时候缓存
                        cacheAdVideo(url);
                    }
                    if (!isCache) {// 不缓存的时候播放流媒体
                        mAdVideoUrl = url;
                        showAdVideo(activity);
                    }
                } else {// 不存在广告
                    mAdExist = false;
                    mEventListener.onAdExist(false);
                }
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
        }, new Handler(Looper.myLooper()));

    }

    private void pushAdEvent() {

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

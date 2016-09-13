package com.ksc.client.ads.bean;

import java.util.List;
import java.util.Map;

/**
 * Created by Alamusi on 2016/9/2.
 */
public class KSCVideoAdBean {

    private String mHtml;// 落地页代码
    private String mVideoUrl;// 视频链接
    private String mClickUrl;//
    private String mDownloadPath;// 视频缓存地址
    private String mBrandName;// 应用名称
    private String mPackageName;// 下载APK的包名
    private Map<Integer, List<String>> mTrackingUrl;// 广告跟踪地址集
    private boolean mIsCached = false;// 是否缓存

    public String getHtml() {
        return mHtml;
    }

    public void setHtml(String html) {
        this.mHtml = html;
    }

    public String getVideoUrl() {
        return mVideoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.mVideoUrl = videoUrl;
    }

    public String getClickUrl() {
        return mClickUrl;
    }

    public void setClickUrl(String clickUrl) {
        this.mClickUrl = clickUrl;
    }

    public String getDownloadPath() {
        return mDownloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        mDownloadPath = downloadPath;
    }

    public String getBrandName() {
        return mBrandName;
    }

    public void setBrandName(String brandName) {
        mBrandName = brandName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public Map<Integer, List<String>> getTrackingUrl() {
        return mTrackingUrl;
    }

    public void setTrackingUrl(Map<Integer, List<String>> trackingUrl) {
        this.mTrackingUrl = trackingUrl;
    }

    public boolean getIsCached() {
        return mIsCached;
    }

    public void setIsCached(boolean isCached) {
        mIsCached = isCached;
    }
}

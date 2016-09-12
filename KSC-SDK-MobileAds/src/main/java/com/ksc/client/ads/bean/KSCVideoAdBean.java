package com.ksc.client.ads.bean;

import java.util.List;
import java.util.Map;

/**
 * Created by Alamusi on 2016/9/2.
 */
public class KSCVideoAdBean {

    private String mHtml;
    private String mVideoUrl;
    private String mClickUrl;
    private String mDownloadPath;
    private String mBrandName;
    private Map<Integer, List<String>> mTrackingUrl;
    private boolean mIsCached = false;

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

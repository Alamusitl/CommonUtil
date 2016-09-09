package com.ksc.client.ads.bean;

import com.ksc.client.ads.proto.KSCMobileAdsProto530.MaterialMeta;

import java.util.List;
import java.util.Map;

/**
 * Created by Alamusi on 2016/9/2.
 */
public class KSCVideoAdBean {

    private String mAdSlotId;
    private String mAdKey;
    private String mHtml;
    private String mVideoUrl;
    private String mClickUrl;
    private String mDownloadPath;
    private String mBrandName;
    private MaterialMeta.InteractionType mInteractionType;
    private MaterialMeta.CreativeType mCreativeType;
    private Map<Integer, List<String>> mTrackingUrl;

    public String getAdSlotId() {
        return mAdSlotId;
    }

    public void setAdSlotId(String adSlotId) {
        this.mAdSlotId = adSlotId;
    }

    public String getAdKey() {
        return mAdKey;
    }

    public void setAdKey(String adKey) {
        this.mAdKey = adKey;
    }

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

    public MaterialMeta.InteractionType getInteractionType() {
        return mInteractionType;
    }

    public void setInteractionType(MaterialMeta.InteractionType interactionType) {
        this.mInteractionType = interactionType;
    }

    public MaterialMeta.CreativeType getCreativeType() {
        return mCreativeType;
    }

    public void setCreativeType(MaterialMeta.CreativeType creativeType) {
        this.mCreativeType = creativeType;
    }

    public Map<Integer, List<String>> getTrackingUrl() {
        return mTrackingUrl;
    }

    public void setTrackingUrl(Map<Integer, List<String>> trackingUrl) {
        this.mTrackingUrl = trackingUrl;
    }
}

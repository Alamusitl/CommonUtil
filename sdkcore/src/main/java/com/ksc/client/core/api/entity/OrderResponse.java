package com.ksc.client.core.api.entity;

/**
 * Created by Alamusi on 2016/6/27.
 */
public class OrderResponse {

    private String mKscAppId;// KSC APPID
    private String mChannel;// 渠道标示
    private String mKscVersion;// KSC 版本号
    private String mSdkVersion;// 渠道SDK版本号
    private String mUid;// 用户ID
    private String mKscOrder;// KSC 订单号
    private String mGameOrder;// 游戏订单号
    private String mCustomInfo;// 自定义信息
    private String mSubmitTime;// 订单创建时间
    private String mSign;// 签名
    private String mChannelProductId;

    public String getKscAppid() {
        return mKscAppId;
    }

    public void setKscAppid(String appid) {
        this.mKscAppId = appid;
    }

    public String getChannel() {
        return mChannel;
    }

    public void setChannel(String channel) {
        this.mChannel = channel;
    }

    public String getKscVersion() {
        return mKscVersion;
    }

    public void setKscVersion(String version) {
        this.mKscVersion = version;
    }

    public String getSdkVersion() {
        return mSdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.mSdkVersion = sdkVersion;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        this.mUid = uid;
    }

    public String getKscOrder() {
        return mKscOrder;
    }

    public void setKscOrder(String order) {
        this.mKscOrder = order;
    }

    public String getGameOrder() {
        return mGameOrder;
    }

    public void setGameOrder(String gameOrder) {
        this.mGameOrder = gameOrder;
    }

    public String getCustomInfo() {
        return mCustomInfo;
    }

    public void setCustomInfo(String customInfo) {
        this.mCustomInfo = customInfo;
    }

    public String getSubmitTime() {
        return mSubmitTime;
    }

    public void setSubmitTime(String submitTime) {
        this.mSubmitTime = submitTime;
    }

    public String getSign() {
        return mSign;
    }

    public void setSign(String sign) {
        this.mSign = sign;
    }

    public String getChannelProductId() {
        return mChannelProductId;
    }

    public void setChannelProductId(String channelProductId) {
        mChannelProductId = channelProductId;
    }

    @Override
    public String toString() {
        return "OrderResponse{" +
                ", mKscAppId='" + mKscAppId + '\'' +
                ", mChannel='" + mChannel + '\'' +
                ", mKscVersion='" + mKscVersion + '\'' +
                ", mSdkVersion='" + mSdkVersion + '\'' +
                ", mUid='" + mUid + '\'' +
                ", mKscOrder='" + mKscOrder + '\'' +
                ", mGameOrder='" + mGameOrder + '\'' +
                ", mCustomInfo='" + mCustomInfo + '\'' +
                ", mSubmitTime='" + mSubmitTime + '\'' +
                ", mSign='" + mSign + '\'' +
                ", mChannelProductId='" + mChannelProductId + '\'' +
                '}';
    }
}

package com.ksc.client.core.update.entity;

/**
 * Created by Alamusi on 2016/7/21.
 */
public class KSCUpdateInfo {

    private String mId;
    private String mVersion;
    private String mUrl;
    private String mType;
    private String mIsForce;
    private String mSuffix;
    private String mUpdateMsg;
    private String mMD5;

    public KSCUpdateInfo(String mId, String mVersion, String mUrl, String mType, String mIsForce, String mSuffix, String mUpdateMsg, String mMD5) {
        this.mId = mId;
        this.mVersion = mVersion;
        this.mUrl = mUrl;
        this.mType = mType;
        this.mIsForce = mIsForce;
        this.mSuffix = mSuffix;
        this.mUpdateMsg = mUpdateMsg;
        this.mMD5 = mMD5;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getVersion() {
        return mVersion;
    }

    public void setVersion(String version) {
        mVersion = version;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        this.mType = type;
    }

    public String getIsForce() {
        return mIsForce;
    }

    public void setIsForce(String isForce) {
        this.mIsForce = isForce;
    }

    public String getSuffix() {
        return mSuffix;
    }

    public void setSuffix(String suffix) {
        this.mSuffix = suffix;
    }

    public String getUpdateMsg() {
        return mUpdateMsg;
    }

    public void setUpdateMsg(String updateMsg) {
        this.mUpdateMsg = updateMsg;
    }

    public String getMD5() {
        return mMD5;
    }

    public void setMD5(String MD5) {
        this.mMD5 = MD5;
    }

    @Override
    public String toString() {
        return "KSCUpdateInfo{" +
                "mId='" + mId + '\'' +
                ", mVersion='" + mVersion + '\'' +
                ", mUrl='" + mUrl + '\'' +
                ", mType='" + mType + '\'' +
                ", mIsForce='" + mIsForce + '\'' +
                ", mSuffix='" + mSuffix + '\'' +
                ", mUpdateMsg='" + mUpdateMsg + '\'' +
                ", mMD5='" + mMD5 + '\'' +
                '}';
    }
}

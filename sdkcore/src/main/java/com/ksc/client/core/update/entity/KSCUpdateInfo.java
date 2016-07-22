package com.ksc.client.core.update.entity;

/**
 * Created by Alamusi on 2016/7/21.
 */
public class KSCUpdateInfo {

    private String mId;// 更新包的ID
    private String mVersion;// 版本号
    private String mUrl;// 更新包URL
    private String mType;// 更新类型，完整包/补丁包
    private String mIsForce;// 是否强制更新, Force/Free
    private String mSuffix; // 更新包文件类型，zip/tgz/rar/bz2
    private String mUpdateMsg;// 更新说明
    private String mMD5;// 更新包MD5

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

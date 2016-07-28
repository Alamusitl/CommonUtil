package com.ksc.client.core.update.entity;

/**
 * Created by Alamusi on 2016/7/21.
 */
public class KSCUpdateInfo {

    private String mId;// 更新包的ID
    private String mVersion;// 版本号
    private String mUrl;// 更新包URL
    private String mType;// 更新类型，完整包/补丁包
    private boolean mIsForce;// 是否强制更新, Force/Free
    private String mSuffix; // 更新包文件类型，zip/tgz/rar/bz2
    private String mUpdateMsg;// 更新说明
    private String mMD5;// 更新包MD5

    public KSCUpdateInfo(String id, String version, String url, String type, boolean isForce, String suffix, String updateMsg, String MD5) {
        this.mId = id;
        this.mVersion = version;
        this.mUrl = url;
        this.mType = type;
        this.mIsForce = isForce;
        this.mSuffix = suffix;
        this.mUpdateMsg = updateMsg;
        this.mMD5 = MD5;
    }

    public String getId() {
        return mId;
    }

    public String getVersion() {
        return mVersion;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getType() {
        return mType;
    }

    public boolean getIsForce() {
        return mIsForce;
    }

    public String getSuffix() {
        return mSuffix;
    }

    public String getUpdateMsg() {
        return mUpdateMsg;
    }

    public String getMD5() {
        return mMD5;
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

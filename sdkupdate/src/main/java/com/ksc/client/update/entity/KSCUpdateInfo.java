package com.ksc.client.update.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.ksc.client.update.KSCUpdateKeyCode;

/**
 * Created by Alamusi on 2016/7/21.
 */
public class KSCUpdateInfo implements Parcelable {

    public static final Parcelable.Creator<KSCUpdateInfo> CREATOR = new Creator<KSCUpdateInfo>() {
        @Override
        public KSCUpdateInfo createFromParcel(Parcel parcel) {
            String name = parcel.readString();
            String version = parcel.readString();
            String url = parcel.readString();
            String type = parcel.readString();
            boolean isForce = parcel.readString().equals(KSCUpdateKeyCode.KEY_TYPE_FORCE);
            String updateMsg = parcel.readString();
            int size = parcel.readInt();
            String MD5 = parcel.readString();
            return new KSCUpdateInfo(name, version, url, type, isForce, updateMsg, size, MD5);
        }

        @Override
        public KSCUpdateInfo[] newArray(int i) {
            return new KSCUpdateInfo[i];
        }
    };
    private String mName;// 更新包的ID
    private String mVersion;// 版本号
    private String mUrl;// 更新包URL
    private String mType;// 更新类型，完整包/补丁包/资源包
    private boolean mIsForce;// 是否强制更新
    private String mUpdateMsg;// 更新说明
    private int mSize;// 下载文件的大小
    private String mMD5;// 更新包MD5

    public KSCUpdateInfo(String name, String version, String url, String type, boolean isForce, String updateMsg, int size, String MD5) {
        this.mName = name;
        this.mVersion = version;
        this.mUrl = url;
        this.mType = type;
        this.mIsForce = isForce;
        this.mUpdateMsg = updateMsg;
        this.mSize = size;
        this.mMD5 = MD5;
    }

    public String getName() {
        return mName;
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

    public String getUpdateMsg() {
        return mUpdateMsg;
    }

    public String getMD5() {
        return mMD5;
    }

    public int getSize() {
        return mSize;
    }

    public void setSize(int size) {
        mSize = size;
    }

    @Override
    public String toString() {
        return "KSCUpdateInfo{" +
                "mName='" + mName + '\'' +
                ", mVersion='" + mVersion + '\'' +
                ", mUrl='" + mUrl + '\'' +
                ", mType='" + mType + '\'' +
                ", mIsForce=" + mIsForce +
                ", mUpdateMsg='" + mUpdateMsg + '\'' +
                ", mSize=" + mSize +
                ", mMD5='" + mMD5 + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mName);
        parcel.writeString(mVersion);
        parcel.writeString(mUrl);
        parcel.writeString(mType);
        if (mIsForce) {
            parcel.writeString(KSCUpdateKeyCode.KEY_TYPE_FORCE);
        } else {
            parcel.writeString(KSCUpdateKeyCode.KEY_TYPE_FREE);
        }
        parcel.writeString(mUpdateMsg);
        parcel.writeInt(mSize);
        parcel.writeString(mMD5);
    }

}

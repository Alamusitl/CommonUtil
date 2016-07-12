package com.ksc.client.core.base.entity;

/**
 * Created by Alamusi on 2016/6/21.
 */
public class RoleInfo {
    private String mUid;// 用户ID
    private String mRoleId;// 角色ID
    private String mRoleName;// 角色名字
    private String mRoleLevel;// 角色等级
    private String mRoleVip;// 角色VIP等级
    private String mZoneID;// 角色区ID
    private String mZoneName;// 角色区名称
    private String mPartyName; // 角色工会，家族的名字
    private String mBalance;// 角色账户余额
    private String mGender;// 角色性别
    private String mRoleCreateTime;// 创建角色时间
    private String mRoleLevelChangeTime;// 角色等级变化时间

    public String getUid() {
        return mUid;
    }

    public void setUid(String mUid) {
        this.mUid = mUid;
    }

    public String getRoleId() {
        return mRoleId;
    }

    public void setRoleId(String mRoleId) {
        this.mRoleId = mRoleId;
    }

    public String getRoleName() {
        return mRoleName;
    }

    public void setRoleName(String mRoleName) {
        this.mRoleName = mRoleName;
    }

    public String getRoleLevel() {
        return mRoleLevel;
    }

    public void setRoleLevel(String mRoleLevel) {
        this.mRoleLevel = mRoleLevel;
    }

    public String getRoleVip() {
        return mRoleVip;
    }

    public void setRoleVip(String mRoleVip) {
        this.mRoleVip = mRoleVip;
    }

    public String getZoneID() {
        return mZoneID;
    }

    public void setZoneID(String mZoneID) {
        this.mZoneID = mZoneID;
    }

    public String getZoneName() {
        return mZoneName;
    }

    public void setZoneName(String mZoneName) {
        this.mZoneName = mZoneName;
    }

    public String getPartyName() {
        return mPartyName;
    }

    public void setPartyName(String mPartyName) {
        this.mPartyName = mPartyName;
    }

    public String getBalance() {
        return mBalance;
    }

    public void setBalance(String mBalance) {
        this.mBalance = mBalance;
    }

    public String getGender() {
        return mGender;
    }

    public void setGender(String mGender) {
        this.mGender = mGender;
    }

    public String getRoleCreateTime() {
        return mRoleCreateTime;
    }

    public void setRoleCreateTime(String mRoleCreateTime) {
        this.mRoleCreateTime = mRoleCreateTime;
    }

    public String getRoleLevelChangeTime() {
        return mRoleLevelChangeTime;
    }

    public void setRoleLevelChangeTime(String mRoleLevelChangeTime) {
        this.mRoleLevelChangeTime = mRoleLevelChangeTime;
    }

    @Override
    public String toString() {
        return "RoleInfo{" +
                "mUid='" + mUid + '\'' +
                ", mRoleId='" + mRoleId + '\'' +
                ", mRoleName='" + mRoleName + '\'' +
                ", mRoleLevel='" + mRoleLevel + '\'' +
                ", mRoleVip='" + mRoleVip + '\'' +
                ", mZoneID='" + mZoneID + '\'' +
                ", mZoneName='" + mZoneName + '\'' +
                ", mPartyName='" + mPartyName + '\'' +
                ", mBalance='" + mBalance + '\'' +
                ", mGender='" + mGender + '\'' +
                ", mRoleCreateTime='" + mRoleCreateTime + '\'' +
                ", mRoleLevelChangeTime='" + mRoleLevelChangeTime + '\'' +
                '}';
    }
}

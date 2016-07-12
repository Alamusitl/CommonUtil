package com.ksc.client.core.base.entity;

/**
 * Created by Alamusi on 2016/6/21.
 */
public class PayInfo {
    private String mUid;// 玩家账号ID
    private String mProductId;// 商品ID
    private String mProductName;// 商品名称
    private String mProductDest;// 商品描述
    private String mProductUnit;// 商品单位，如钻石，金币
    private int mProductQuantity = 1;// 商品数量，默认为1
    private String mOrder;// 游戏订单号
    private String mCurrencyName;// 货币名称
    private String mRoleId;// 角色ID
    private String mRoleName;// 角色名字
    private String mRoleLevel;// 角色等级
    private String mRoleVip;// 角色VIP等级
    private String mZoneId;// 角色服ID
    private String mZoneName;// 角色服名称
    private String mFamilyName;// 角色帮派名称
    private String mBalance;// 角色余额
    private String mCustomInfo;// 透传信息
    private String mPrice;// 金额，单位为分
    private String mRate;// 商品比例

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        this.mUid = uid;
    }

    public String getProductId() {
        return mProductId;
    }

    public void setProductId(String productId) {
        this.mProductId = productId;
    }

    public String getProductName() {
        return mProductName;
    }

    public void setProductName(String productName) {
        this.mProductName = productName;
    }

    public String getProductDest() {
        return mProductDest;
    }

    public void setProductDest(String productDest) {
        this.mProductDest = productDest;
    }

    public String getProductUnit() {
        return mProductUnit;
    }

    public void setProductUnit(String productUnit) {
        this.mProductUnit = productUnit;
    }

    public int getProductQuantity() {
        return mProductQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.mProductQuantity = productQuantity;
    }

    public String getOrder() {
        return mOrder;
    }

    public void setOrder(String order) {
        this.mOrder = order;
    }

    public String getCurrencyName() {
        return mCurrencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.mCurrencyName = currencyName;
    }

    public String getRoleId() {
        return mRoleId;
    }

    public void setRoleId(String roleId) {
        this.mRoleId = roleId;
    }

    public String getRoleName() {
        return mRoleName;
    }

    public void setRoleName(String roleName) {
        this.mRoleName = roleName;
    }

    public String getRoleLevel() {
        return mRoleLevel;
    }

    public void setRoleLevel(String roleLevel) {
        this.mRoleLevel = roleLevel;
    }

    public String getRoleVip() {
        return mRoleVip;
    }

    public void setRoleVip(String roleVip) {
        this.mRoleVip = roleVip;
    }

    public String getZoneId() {
        return mZoneId;
    }

    public void setZoneId(String zoneId) {
        this.mZoneId = zoneId;
    }

    public String getZoneName() {
        return mZoneName;
    }

    public void setZoneName(String zoneName) {
        this.mZoneName = zoneName;
    }

    public String getFamilyName() {
        return mFamilyName;
    }

    public void setFamilyName(String familyName) {
        this.mFamilyName = familyName;
    }

    public String getBalance() {
        return mBalance;
    }

    public void setBalance(String balance) {
        this.mBalance = balance;
    }

    public String getCustomInfo() {
        return mCustomInfo;
    }

    public void setCustomInfo(String customInfo) {
        this.mCustomInfo = customInfo;
    }

    public String getPrice() {
        return mPrice;
    }

    public void setPrice(String price) {
        mPrice = price;
    }

    public String getRate() {
        return mRate;
    }

    public void setRate(String rate) {
        mRate = rate;
    }

    @Override
    public String toString() {
        return "PayInfo{" +
                "mUid='" + mUid + '\'' +
                ", mProductId='" + mProductId + '\'' +
                ", mProductName='" + mProductName + '\'' +
                ", mProductDest='" + mProductDest + '\'' +
                ", mProductUnit='" + mProductUnit + '\'' +
                ", mProductQuantity=" + mProductQuantity +
                ", mOrder='" + mOrder + '\'' +
                ", mCurrencyName='" + mCurrencyName + '\'' +
                ", mRoleId='" + mRoleId + '\'' +
                ", mRoleName='" + mRoleName + '\'' +
                ", mRoleLevel='" + mRoleLevel + '\'' +
                ", mRoleVip='" + mRoleVip + '\'' +
                ", mZoneId='" + mZoneId + '\'' +
                ", mZoneName='" + mZoneName + '\'' +
                ", mFamilyName='" + mFamilyName + '\'' +
                ", mBalance='" + mBalance + '\'' +
                ", mCustomInfo='" + mCustomInfo + '\'' +
                ", mPrice='" + mPrice + '\'' +
                ", mRate='" + mRate + '\'' +
                '}';
    }
}

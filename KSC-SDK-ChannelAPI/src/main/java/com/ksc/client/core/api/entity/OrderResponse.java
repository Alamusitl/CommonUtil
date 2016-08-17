package com.ksc.client.core.api.entity;

/**
 * Created by Alamusi on 2016/6/27.
 */
public class OrderResponse {

    private String mOrder;// KSC 订单号
    private String mAmount;// 商品价格，单位为分
    private String mProductId;// 商品ID
    private String mProductName;// 商品名称
    private String mProductDesc;// 商品描述
    private String mCustomInfo;// 自定义信息
    private String mSubmitTime;// 订单创建时间
    private String mAccessKey;// 签名方法

    public String getKscOrder() {
        return mOrder;
    }

    public void setKscOrder(String order) {
        this.mOrder = order;
    }

    public String getAmount() {
        return mAmount;
    }

    public void setAmount(String amount) {
        mAmount = amount;
    }

    public String getProductId() {
        return mProductId;
    }

    public void setProductId(String channelProductId) {
        mProductId = channelProductId;
    }

    public String getProductName() {
        return mProductName;
    }

    public void setProductName(String productName) {
        mProductName = productName;
    }

    public String getProductDesc() {
        return mProductDesc;
    }

    public void setProductDesc(String productDesc) {
        mProductDesc = productDesc;
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

    public String getAccessKey() {
        return mAccessKey;
    }

    public void setAccessKey(String accessKey) {
        mAccessKey = accessKey;
    }

    @Override
    public String toString() {
        return "OrderResponse{" +
                "mOrder='" + mOrder + '\'' +
                ", mAmount='" + mAmount + '\'' +
                ", mProductId='" + mProductId + '\'' +
                ", mProductName='" + mProductName + '\'' +
                ", mProductDesc='" + mProductDesc + '\'' +
                ", mCustomInfo='" + mCustomInfo + '\'' +
                ", mSubmitTime='" + mSubmitTime + '\'' +
                ", mAccessKey='" + mAccessKey + '\'' +
                '}';
    }
}

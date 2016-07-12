package com.ksc.client.core.base.callback;

import com.ksc.client.core.base.entity.PayInfo;

/**
 * Created by Alamusi on 2016/6/21.
 */
public interface PayCallBack {
    //支付成功回调
    void onPaySuccess(final PayInfo payInfo, int code, String msg);

    //支付失败回调
    void onPayFail(final PayInfo payInfo, int code, String msg);

    //支付取消回调
    void onPayCancel(final PayInfo payInfo, int code, String msg);

    //支付结果未知回调
    void onPayOthers(final PayInfo payInfo, int code, String msg);

    //支付过程中回调
    void onPayProgress(final PayInfo payInfo, int code, String msg);
}

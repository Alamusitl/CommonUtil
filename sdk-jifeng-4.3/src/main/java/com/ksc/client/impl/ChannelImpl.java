package com.ksc.client.impl;

import android.app.Activity;

import com.ksc.client.core.api.entity.OrderResponse;
import com.ksc.client.core.base.entity.AppInfo;
import com.ksc.client.core.base.entity.PayInfo;
import com.ksc.client.core.config.KSCStatusCode;
import com.ksc.client.core.inner.ChannelBase;
import com.mappn.sdk.Gfan;
import com.mappn.sdk.gfanpay.GfanPay;
import com.mappn.sdk.gfanpay.GfanPayResult;
import com.mappn.sdk.init.InitControl;
import com.mappn.sdk.uc.LoginControl;
import com.mappn.sdk.uc.LoginResult;

import org.json.JSONObject;

/**
 * Created by Alamusi on 2016/7/25.
 */
public class ChannelImpl extends ChannelBase {

    @Override
    public void init(Activity activity, AppInfo appInfo, JSONObject channelInfo) {
        Gfan.init(activity, new InitControl.Listener() {
            @Override
            public void onComplete() {
                mUserCallBack.onInitSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
            }
        });
    }

    @Override
    public void login(Activity activity) {
        Gfan.login(activity, new LoginControl.Listener() {
            @Override
            public void onComplete(LoginResult loginResult) {
                switch (loginResult.getLoginType()) {
                    case Quick:
                    case Common:
                        setAuthInfo(loginResult.getUserId() + "___" + loginResult.getToken() + "___" + loginResult.getUserName());
                        mUserCallBack.onLoginSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                        break;
                }
            }
        });
    }

    @Override
    public void switchAccount(Activity activity) {
        logout(activity);
        login(activity);
    }

    @Override
    public void pay(Activity activity, final PayInfo payInfo, OrderResponse response) {
        Gfan.pay(activity, response.getKscOrder(), Integer.parseInt(response.getAmount()) / 10, response.getProductName(), response.getProductDesc(), response.getCustomInfo(), new GfanPay.Listener() {
            @Override
            public void onComplete(GfanPayResult gfanPayResult) {
                switch (gfanPayResult.getStatusCode()) {
                    case Success:
                        mUserCallBack.onPaySuccess(payInfo, KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                        break;
                    case UserBreak:
                        mUserCallBack.onPayCancel(payInfo, KSCStatusCode.PAY_CANCELED, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_CANCELED));
                        break;
                    case Fail:
                        mUserCallBack.onPayFail(payInfo, KSCStatusCode.PAY_FAILED, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_FAILED));
                        break;
                }
            }
        });
    }

    @Override
    public void onDestroy(Activity activity) {
        super.onDestroy(activity);
        Gfan.destroy(activity);
    }
}

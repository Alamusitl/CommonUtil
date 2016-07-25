package com.ksc.client.impl;

import android.app.Activity;

import com.appchina.model.ErrorMsg;
import com.appchina.usersdk.Account;
import com.ksc.client.core.api.entity.OrderResponse;
import com.ksc.client.core.base.entity.AppInfo;
import com.ksc.client.core.base.entity.PayInfo;
import com.ksc.client.core.config.KSCStatusCode;
import com.ksc.client.core.inner.ChannelBase;
import com.yyh.sdk.AccountCallback;
import com.yyh.sdk.CPInfo;
import com.yyh.sdk.InitCallback;
import com.yyh.sdk.LoginCallback;
import com.yyh.sdk.PayCallback;
import com.yyh.sdk.PayParam;
import com.yyh.sdk.YYHSDKAPI;

import org.json.JSONObject;

/**
 * Created by Alamusi on 2016/7/25.
 */
public class ChannelImpl extends ChannelBase {

    @Override
    public void init(Activity activity, AppInfo appInfo, JSONObject channelInfo) {
        CPInfo cpInfo = new CPInfo();
        cpInfo.loginId = Integer.parseInt(channelInfo.optString("loginAppId"));
        cpInfo.loginKey = channelInfo.optString("loginAppKey");
        cpInfo.appid = channelInfo.optString("payAppId");
        cpInfo.appkey = channelInfo.optString("payAppKey");
        cpInfo.orientation = appInfo.getScreenOrientation();
        YYHSDKAPI.initSDKAPI(activity, cpInfo, new InitCallback() {
            @Override
            public void onFinish() {
                mUserCallBack.onInitSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
            }

            @Override
            public void onError(String s) {
                mUserCallBack.onInitFail(KSCStatusCode.INIT_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.INIT_FAIL));
            }
        }, new AccountCallback() {
            @Override
            public void onSwitchAccount(Account account, Account account1) {
                int userId = account1.userId;
                String ticket = account1.ticket;
                String userName = account.userName;
                String nickName = account.nickName;
                setAuthInfo(userId + "___" + ticket + "___" + userName + "___" + nickName);
                YYHSDKAPI.showToolbar(true);
                mUserCallBack.onSwitchAccountSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
            }

            @Override
            public void onLogout() {
                setAuthInfo(null);
                mUserCallBack.onLogoutSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
            }
        });
    }

    @Override
    public void login(Activity activity) {
        YYHSDKAPI.login(activity, new LoginCallback() {
            @Override
            public void onLoginSuccess(Activity activity, Account account) {
                int userId = account.userId;
                String ticket = account.ticket;
                String userName = account.userName;
                String nickName = account.nickName;
                setAuthInfo(userId + "___" + ticket + "___" + userName + "___" + nickName);
                YYHSDKAPI.showToolbar(true);
                mUserCallBack.onLoginSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
            }

            @Override
            public void onLoginCancel() {
                mUserCallBack.onLoginCancel(KSCStatusCode.LOGIN_CANCEL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_CANCEL));
            }

            @Override
            public void onLoginError(Activity activity, ErrorMsg errorMsg) {
                mUserCallBack.onLoginFail(KSCStatusCode.LOGIN_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_FAIL));
            }
        });
    }

    @Override
    public void logout(Activity activity) {
        logout(activity);
    }

    @Override
    public void switchAccount(Activity activity) {
        if (!YYHSDKAPI.isLogined(activity)) {
            login(activity);
        } else {
            YYHSDKAPI.openAccountCenter(activity);
        }
    }

    @Override
    public void pay(Activity activity, final PayInfo payInfo, OrderResponse response) {
        PayParam param = new PayParam(Integer.parseInt(response.getProductId()), Integer.parseInt(response.getAmount()), 1, response.getKscOrder());
        YYHSDKAPI.stratPay(activity, param, new PayCallback() {
            @Override
            public void onPayFaild(int i, String s) {
                mUserCallBack.onPayFail(payInfo, KSCStatusCode.PAY_FAILED, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_FAILED));
            }

            @Override
            public void onPaySuccess(int i, String s, String s1) {
                mUserCallBack.onPaySuccess(payInfo, KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
            }
        });
    }

    @Override
    public boolean isMethodSupport(String methodName) {
        return true;
    }

    @Override
    public void openUserCenter(Activity activity) {
        YYHSDKAPI.openAccountCenter(activity);
    }
}

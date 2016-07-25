package com.ksc.client.impl;

import android.app.Activity;
import android.content.Context;

import com.ksc.client.core.api.entity.OrderResponse;
import com.ksc.client.core.base.entity.AppInfo;
import com.ksc.client.core.base.entity.PayInfo;
import com.ksc.client.core.config.KSCSDKInfo;
import com.ksc.client.core.config.KSCStatusCode;
import com.ksc.client.core.inner.ChannelBase;
import com.ksc.client.util.KSCLog;
import com.wandoujia.mariosdk.plugin.api.api.WandouGamesApi;
import com.wandoujia.mariosdk.plugin.api.model.callback.OnLoginFinishedListener;
import com.wandoujia.mariosdk.plugin.api.model.callback.OnLogoutFinishedListener;
import com.wandoujia.mariosdk.plugin.api.model.callback.OnPayFinishedListener;
import com.wandoujia.mariosdk.plugin.api.model.callback.WandouAccountListener;
import com.wandoujia.mariosdk.plugin.api.model.model.LoginFinishType;
import com.wandoujia.mariosdk.plugin.api.model.model.LogoutFinishType;
import com.wandoujia.mariosdk.plugin.api.model.model.PayResult;
import com.wandoujia.mariosdk.plugin.api.model.model.UnverifiedPlayer;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Alamusi on 2016/7/25.
 */
public class ChannelImpl extends ChannelBase {

    private WandouGamesApi mWanDouGameApi;

    @Override
    public void onApplicationCreate(Context context) {
        super.onApplicationCreate(context);
        try {
            JSONObject channelInfo = new JSONObject(KSCSDKInfo.getChannelParam());
            WandouGamesApi.initPlugin(context, Long.parseLong(channelInfo.optString("appKey")), channelInfo.optString("appSecret"));
            mWanDouGameApi = new WandouGamesApi.Builder(context, Long.parseLong(channelInfo.optString("appKey")), channelInfo.optString("appSecret")).create();
            mWanDouGameApi.setLogEnabled(false);
        } catch (JSONException e) {
            KSCLog.e("convert channelInfo to Json fail , channelInfo : " + KSCSDKInfo.getChannelParam(), e);
        }
    }

    @Override
    public void init(Activity activity, AppInfo appInfo, JSONObject channelInfo) {
        if (mWanDouGameApi == null) {
            WandouGamesApi.initPlugin(activity.getApplicationContext(), Long.parseLong(channelInfo.optString("appKey")), channelInfo.optString("appSecret"));
            mWanDouGameApi = new WandouGamesApi.Builder(activity.getApplicationContext(), Long.parseLong(channelInfo.optString("appKey")), channelInfo.optString("appSecret")).create();
            mWanDouGameApi.setLogEnabled(false);
        }
        mWanDouGameApi.init(activity);
        mWanDouGameApi.addWandouAccountListener(new WandouAccountListener() {
            @Override
            public void onLoginSuccess() {

            }

            @Override
            public void onLoginFailed(int i, String s) {
                mUserCallBack.onLoginFail(KSCStatusCode.LOGIN_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_FAIL));
            }

            @Override
            public void onLogoutSuccess() {
                mUserCallBack.onLogoutSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
            }
        });
        mUserCallBack.onInitSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
    }

    @Override
    public void login(Activity activity) {
        mWanDouGameApi.login(new OnLoginFinishedListener() {
            @Override
            public void onLoginFinished(LoginFinishType loginFinishType, UnverifiedPlayer unverifiedPlayer) {
                if (loginFinishType == LoginFinishType.CANCEL) {
                    mUserCallBack.onLoginCancel(KSCStatusCode.LOGIN_CANCEL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_CANCEL));
                } else {
                    String uid = unverifiedPlayer.getId();
                    String token = unverifiedPlayer.getToken();
                    setAuthInfo(uid + "___" + token);
                    mUserCallBack.onLoginSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                }
            }
        });
    }

    @Override
    public void logout(Activity activity) {
        mWanDouGameApi.logout(new OnLogoutFinishedListener() {
            @Override
            public void onLoginFinished(LogoutFinishType logoutFinishType) {
                mUserCallBack.onLogoutSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
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
        if (!mWanDouGameApi.isLoginned()) {
            mUserCallBack.onPayFail(payInfo, KSCStatusCode.PAY_FAILED, "未登录，请登录");
            return;
        }
        mWanDouGameApi.pay(activity, response.getProductName(), Integer.parseInt(response.getAmount()), response.getKscOrder(), new OnPayFinishedListener() {
            @Override
            public void onPaySuccess(PayResult payResult) {
                mUserCallBack.onPaySuccess(payInfo, KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
            }

            @Override
            public void onPayFail(PayResult payResult) {
                mUserCallBack.onPayFail(payInfo, KSCStatusCode.PAY_FAILED, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_FAILED));
            }
        });
    }

    @Override
    public void onResume(Activity activity) {
        super.onResume(activity);
        if (mWanDouGameApi != null) {
            mWanDouGameApi.onResume(activity);
        }
    }

    @Override
    public void onPause(Activity activity) {
        super.onPause(activity);
        if (mWanDouGameApi != null) {
            mWanDouGameApi.onPause(activity);
        }
    }
}

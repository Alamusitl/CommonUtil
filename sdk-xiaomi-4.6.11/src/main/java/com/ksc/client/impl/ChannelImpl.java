package com.ksc.client.impl;

import android.app.Activity;
import android.os.Bundle;

import com.ksc.client.core.api.entity.OrderResponse;
import com.ksc.client.core.base.entity.AppInfo;
import com.ksc.client.core.base.entity.PayInfo;
import com.ksc.client.core.config.KSCSDKInfo;
import com.ksc.client.core.config.KSCStatusCode;
import com.ksc.client.core.inner.ChannelBase;
import com.ksc.client.util.KSCLog;
import com.xiaomi.gamecenter.sdk.GameInfoField;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;
import com.xiaomi.gamecenter.sdk.OnExitListner;
import com.xiaomi.gamecenter.sdk.OnLoginProcessListener;
import com.xiaomi.gamecenter.sdk.OnPayProcessListener;
import com.xiaomi.gamecenter.sdk.entry.MiAccountInfo;
import com.xiaomi.gamecenter.sdk.entry.MiAppInfo;
import com.xiaomi.gamecenter.sdk.entry.MiBuyInfo;
import com.xiaomi.gamecenter.sdk.entry.ScreenOrientation;

import org.json.JSONObject;

/**
 * Created by Alamusi on 2016/7/14.
 */
public class ChannelImpl extends ChannelBase {

    @Override
    public void init(Activity activity, AppInfo appInfo, JSONObject channelInfo) {
        if (!channelInfo.has("appId") || !channelInfo.has("appKey")) {
            mUserCallBack.onInitFail(KSCStatusCode.INIT_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.INIT_FAIL));
            return;
        }
        MiAppInfo miAppInfo = new MiAppInfo();
        miAppInfo.setAppId(channelInfo.optString("appId"));
        miAppInfo.setAppKey(channelInfo.optString("appKey"));
        if (KSCSDKInfo.isLandscape()) {
            miAppInfo.setOrientation(ScreenOrientation.horizontal);
        } else {
            miAppInfo.setOrientation(ScreenOrientation.vertical);
        }
        MiCommplatform.Init(activity.getApplicationContext(), miAppInfo);
        mUserCallBack.onInitSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
    }

    @Override
    public void login(Activity activity) {
        MiCommplatform.getInstance().miLogin(activity, new OnLoginProcessListener() {
            @Override
            public void finishLoginProcess(int i, MiAccountInfo miAccountInfo) {
                KSCLog.d("XiaoMi login response, code:" + i + "[0:success -102:failure -12:cancel],  uid:" + (miAccountInfo != null ? miAccountInfo.getUid() : null));
                switch (i) {
                    case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS:
                        KSCLog.d("XiaoMi login success");
                        if (miAccountInfo == null) {
                            mUserCallBack.onLoginFail(KSCStatusCode.LOGIN_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_FAIL));
                            break;
                        }
                        long uid = miAccountInfo.getUid();
                        String session = miAccountInfo.getSessionId();
                        setAuthInfo(uid + "___" + session);
                        mUserCallBack.onLoginSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                        break;
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_LOGIN_FAIL:
                        KSCLog.d("XiaoMi login fail");
                        mUserCallBack.onLoginFail(KSCStatusCode.LOGIN_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_FAIL));
                        break;
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_CANCEL:
                        KSCLog.d("XiaoMi login cancel");
                        mUserCallBack.onLoginCancel(KSCStatusCode.LOGIN_CANCEL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_CANCEL));
                        break;
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:
                        break;
                    default:
                        KSCLog.d("XiaoMi login failure");
                        mUserCallBack.onLoginFail(KSCStatusCode.LOGIN_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_FAIL));
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
        MiBuyInfo buyInfo = new MiBuyInfo();
        buyInfo.setCpOrderId(response.getKscOrder());
        buyInfo.setAmount(Integer.parseInt(response.getAmount()));
        buyInfo.setCpUserInfo(response.getCustomInfo());

        // 网游必须设置
        Bundle mBundle = new Bundle();
        mBundle.putString(GameInfoField.GAME_USER_BALANCE, payInfo.getBalance()); // 用户余额
        mBundle.putString(GameInfoField.GAME_USER_GAMER_VIP, payInfo.getRoleVip()); // vip等级
        mBundle.putString(GameInfoField.GAME_USER_LV, payInfo.getRoleLevel()); // 角色等级
        mBundle.putString(GameInfoField.GAME_USER_PARTY_NAME, payInfo.getFamilyName()); // 工会，帮派
        mBundle.putString(GameInfoField.GAME_USER_ROLE_NAME, payInfo.getRoleName()); // 角色名称
        mBundle.putString(GameInfoField.GAME_USER_ROLEID, payInfo.getRoleId()); // 角色id
        mBundle.putString(GameInfoField.GAME_USER_SERVER_NAME, payInfo.getZoneName()); // 所在服务器
        buyInfo.setExtraInfo(mBundle); // 设置用户信息

        MiCommplatform.getInstance().miUniPay(activity, buyInfo, new OnPayProcessListener() {
            @Override
            public void finishPayProcess(int i) {
                KSCLog.d("XiaoMi pay response, code:" + i + "[0:success -18003:failure -18004:cancel -18006:in progress 2090:failure(unknown)]");
                switch (i) {
                    case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS:
                        // 购买成功
                        mUserCallBack.onPaySuccess(payInfo, KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                        break;
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_CANCEL:
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_CANCEL:
                        // 取消购买
                        mUserCallBack.onPayCancel(payInfo, KSCStatusCode.PAY_CANCELED, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_CANCELED));
                        break;
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_FAILURE:
                        // 购买失败
                        mUserCallBack.onPayFail(payInfo, KSCStatusCode.PAY_FAILED, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_FAILED));
                        break;
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:
                        // 操作正在进行中
                        mUserCallBack.onPayProgress(payInfo, KSCStatusCode.PAY_PROGRESS, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_PROGRESS));
                        break;
                    default:
                        // 购买失败
                        mUserCallBack.onPayFail(payInfo, KSCStatusCode.PAY_FAILED, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_FAILED));
                        break;
                }
            }
        });
    }

    @Override
    public void exit(Activity activity) {
        MiCommplatform.getInstance().miAppExit(activity, new OnExitListner() {
            @Override
            public void onExit(int i) {
                if (i == MiErrorCode.MI_XIAOMI_EXIT) {
                    mUserCallBack.doExit();
                }
            }
        });
    }
}

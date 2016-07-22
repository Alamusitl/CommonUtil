package com.ksc.client.impl;

import android.app.Activity;

import com.flamingo.sdk.access.GPApiFactory;
import com.flamingo.sdk.access.GPExitResult;
import com.flamingo.sdk.access.GPPayResult;
import com.flamingo.sdk.access.GPSDKGamePayment;
import com.flamingo.sdk.access.GPSDKInitResult;
import com.flamingo.sdk.access.GPSDKPlayerInfo;
import com.flamingo.sdk.access.GPUploadPlayerInfoResult;
import com.flamingo.sdk.access.GPUserResult;
import com.flamingo.sdk.access.IGPExitObsv;
import com.flamingo.sdk.access.IGPPayObsv;
import com.flamingo.sdk.access.IGPSDKInitObsv;
import com.flamingo.sdk.access.IGPUploadPlayerInfoObsv;
import com.flamingo.sdk.access.IGPUserObsv;
import com.ksc.client.core.api.entity.OrderResponse;
import com.ksc.client.core.base.entity.AppInfo;
import com.ksc.client.core.base.entity.PayInfo;
import com.ksc.client.core.base.entity.RoleInfo;
import com.ksc.client.core.config.KSCStatusCode;
import com.ksc.client.core.inner.ChannelBase;
import com.ksc.client.util.KSCLog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Alamusi on 2016/7/22.
 */
public class ChannelImpl extends ChannelBase {

    @Override
    public void init(Activity activity, AppInfo appInfo, final String channelInfo) {
        String appId = null;
        String appKey = null;
        try {
            JSONObject tmp = new JSONObject(channelInfo);
            appId = tmp.optString("appId");
            appKey = tmp.optString("appKey");
        } catch (JSONException e) {
            KSCLog.e("JSONException channelInfo: " + channelInfo, e);
        }
        GPApiFactory.getGPApi().setLogOpen(false);
        GPApiFactory.getGPApi().initSdk(activity, appId, appKey, new IGPSDKInitObsv() {
            @Override
            public void onInitFinish(GPSDKInitResult gpsdkInitResult) {
                KSCLog.d("GPSDKInitResult mInitErrCode: " + gpsdkInitResult.mInitErrCode);
                switch (gpsdkInitResult.mInitErrCode) {
                    case GPSDKInitResult.GPInitErrorCodeConfig:
                        mUserCallBack.onInitFail(KSCStatusCode.INIT_FAIL, "初始化配置错误");
                        break;
                    case GPSDKInitResult.GPInitErrorCodeNeedUpdate:
                        mUserCallBack.onInitFail(KSCStatusCode.INIT_FAIL, "游戏需要更新");
                        break;
                    case GPSDKInitResult.GPInitErrorCodeNet:
                        mUserCallBack.onInitFail(KSCStatusCode.INIT_FAIL, "网络错误");
                        break;
                    case GPSDKInitResult.GPInitErrorCodeNone:
                        mUserCallBack.onInitSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                        break;
                }
            }
        });
    }

    @Override
    public void login(Activity activity) {
        GPApiFactory.getGPApi().login(activity, new IGPUserObsv() {
            @Override
            public void onFinish(GPUserResult gpUserResult) {
                switch (gpUserResult.mErrCode) {
                    case GPUserResult.USER_RESULT_LOGIN_FAIL:
                        mUserCallBack.onLogoutFail(KSCStatusCode.LOGIN_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_FAIL));
                        break;
                    case GPUserResult.USER_RESULT_LOGIN_SUCC:
                        String token = GPApiFactory.getGPApi().getLoginToken();
                        String uid = GPApiFactory.getGPApi().getLoginUin();
                        String nickName = GPApiFactory.getGPApi().getAccountName();
                        setAuthInfo(uid + "___" + nickName + "___" + token);
                        mUserCallBack.onLoginSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                        break;
                }
            }
        });
    }

    @Override
    public void logout(Activity activity) {
        GPApiFactory.getGPApi().logout();
        setAuthInfo(null);
        mUserCallBack.onLogoutSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
    }

    @Override
    public void switchAccount(Activity activity) {
        logout(activity);
        login(activity);
    }

    @Override
    public void pay(Activity activity, final PayInfo payInfo, OrderResponse response) {
        GPSDKGamePayment payment = new GPSDKGamePayment();
        payment.mItemName = response.getProductName();
        payment.mPaymentDes = response.getProductDesc();
        payment.mItemPrice = Integer.parseInt(response.getAmount());
        payment.mItemOrigPrice = Integer.parseInt(response.getAmount());
        payment.mCurrentActivity = activity;
        payment.mItemCount = 1;
        payment.mSerialNumber = response.getKscOrder();
        payment.mItemId = response.getProductId();
        payment.mReserved = response.getCustomInfo();
        GPApiFactory.getGPApi().buy(payment, new IGPPayObsv() {
            @Override
            public void onPayFinish(GPPayResult gpPayResult) {
                switch (gpPayResult.mErrCode) {
                    case GPPayResult.GPSDKPayResultCodeBackgroundSucceed:
                    case GPPayResult.GPSDKPayResultCodeSucceed:
                        mUserCallBack.onPaySuccess(payInfo, KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                        break;
                    case GPPayResult.GPSDKPayResultCodeCancel:
                        mUserCallBack.onPayCancel(payInfo, KSCStatusCode.PAY_CANCELED, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_CANCELED));
                        break;
                    case GPPayResult.GPSDKPayResultCodePayBackground:
                        mUserCallBack.onPayProgress(payInfo, KSCStatusCode.PAY_PROGRESS, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_PROGRESS));
                        break;
                    case GPPayResult.GPSDKPayResultCodeBackgroundTimeOut:
                        mUserCallBack.onPayOthers(payInfo, KSCStatusCode.PAY_RESULT_UNKNOWN, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_RESULT_UNKNOWN));
                        break;
                    case GPPayResult.GPSDKPayResultCodeNotEnough:
                        KSCLog.w("余额不足");
                    case GPPayResult.GPSDKPayResultCodeOtherError:
                        KSCLog.w("其他错误");
                    case GPPayResult.GPSDKPayResultCodePayForbidden:
                        KSCLog.w("用户被限制");
                    case GPPayResult.GPSDKPayResultCodePayHadFinished:
                        KSCLog.w("该订单已经完成");
                    case GPPayResult.GPSDKPayResultCodeServerError:
                        KSCLog.w("服务器错误");
                    case GPPayResult.GPSDKPayResultNotLogined:
                        KSCLog.w("无登陆");
                    case GPPayResult.GPSDKPayResultParamWrong:
                        KSCLog.w("参数错误");
                    case GPPayResult.GPSDKPayResultCodeLoginOutofDate:
                        KSCLog.w("登录态失效");
                    default:
                        KSCLog.w("未知错误");
                        mUserCallBack.onPayFail(payInfo, KSCStatusCode.PAY_FAILED, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_FAILED));
                        break;
                }
            }
        });
    }

    @Override
    public void exit(Activity activity) {
        GPApiFactory.getGPApi().exit(new IGPExitObsv() {
            @Override
            public void onExitFinish(GPExitResult gpExitResult) {
                switch (gpExitResult.mResultCode) {
                    case GPExitResult.GPSDKExitResultCodeExitGame:
                        mUserCallBack.doExit();
                        break;
                }
            }
        });
    }

    @Override
    public void onEnterGame(RoleInfo roleInfo) {
        GPSDKPlayerInfo gpsdkPlayerInfo = new GPSDKPlayerInfo();
        gpsdkPlayerInfo.mGameLevel = roleInfo.getRoleLevel();
        gpsdkPlayerInfo.mPlayerId = roleInfo.getRoleId();
        gpsdkPlayerInfo.mPlayerNickName = roleInfo.getRoleName();
        gpsdkPlayerInfo.mServerId = roleInfo.getZoneID();
        gpsdkPlayerInfo.mServerName = roleInfo.getZoneName();
        GPApiFactory.getGPApi().uploadPlayerInfo(gpsdkPlayerInfo, new IGPUploadPlayerInfoObsv() {
            @Override
            public void onUploadFinish(GPUploadPlayerInfoResult gpUploadPlayerInfoResult) {
                switch (gpUploadPlayerInfoResult.mResultCode) {
                    case GPUploadPlayerInfoResult.GPSDKUploadSuccess:
                        KSCLog.d("upload info success");
                        break;
                    case GPUploadPlayerInfoResult.GPSDKUploadFail:
                        KSCLog.d("upload info fail");
                        break;
                }
            }
        });
    }
}

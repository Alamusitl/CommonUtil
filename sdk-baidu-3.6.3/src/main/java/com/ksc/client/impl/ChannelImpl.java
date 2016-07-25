package com.ksc.client.impl;

import android.app.Activity;

import com.baidu.gamesdk.ActivityAdPage;
import com.baidu.gamesdk.ActivityAnalytics;
import com.baidu.gamesdk.BDGameSDK;
import com.baidu.gamesdk.BDGameSDKSetting;
import com.baidu.gamesdk.IResponse;
import com.baidu.gamesdk.OnGameExitListener;
import com.baidu.gamesdk.ResultCode;
import com.baidu.platformsdk.PayOrderInfo;
import com.ksc.client.core.api.entity.OrderResponse;
import com.ksc.client.core.base.entity.AppInfo;
import com.ksc.client.core.base.entity.PayInfo;
import com.ksc.client.core.config.KSCSDKInfo;
import com.ksc.client.core.config.KSCStatusCode;
import com.ksc.client.core.inner.ChannelBase;
import com.ksc.client.util.KSCLog;

import org.json.JSONObject;

/**
 * Created by Alamusi on 2016/7/25.
 */
public class ChannelImpl extends ChannelBase {

    private ActivityAdPage mActivityAdPage;
    private ActivityAnalytics mActivityAnalytics;

    @Override
    public void init(final Activity activity, AppInfo appInfo, JSONObject channelInfo) {
        BDGameSDKSetting gameSDKSetting = new BDGameSDKSetting();
        gameSDKSetting.setAppID(channelInfo.optInt("appId"));
        gameSDKSetting.setAppKey(channelInfo.optString("appKey"));
        gameSDKSetting.setDomain(BDGameSDKSetting.Domain.RELEASE);
        BDGameSDKSetting.Orientation orientation = BDGameSDKSetting.Orientation.PORTRAIT;
        if (KSCSDKInfo.isLandscape()) {
            orientation = BDGameSDKSetting.Orientation.LANDSCAPE;
        }
        gameSDKSetting.setOrientation(orientation);
        BDGameSDK.init(activity, gameSDKSetting, new IResponse<Void>() {
            @Override
            public void onResponse(int i, String s, Void aVoid) {
                switch (i) {
                    case ResultCode.INIT_SUCCESS:
                        BDGameSDK.getAnnouncementInfo(activity);
                        mUserCallBack.onInitSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                        break;
                    case ResultCode.INIT_FAIL:
                        mUserCallBack.onInitFail(KSCStatusCode.INIT_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.INIT_FAIL));
                    default:
                        break;
                }
            }
        });
        BDGameSDK.setSuspendWindowChangeAccountListener(new IResponse<Void>() {
            @Override
            public void onResponse(int i, String s, Void aVoid) {
                switch (i) {
                    case ResultCode.LOGIN_SUCCESS:
                        logout(activity);
                        String uid = BDGameSDK.getLoginUid();
                        String token = BDGameSDK.getLoginAccessToken();
                        setAuthInfo(uid + token);
                        mUserCallBack.onSwitchAccountSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BDGameSDK.showFloatView(activity);
                            }
                        });
                        break;
                    case ResultCode.LOGIN_CANCEL:
                    case ResultCode.LOGIN_FAIL:
                    default:
                        KSCLog.d("BaiDu switch account fail or cancel");
                        break;
                }
            }
        });
        BDGameSDK.setSessionInvalidListener(new IResponse<Void>() {
            @Override
            public void onResponse(int i, String s, Void aVoid) {
                if (i == ResultCode.SESSION_INVALID) {
                    logout(activity);
                    login(activity);
                }
            }
        });
        mActivityAnalytics = new ActivityAnalytics(activity);
        mActivityAdPage = new ActivityAdPage(activity, new ActivityAdPage.Listener() {
            @Override
            public void onClose() {
                KSCLog.d("BaiDu AdPage close, continue game");
            }
        });
        BDGameSDK.setSupportScreenRecord(false);
    }

    @Override
    public void login(final Activity activity) {
        BDGameSDK.login(new IResponse<Void>() {
            @Override
            public void onResponse(int i, String s, Void aVoid) {
                switch (i) {
                    case ResultCode.LOGIN_SUCCESS:
                        String uid = BDGameSDK.getLoginUid();
                        String token = BDGameSDK.getLoginAccessToken();
                        setAuthInfo(uid + token);
                        mUserCallBack.onLoginSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BDGameSDK.showFloatView(activity);
                            }
                        });
                        break;
                    case ResultCode.LOGIN_CANCEL:
                        mUserCallBack.onLoginCancel(KSCStatusCode.LOGIN_CANCEL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_CANCEL));
                        break;
                    case ResultCode.LOGIN_FAIL:
                    default:
                        mUserCallBack.onLoginFail(KSCStatusCode.LOGIN_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_FAIL));
                        break;
                }
            }
        });
    }

    @Override
    public void logout(Activity activity) {
        super.logout(activity);
        BDGameSDK.closeFloatView(activity);
    }

    @Override
    public void switchAccount(Activity activity) {
        logout(activity);
        login(activity);
    }

    @Override
    public void pay(Activity activity, final PayInfo payInfo, OrderResponse response) {
        if (!BDGameSDK.isLogined()) {
            mUserCallBack.onPayFail(payInfo, KSCStatusCode.PAY_FAILED, "BaiDu account is not login, please login!");
            return;
        }
        PayOrderInfo payOrderInfo = new PayOrderInfo();
        payOrderInfo.setCooperatorOrderSerial(response.getKscOrder());
        payOrderInfo.setProductName(response.getProductName());
        payOrderInfo.setTotalPriceCent(Long.parseLong(response.getAmount()));// 单位为分
        payOrderInfo.setRatio(1);
        payOrderInfo.setExtInfo(response.getCustomInfo());
        BDGameSDK.pay(payOrderInfo, null, new IResponse<PayOrderInfo>() {
            @Override
            public void onResponse(int i, String s, PayOrderInfo payOrderInfo) {
                switch (i) {
                    case ResultCode.PAY_SUCCESS:
                        mUserCallBack.onPaySuccess(payInfo, KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                        break;
                    case ResultCode.PAY_CANCEL:
                        mUserCallBack.onPayCancel(payInfo, KSCStatusCode.PAY_CANCELED, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_CANCELED));
                        break;
                    case ResultCode.PAY_SUBMIT_ORDER:
                        mUserCallBack.onPayProgress(payInfo, KSCStatusCode.PAY_PROGRESS, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_PROGRESS));
                        break;
                    case ResultCode.PAY_FAIL:
                    default:
                        mUserCallBack.onPayFail(payInfo, KSCStatusCode.PAY_FAILED, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_FAILED));
                        break;
                }
            }
        });
    }

    @Override
    public void onResume(Activity activity) {
        super.onResume(activity);
        if (mActivityAdPage != null) {
            mActivityAdPage.onResume();
        }
        if (mActivityAnalytics != null) {
            mActivityAnalytics.onResume();
        }
        if (BDGameSDK.isLogined()) {
            BDGameSDK.onResume(activity);
            BDGameSDK.showFloatView(activity);
        }
    }

    @Override
    public void onPause(Activity activity) {
        super.onPause(activity);
        if (mActivityAdPage != null) {
            mActivityAdPage.onPause();
        }
        if (mActivityAnalytics != null) {
            mActivityAnalytics.onPause();
        }
        if (BDGameSDK.isLogined()) {
            BDGameSDK.onPause(activity);
            BDGameSDK.closeFloatView(activity);
        }
    }

    @Override
    public void onStop(Activity activity) {
        super.onStop(activity);
        if (mActivityAdPage != null) {
            mActivityAdPage.onStop();
        }
        if (BDGameSDK.isLogined()) {
            BDGameSDK.closeFloatView(activity);
        }
    }

    @Override
    public void onDestroy(Activity activity) {
        super.onDestroy(activity);
        if (mActivityAdPage != null) {
            mActivityAdPage.onDestroy();
        }
    }

    @Override
    public void exit(Activity activity) {
        BDGameSDK.gameExit(activity, new OnGameExitListener() {
            @Override
            public void onGameExit() {
                mUserCallBack.doExit();
            }
        });
    }

    @Override
    public boolean isMethodSupport(String methodName) {
        if (methodName.equals("openUserCenter")) {
            return false;
        } else if (methodName.equals("switchAccount")) {
            return true;
        }
        return false;
    }

}

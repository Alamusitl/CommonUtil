package com.ksc.client.impl;

import android.app.Activity;

import com.anzhi.usercenter.sdk.AnzhiUserCenter;
import com.anzhi.usercenter.sdk.inter.AnzhiCallback;
import com.anzhi.usercenter.sdk.inter.AzOutGameInter;
import com.anzhi.usercenter.sdk.inter.InitSDKCallback;
import com.anzhi.usercenter.sdk.inter.KeybackCall;
import com.anzhi.usercenter.sdk.item.CPInfo;
import com.anzhi.usercenter.sdk.item.UserGameInfo;
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
 * Created by Alamusi on 2016/7/13.
 */
public class ChannelImpl extends ChannelBase {

    private static final String KEY_LOGIN = "key_login";// 登录的key
    private static final String KEY_LOGOUT = "key_logout";// 登出的KEY
    private static final String KEY_PAY = "key_pay";// 支付的key
    private static final String JS_CALLBACK_KEY = "callback_key";
    /**
     * 初始化完成的回调
     */
    InitSDKCallback mInitSDKCallback = new InitSDKCallback() {
        @Override
        public void initSdkCallcack() {
            mUserCallBack.onInitSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
        }
    };
    private String mGameName;
    private String mUid;
    private String mNickName;
    private Activity mCurrentActivity;
    private AnzhiUserCenter mAnZhiUserCenter;
    /**
     * 退出游戏的回调接口
     */
    AzOutGameInter mOutGameInter = new AzOutGameInter() {
        @Override
        public void azOutGameInter(int i) {
            switch (i) {
                case AzOutGameInter.KEY_CANCEL:
                    break;
                case AzOutGameInter.KEY_OUT_GAME:
                    mAnZhiUserCenter.removeFloaticon(mCurrentActivity.getApplicationContext());
                    mUserCallBack.doExit();
                    break;
                default:
                    break;
            }
        }
    };
    private PayInfo mPayInfo;
    /**
     * 登录，登出，支付通知
     */
    AnzhiCallback mCallback = new AnzhiCallback() {
        @Override
        public void onCallback(CPInfo cpInfo, final String result) {
            KSCLog.e("AnZhiCallback result " + result);
            try {
                JSONObject json = new JSONObject(result);
                String key = json.optString(JS_CALLBACK_KEY);
                if (KEY_PAY.equals(key)) {// 支付结果通知
                    int code = json.optInt("code");
                    if (code == 200 || code == 201) {
                        mUserCallBack.onPaySuccess(mPayInfo, KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                    } else {
                        mUserCallBack.onPayFail(mPayInfo, KSCStatusCode.PAY_FAILED, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_FAILED));
                    }
                } else if (KEY_LOGOUT.equals(key)) {// 切换或退出账号的通知
                    mAnZhiUserCenter.dismissFloaticon();
                    setAuthInfo(null);
                    mUserCallBack.onLogoutSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                } else if (KEY_LOGIN.equals(key)) {// 登录游戏的方法
                    int code = json.optInt("code");
                    String sid = json.optString("sid");
                    mUid = json.optString("uid");
                    mNickName = json.optString("nick_name");
                    if (code == 200) {
                        setAuthInfo(mUid + "___" + sid);
                        mUserCallBack.onLoginSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                        if (mAnZhiUserCenter != null) {
                            mAnZhiUserCenter.showFloaticon();
                        }
                    } else {
                        mUserCallBack.onLoginFail(KSCStatusCode.LOGIN_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_FAIL));
                    }
                }
            } catch (JSONException e) {
                KSCLog.e("JSONException : " + result, e);
            }
        }
    };
    /**
     * 页面返回回调 ，包括登录、支付等
     */
    KeybackCall mKeyCall = new KeybackCall() {
        @Override
        public void KeybackCall(String s) {
            KSCLog.i("Call == " + s);
            if (s.toLowerCase().equals("login")) {
                mUserCallBack.onLoginCancel(KSCStatusCode.LOGIN_CANCEL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_CANCEL));
            } else if (s.toLowerCase().equals("gamePay")) {
                mUserCallBack.onPayCancel(mPayInfo, KSCStatusCode.PAY_CANCELED, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_CANCELED));
            }
        }
    };

    @Override
    public void init(Activity activity, AppInfo appInfo, String channelInfo) {
        mCurrentActivity = activity;
        JSONObject data;
        try {
            data = new JSONObject(channelInfo);
        } catch (JSONException e) {
            KSCLog.e("channel info can not be format json." + channelInfo, e);
            return;
        }
        String appKey = data.optString("appKey");
        String appSecret = data.optString("appSecret");
        mGameName = data.optString("gameName");
        CPInfo info = new CPInfo();
        info.setOpenOfficialLogin(false);
        info.setAppKey(appKey);
        info.setSecret(appSecret);
        info.setChannel("AnZhi");
        info.setGameName(mGameName);
        mAnZhiUserCenter = AnzhiUserCenter.getInstance();
        mAnZhiUserCenter.setKeybackCall(mKeyCall);
        mAnZhiUserCenter.azinitSDK(activity, info, mInitSDKCallback, mOutGameInter);
        mAnZhiUserCenter.setCallback(mCallback);
        mAnZhiUserCenter.setActivityOrientation(appInfo.getScreenOrientation());
    }

    @Override
    public void login(Activity activity) {
        if (mAnZhiUserCenter != null) {
            mAnZhiUserCenter.login(activity, true);
        }
    }

    @Override
    public void logout(Activity activity) {
        if (mAnZhiUserCenter != null) {
            mAnZhiUserCenter.logout(activity);
        }
    }

    @Override
    public void switchAccount(Activity activity) {
        logout(activity);
        login(activity);
    }

    @Override
    public void pay(Activity activity, PayInfo payInfo, OrderResponse response) {
        mPayInfo = payInfo;
        final int amount = Integer.parseInt(payInfo.getPrice());
        final String productName = payInfo.getProductName();
        final String payDes = payInfo.getProductDest();
        final String order = response.getKscOrder();
        final String payDesWithOrder = order + "___" + payDes;

        mAnZhiUserCenter.pay(activity, 0, amount / 100, productName, payDesWithOrder);
    }

    @Override
    public void onBackPressed(Activity activity) {
        super.onBackPressed(activity);
        mAnZhiUserCenter.azoutGame(true);
    }

    @Override
    public void onPause(Activity activity) {
        super.onPause(activity);
        if (mAnZhiUserCenter != null) {
            mAnZhiUserCenter.dismissFloaticon();
        }
    }

    @Override
    public void onResume(Activity activity) {
        super.onResume(activity);
        if (mAnZhiUserCenter != null) {
            mAnZhiUserCenter.showFloaticon();
        }
    }

    @Override
    public void exit(Activity activity) {
        if (mAnZhiUserCenter != null) {
            mAnZhiUserCenter.azoutGame(true);
        } else {
            super.exit(activity);
        }
    }

    @Override
    public void onEnterGame(RoleInfo roleInfo) {
        super.onEnterGame(roleInfo);
        UserGameInfo userGameInfo = new UserGameInfo();
        userGameInfo.setNickName(mNickName);
        userGameInfo.setUid(mUid);
        userGameInfo.setAppName(mGameName);
        userGameInfo.setGameArea(roleInfo.getZoneName());
        userGameInfo.setGameLevel(roleInfo.getRoleLevel());
        userGameInfo.setUserRole(roleInfo.getRoleName());
        userGameInfo.setMemo("");
        mAnZhiUserCenter.submitGameInfo(mCurrentActivity.getApplicationContext(), userGameInfo);
    }

    @Override
    public boolean isMethodSupport(String methodName) {
        return true;
    }

    @Override
    public void openUserCenter(Activity activity) {
        if (mAnZhiUserCenter != null) {
            mAnZhiUserCenter.viewUserInfo(activity);
        }
    }
}

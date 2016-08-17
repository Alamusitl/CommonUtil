package com.ksc.client.impl;

import android.app.Activity;
import android.content.pm.ActivityInfo;

import com.ksc.client.core.api.entity.OrderResponse;
import com.ksc.client.core.base.entity.AppInfo;
import com.ksc.client.core.base.entity.PayInfo;
import com.ksc.client.core.base.entity.RoleInfo;
import com.ksc.client.core.config.KSCStatusCode;
import com.ksc.client.core.inner.ChannelBase;
import com.ksc.client.util.KSCLog;

import org.json.JSONObject;

import cn.uc.gamesdk.UCCallbackListener;
import cn.uc.gamesdk.UCCallbackListenerNullException;
import cn.uc.gamesdk.UCFloatButtonCreateException;
import cn.uc.gamesdk.UCGameSDK;
import cn.uc.gamesdk.UCGameSDKStatusCode;
import cn.uc.gamesdk.UCLogLevel;
import cn.uc.gamesdk.UCLoginFaceType;
import cn.uc.gamesdk.UCOrientation;
import cn.uc.gamesdk.info.FeatureSwitch;
import cn.uc.gamesdk.info.GameParamInfo;
import cn.uc.gamesdk.info.OrderInfo;
import cn.uc.gamesdk.info.PaymentInfo;

/**
 * Created by Alamusi on 2016/7/20.
 */
public class ChannelImpl extends ChannelBase {

    private UCGameSDK mUCGameSDK;

    @Override
    public void init(final Activity activity, AppInfo appInfo, JSONObject channelInfo) {
        try {
            int gameId = channelInfo.optInt("gameId");
            int cpId = channelInfo.optInt("cpId");
            UCOrientation ucOrientation;
            if (appInfo.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                ucOrientation = UCOrientation.PORTRAIT;
            } else {
                ucOrientation = UCOrientation.LANDSCAPE;
            }
            GameParamInfo gp = new GameParamInfo();
            gp.setGameId(gameId);
            gp.setCpId(cpId);
            gp.setServerId(0);
            gp.setFeatureSwitch(new FeatureSwitch(true, false));
            mUCGameSDK = UCGameSDK.defaultSDK();
            mUCGameSDK.setLogoutNotifyListener(new UCCallbackListener<String>() {
                @Override
                public void callback(int code, String msg) {
                    if (code == UCGameSDKStatusCode.SUCCESS) {
                        setAuthInfo(null);
                        destroyFloatButton(activity);
                        mUserCallBack.onLogoutSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                    } else if (code == UCGameSDKStatusCode.FAIL) {
                        mUserCallBack.onLogoutFail(KSCStatusCode.LOGOUT_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGOUT_FAIL));
                    } else if (code == UCGameSDKStatusCode.NO_INIT) {
                        mUserCallBack.onLogoutFail(KSCStatusCode.LOGOUT_FAIL, "未初始化：" + msg);
                    } else if (code == UCGameSDKStatusCode.NO_LOGIN) {
                        mUserCallBack.onLogoutFail(KSCStatusCode.LOGOUT_FAIL, "未登录:" + msg);
                    }
                }
            });
            mUCGameSDK.setOrientation(ucOrientation);
            mUCGameSDK.setLoginUISwitch(UCLoginFaceType.USE_WIDGET);
            mUCGameSDK.initSDK(activity, UCLogLevel.DEBUG, true, gp, new UCCallbackListener<String>() {
                @Override
                public void callback(int code, String msg) {
                    KSCLog.d("init callback,code:" + code + " ,msg:" + msg);
                    switch (code) {
                        case UCGameSDKStatusCode.SUCCESS:
                            mUserCallBack.onInitSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                            break;
                        case UCGameSDKStatusCode.INIT_FAIL:
                            mUserCallBack.onInitFail(KSCStatusCode.INIT_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.INIT_FAIL));
                            break;
                        default:
                            break;
                    }
                }
            });
        } catch (UCCallbackListenerNullException e) {
            KSCLog.e("Init UC Callback Listener Null Exception", e);
        }
    }

    @Override
    public void login(final Activity activity) {
        try {
            mUCGameSDK.login(activity, new UCCallbackListener<String>() {
                @Override
                public void callback(int code, String msg) {
                    KSCLog.d("login callback,code:" + code + ",msg:" + msg);
                    if (code == UCGameSDKStatusCode.SUCCESS) {
                        String sid = mUCGameSDK.getSid();
                        setAuthInfo(sid);
                        createFloatButton(activity);
                        showFloatButton(activity);
                        mUserCallBack.onLoginSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                    } else if (code == UCGameSDKStatusCode.NO_INIT) {
                        msg = "没有初始化就进行登录调用，需要游戏调用SDK初始化方法" + msg;
                        mUserCallBack.onLoginFail(KSCStatusCode.NOT_INIT, msg);
                    } else if (code == UCGameSDKStatusCode.LOGIN_EXIT) {
                        if (!isLogin()) {
                            mUserCallBack.onLoginCancel(KSCStatusCode.LOGIN_CANCEL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_CANCEL));
                        }
                    }
                }

            });
        } catch (UCCallbackListenerNullException e) {
            KSCLog.e("login UCCallbackListenerNullException", e);
        }
    }

    @Override
    public void logout(Activity activity) {
        try {
            mUCGameSDK.logout();
        } catch (UCCallbackListenerNullException e) {
            KSCLog.e("UC Logout callback null exception", e);
        }
        mUserCallBack.onLogoutSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
    }

    @Override
    public void switchAccount(Activity activity) {
        if (!isLogin()) {
            login(activity);
        } else {
            logout(activity);
            login(activity);
        }
    }

    @Override
    public void pay(Activity activity, final PayInfo payInfo, OrderResponse response) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setServerId(0);
        paymentInfo.setRoleId(payInfo.getRoleId());
        paymentInfo.setRoleName(payInfo.getRoleName());
        paymentInfo.setAmount(Float.parseFloat(response.getAmount()) / 100);
        paymentInfo.setCustomInfo(response.getCustomInfo());
        paymentInfo.setTransactionNumCP(response.getKscOrder());
        try {
            mUCGameSDK.pay(activity, paymentInfo, new UCCallbackListener<OrderInfo>() {
                @Override
                public void callback(int i, OrderInfo orderInfo) {
                    switch (i) {
                        case UCGameSDKStatusCode.SUCCESS:
                            KSCLog.d("渠道订单提交成功");
                            break;
                        case UCGameSDKStatusCode.NO_INIT:
                            mUserCallBack.onPayFail(payInfo, KSCStatusCode.PAY_FAILED, "未初始化");
                            break;
                        case UCGameSDKStatusCode.PAY_USER_EXIT:
                            mUserCallBack.onPayOthers(payInfo, KSCStatusCode.PAY_RESULT_UNKNOWN, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_RESULT_UNKNOWN));
                            break;
                    }
                }
            });
        } catch (UCCallbackListenerNullException e) {
            KSCLog.e("UC pay callback exception", e);
        }
    }

    @Override
    public void exit(final Activity activity) {
        mUCGameSDK.exitSDK(activity, new UCCallbackListener<String>() {
            @Override
            public void callback(int i, String s) {
                if (i == UCGameSDKStatusCode.SDK_EXIT) {
                    destroyFloatButton(activity);
                    mUserCallBack.doExit();
                }
            }
        });
    }

    private void createFloatButton(Activity activity) {
        try {
            mUCGameSDK.createFloatButton(activity, new UCCallbackListener<String>() {
                @Override
                public void callback(int i, String s) {
                    KSCLog.d("ToolBar callback , code : " + i + ", msg = " + s);
                }
            });
        } catch (UCCallbackListenerNullException | UCFloatButtonCreateException e) {
            KSCLog.e("UC createFloatException", e);
        }
    }

    private void showFloatButton(Activity activity) {
        try {
            mUCGameSDK.showFloatButton(activity, 100, 50, true);
        } catch (UCCallbackListenerNullException e) {
            KSCLog.e("UC showFloatException", e);
        }
    }

    private void destroyFloatButton(Activity activity) {
        mUCGameSDK.destoryFloatButton(activity);
    }

    @Override
    public void onEnterGame(RoleInfo roleInfo) {
        KSCLog.d("UC onEnterGame, roleInfo : " + roleInfo.toString());
        try {
            JSONObject jsonExData = new JSONObject();
            jsonExData.put("roleId", roleInfo.getRoleId());
            jsonExData.put("roleName", roleInfo.getRoleName());
            jsonExData.put("roleLevel", roleInfo.getRoleLevel());
            jsonExData.put("zoneId", roleInfo.getZoneID());
            jsonExData.put("zoneName", roleInfo.getZoneName());
            UCGameSDK.defaultSDK().submitExtendData("loginGameRole", jsonExData);
            KSCLog.d("success in onEnterGame, userInfo: " + jsonExData.toString());
        } catch (Exception e) {
            KSCLog.e("error in onEnterGame Exception is :" + e.getMessage(), e);
        }
    }

    @Override
    public boolean isMethodSupport(String methodName) {
        return true;
    }

    @Override
    public void openUserCenter(Activity activity) {
        try {
            mUCGameSDK.enterUserCenter(activity, new UCCallbackListener<String>() {
                @Override
                public void callback(int i, String s) {
                    KSCLog.d("open UserCenter callback, code = " + i + ", msg = " + s);
                }
            });
        } catch (UCCallbackListenerNullException e) {
            KSCLog.e("open UserCenter Exception", e);
        }
    }
}

package com.ksc.client.impl;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.ksc.client.core.api.entity.OrderResponse;
import com.ksc.client.core.base.entity.AppInfo;
import com.ksc.client.core.base.entity.PayInfo;
import com.ksc.client.core.config.KSCStatusCode;
import com.ksc.client.core.inner.ChannelBase;
import com.ksc.client.util.KSCLog;
import com.qihoo.gamecenter.sdk.activity.ContainerActivity;
import com.qihoo.gamecenter.sdk.common.IDispatcherCallback;
import com.qihoo.gamecenter.sdk.matrix.Matrix;
import com.qihoo.gamecenter.sdk.protocols.ProtocolConfigs;
import com.qihoo.gamecenter.sdk.protocols.ProtocolKeys;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Alamusi on 2016/7/1.
 */
public class ChnanelImpl extends ChannelBase {

    private boolean mIsLandScape = false;
    private String mAccessToken = null;
    private String mUid = null;
    private String mNotifyUrl = null;

    @Override
    public void init(Activity activity, AppInfo appInfo, JSONObject channelInfo) {
        KSCLog.d("360 init begin");
        Matrix.init(activity);
        if (appInfo.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mIsLandScape = true;
        } else if (appInfo.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            mIsLandScape = false;
        }
        mUserCallBack.onInitSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
        KSCLog.d("360 init end");
    }

    @Override
    public void login(Activity activity) {
        KSCLog.d("360 Login begin");
        Intent intent = getLoginIntent(activity, mIsLandScape);
        Matrix.execute(activity, intent, new IDispatcherCallback() {
            @Override
            public void onFinished(String data) {
                if (isCancelLogin(data)) {
                    mUserCallBack.onLoginFail(KSCStatusCode.LOGIN_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_FAIL));
                    return;
                }
                mAccessToken = parseAccessTokenFromLoginResult(data);
                if (TextUtils.isEmpty(mAccessToken)) {
                    mUserCallBack.onLoginFail(KSCStatusCode.LOGIN_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_FAIL));
                } else {
                    setAuthInfo(mAccessToken);
                    mUserCallBack.onLoginSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                }
            }
        });
        KSCLog.d("360 Login end");
    }

    private String parseAccessTokenFromLoginResult(String data) {
        try {
            JSONObject jsonResult = new JSONObject(data);
            JSONObject jsonData = jsonResult.getJSONObject("data");
            return jsonData.getString("access_token");
        } catch (JSONException e) {
            KSCLog.e(e.getMessage());
        }
        return null;
    }

    private boolean isCancelLogin(String data) {
        try {
            JSONObject jsonData = new JSONObject(data);
            int error = jsonData.optInt("errno", -1);
            if (error == -1) {
                return true;
            }
        } catch (JSONException e) {
            KSCLog.e(e.getMessage());
        }
        return false;
    }

    private Intent getLoginIntent(Activity activity, boolean isLandScape) {
        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtra(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_LOGIN);
        intent.putExtra(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, isLandScape);
        intent.putExtra(ProtocolKeys.IS_LOGIN_SHOW_CLOSE_ICON, false);
        intent.putExtra(ProtocolKeys.IS_SUPPORT_OFFLINE, false);
        intent.putExtra(ProtocolKeys.IS_SHOW_AUTOLOGIN_SWITCH, true);
        intent.putExtra(ProtocolKeys.IS_HIDE_WELLCOME, false);
        intent.putExtra(ProtocolKeys.IS_AUTOLOGIN_NOUI, false);
        intent.putExtra(ProtocolKeys.IS_SHOW_LOGINDLG_ONFAILED_AUTOLOGIN, true);
        return intent;
    }

    @Override
    public void logout(Activity activity) {
        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtra(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_LOGOUT);
        Matrix.execute(activity, intent, new IDispatcherCallback() {
            @Override
            public void onFinished(String s) {
                setAuthInfo(null);
                mUserCallBack.onLogoutSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
            }
        });
    }

    @Override
    public void switchAccount(Activity activity) {
        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtra(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, mIsLandScape);
        intent.putExtra(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_SWITCH_ACCOUNT);
        intent.putExtra(ProtocolKeys.IS_LOGIN_SHOW_CLOSE_ICON, false);
        Matrix.invokeActivity(activity, intent, new IDispatcherCallback() {
            @Override
            public void onFinished(String data) {
                if (isCancelLogin(data)) {
                    mUserCallBack.onSwitchAccountFail(KSCStatusCode.SWITCH_ACCOUNT_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_FAIL));
                    return;
                }
                mAccessToken = parseAccessTokenFromLoginResult(data);
                if (TextUtils.isEmpty(mAccessToken)) {
                    mUserCallBack.onSwitchAccountFail(KSCStatusCode.SWITCH_ACCOUNT_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_FAIL));
                } else {
                    setAuthInfo(mAccessToken);
                    mUserCallBack.onSwitchAccountSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                }
            }
        });
    }

    @Override
    public void pay(Activity activity, final PayInfo payInfo, OrderResponse orderResponse) {
        QihooPayInfo qihooPayInfo = getQihoopPayInfo(activity, payInfo, orderResponse, mUid);
        Intent intent = getPayIntent(activity, qihooPayInfo);
        Matrix.execute(activity, intent, new IDispatcherCallback() {
            @Override
            public void onFinished(String data) {
                if (TextUtils.isEmpty(data)) {
                    return;
                }
                JSONObject jsonRes;
                try {
                    jsonRes = new JSONObject(data);
                    // error_code 状态码 0 支付成功 -1 支付取消 1 支付失败 2 支付进行中 4010201和4009911
                    // 登录状态已失效，引导用户重新登录
                    int errorCode = jsonRes.getInt("error_code");
                    switch (errorCode) {
                        case 0:
                            mUserCallBack.onPaySuccess(payInfo, KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
                            break;
                        case -1:
                            mUserCallBack.onPaySuccess(payInfo, KSCStatusCode.PAY_CANCELED, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_CANCELED));
                            break;
                        case -2:
                            mUserCallBack.onPaySuccess(payInfo, KSCStatusCode.PAY_PROGRESS, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_PROGRESS));
                            break;
                        case 1:
                            mUserCallBack.onPaySuccess(payInfo, KSCStatusCode.PAY_FAILED, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_FAILED));
                            break;
                        case 4010201:
                        case 4009911:
                            mUserCallBack.onPaySuccess(payInfo, KSCStatusCode.PAY_FAILED_UID_INVALID, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_FAILED_UID_INVALID));
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    KSCLog.e(e.getMessage());
                    mUserCallBack.onPaySuccess(payInfo, KSCStatusCode.PAY_RESULT_UNKNOWN, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_RESULT_UNKNOWN));
                }
            }
        });
    }

    private Intent getPayIntent(Activity activity, QihooPayInfo qihooPayInfo) {
        Bundle bundle = new Bundle();

        // 界面相关参数，360SDK界面是否以横屏显示。
        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, mIsLandScape);

        // *** 以下非界面相关参数 ***

        // 设置QihooPay中的参数。
        // 必需参数，360账号id，整数。
        bundle.putString(ProtocolKeys.QIHOO_USER_ID, qihooPayInfo.getQihooUserId());

        // 必需参数，所购买商品金额, 以分为单位。金额大于等于100分，360SDK运行定额支付流程； 金额数为0，360SDK运行不定额支付流程。
        bundle.putString(ProtocolKeys.AMOUNT, qihooPayInfo.getMoneyAmount());

        // 必需参数，人民币与游戏充值币的默认比例，例如2，代表1元人民币可以兑换2个游戏币，整数。
        bundle.putString(ProtocolKeys.RATE, qihooPayInfo.getExchangeRate());

        // 必需参数，所购买商品名称，应用指定，建议中文，最大10个中文字。
        bundle.putString(ProtocolKeys.PRODUCT_NAME, qihooPayInfo.getProductName());

        // 必需参数，购买商品的商品id，应用指定，最大16字符。
        bundle.putString(ProtocolKeys.PRODUCT_ID, qihooPayInfo.getProductId());

        // 必需参数，应用方提供的支付结果通知uri，最大255字符。360服务器将把支付接口回调给该uri，具体协议请查看文档中，支付结果通知接口–应用服务器提供接口。
        bundle.putString(ProtocolKeys.NOTIFY_URI, qihooPayInfo.getNotifyUri());

        // 必需参数，游戏或应用名称，最大16中文字。
        bundle.putString(ProtocolKeys.APP_NAME, qihooPayInfo.getAppName());

        // 必需参数，应用内的用户名，如游戏角色名。 若应用内绑定360账号和应用账号，则可用360用户名，最大16中文字。（充值不分区服，
        // 充到统一的用户账户，各区服角色均可使用）。
        bundle.putString(ProtocolKeys.APP_USER_NAME, qihooPayInfo.getAppUserName());

        // 必需参数，应用内的用户id。
        // 若应用内绑定360账号和应用账号，充值不分区服，充到统一的用户账户，各区服角色均可使用，则可用360用户ID最大32字符。
        bundle.putString(ProtocolKeys.APP_USER_ID, qihooPayInfo.getAppUserId());

        // 可选参数，应用扩展信息1，原样返回，最大255字符。
        bundle.putString(ProtocolKeys.APP_EXT_1, qihooPayInfo.getAppExt1());

        // 可选参数，应用扩展信息2，原样返回，最大255字符。
        bundle.putString(ProtocolKeys.APP_EXT_2, qihooPayInfo.getAppExt2());

        // 可选参数，应用订单号，应用内必须唯一，最大32字符。
        bundle.putString(ProtocolKeys.APP_ORDER_ID, qihooPayInfo.getAppOrderId());

        // 必需参数，使用360SDK的支付模块。
        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_PAY);

        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtras(bundle);

        return intent;
    }

    private QihooPayInfo getQihoopPayInfo(Activity activity, PayInfo payInfo, OrderResponse response, String mUid) {
        QihooPayInfo qihooPayInfo = new QihooPayInfo();
        qihooPayInfo.setQihooUserId(mUid);
        qihooPayInfo.setAccessToken(mAccessToken);
        qihooPayInfo.setMoneyAmount(payInfo.getPrice());
        qihooPayInfo.setExchangeRate(payInfo.getRate());
        qihooPayInfo.setProductName(payInfo.getProductName());
        qihooPayInfo.setNotifyUri(mNotifyUrl);
        try {
            ApplicationInfo appInfo;
            appInfo = activity.getPackageManager().getApplicationInfo(activity.getPackageName(),
                    PackageManager.GET_META_DATA);
            String gameName = (String) activity.getPackageManager().getApplicationLabel(appInfo);
            qihooPayInfo.setAppName(gameName);
        } catch (PackageManager.NameNotFoundException e) {
            KSCLog.e(e.getMessage());
        }
        qihooPayInfo.setAppUserName(payInfo.getRoleName());
        qihooPayInfo.setAppUserId(payInfo.getRoleId());

        // 可选参数
        qihooPayInfo.setAppExt1(payInfo.getCustomInfo());
        qihooPayInfo.setAppExt2(payInfo.getCustomInfo());
        qihooPayInfo.setAppOrderId(response.getKscOrder());
        return qihooPayInfo;
    }

    @Override
    public void exit(Activity activity) {
        KSCLog.d("360 exit begin");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, mIsLandScape);
        bundle.putString(ProtocolKeys.UI_BACKGROUND_PICTRUE, "");
        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_QUIT);
        final Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtras(bundle);
        Matrix.execute(activity, intent, new IDispatcherCallback() {
            @Override
            public void onFinished(String data) {
                KSCLog.d("360 Exit callback info : " + data);
                try {
                    JSONObject json = new JSONObject(data);
                    int which = json.optInt("which", -1);
                    switch (which) {
                        case 0:
                            break;
                        default:
                            mUserCallBack.doExit();
                            break;
                    }
                } catch (JSONException e) {
                    KSCLog.e(e.getMessage());
                }
            }
        });
        KSCLog.d("360 Exit end");
    }

    @Override
    public void onDestroy(Activity activity) {
        KSCLog.d("360 destroy begin");
        super.onDestroy(activity);
        Matrix.destroy(activity);
        KSCLog.d("360 destroy end");
    }
}

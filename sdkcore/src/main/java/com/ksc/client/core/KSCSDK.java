package com.ksc.client.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;

import com.ksc.client.core.api.KSCCommonService;
import com.ksc.client.core.api.callback.GetInitParamCallBack;
import com.ksc.client.core.api.callback.GetOrderCallBack;
import com.ksc.client.core.api.entity.OrderResponse;
import com.ksc.client.core.base.ISDK;
import com.ksc.client.core.base.callback.ExitCallBack;
import com.ksc.client.core.base.callback.InitCallBack;
import com.ksc.client.core.base.callback.LoginCallBack;
import com.ksc.client.core.base.callback.LogoutCallBack;
import com.ksc.client.core.base.callback.PayCallBack;
import com.ksc.client.core.base.callback.SwitchAccountCallBack;
import com.ksc.client.core.base.entity.AppInfo;
import com.ksc.client.core.base.entity.PayInfo;
import com.ksc.client.core.base.entity.RoleInfo;
import com.ksc.client.core.config.KSCSDKInfo;
import com.ksc.client.core.config.KSCStatusCode;
import com.ksc.client.core.inner.ChannelBase;
import com.ksc.client.core.inner.callbackwrapper.UserCallBackWrapper;
import com.ksc.client.util.KSCLog;

import java.text.MessageFormat;

/**
 * 主入口
 * Created by Alamusi on 2016/6/21.
 */
public class KSCSDK implements ISDK {

    private static KSCSDK mInstance = null;
    private static UserCallBackWrapper mUserCallBack = null;

    public KSCSDK() {
        mUserCallBack = new UserCallBackWrapper();
    }

    public static KSCSDK getInstance() {
        if (mInstance == null) {
            synchronized (KSCSDK.class) {
                if (mInstance == null) {
                    mInstance = new KSCSDK();
                }
            }
        }
        return mInstance;
    }

    @Override
    public void init(final Activity activity, final AppInfo appInfo, InitCallBack initCallBack) {
        KSCLog.d(MessageFormat.format("begin to init. activity={0}, appInfo={1}, initCallBack={2}", activity, appInfo.toString(), initCallBack));
        if (activity == null) {
            KSCLog.e("init param error, activity can not be null, please check!");
            return;
        } else {
            mUserCallBack.setActivity(activity);
        }
        if (initCallBack == null) {
            KSCLog.e("init param error, initCallBack can not be null, please check!");
            return;
        } else {
            mUserCallBack.setInitCallBack(initCallBack);
        }
        if (TextUtils.isEmpty(appInfo.getAppId())) {
            KSCLog.e("init param error, appId can not be null, please check!");
            mUserCallBack.onInitFail(KSCStatusCode.INIT_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.INIT_FAIL));
            return;
        }
        if (TextUtils.isEmpty(appInfo.getAppKey())) {
            KSCLog.e("init param error, appKey can not be null, please check!");
            mUserCallBack.onInitFail(KSCStatusCode.INIT_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.INIT_FAIL));
            return;
        }
        KSCSDKInfo.setAppInfo(appInfo);
        KSCCommonService.getInitParams(activity, KSCSDKInfo.getChannelId(), new GetInitParamCallBack() {
            @Override
            public void onGetParamsResult(int code, String result) {
                if (code == KSCCommonService.K_RESPONSE_OK) {
                    KSCLog.i("get channel param success, begin channel init");
                    if (getChannelImpl() != null) {
                        getChannelImpl().init(activity, appInfo, result);
                    } else {
                        printErrorLogNonChannelImpl();
                    }
                } else if (code == KSCCommonService.K_RESPONSE_FAIL) {
                    KSCLog.i("get channel param fail, init sdk fail!");
                    mUserCallBack.onInitFail(KSCStatusCode.INIT_FAIL, KSCStatusCode.getErrorMsg(KSCStatusCode.INIT_FAIL));
                }
            }
        });
        KSCLog.d(MessageFormat.format("end to init. activity={0}, appInfo={1}, initCallBack={2}", activity, appInfo.toString(), initCallBack));
    }

    @Override
    public void login(Activity activity, LoginCallBack loginCallBack) {
        KSCLog.d(MessageFormat.format("begin to login. activity={0}, loginCallBack={1}", activity, loginCallBack));
        if (activity == null) {
            KSCLog.e("login param error, activity can not be null, please check!");
            return;
        }
        if (loginCallBack == null) {
            KSCLog.e("login param error, loginCallBack can not be null, please check!");
            return;
        } else {
            mUserCallBack.setLoginCallBack(loginCallBack);
        }
        if (getChannelImpl() != null) {
            getChannelImpl().login(activity);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to login. activity={0}, loginCallBack={1}", activity, loginCallBack));
    }

    @Override
    public void logout(Activity activity, LogoutCallBack logoutCallBack) {
        KSCLog.d(MessageFormat.format("begin to logout. activity={0}, logoutCallBack={1}", activity, logoutCallBack));
        if (activity == null) {
            KSCLog.e("logout param error, activity can not be null, please check!");
            return;
        }
        if (logoutCallBack == null) {
            KSCLog.e("logout param error, logoutCallBack can not be null, please check!");
            return;
        } else {
            mUserCallBack.setLogoutCallBack(logoutCallBack);
        }
        if (getChannelImpl() != null) {
            getChannelImpl().logout(activity);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to logout. activity={0}, logoutCallBack={1}", activity, logoutCallBack));
    }

    @Override
    public void pay(final Activity activity, final PayInfo payInfo, final PayCallBack payCallBack) {
        KSCLog.d(MessageFormat.format("begin to pay. activity={0}, payInfo={1}, payCallBack={2}", activity, payInfo.toString(), payCallBack));
        if (activity == null) {
            KSCLog.e("pay param error, activity can not be null, please check!");
            return;
        }
        if (payCallBack == null) {
            KSCLog.e("pay param error, payCallBack can not be null, please check!");
            return;
        } else {
            mUserCallBack.setPayCallBack(payCallBack);
        }
        KSCCommonService.createOrder(activity, getChannelID(), payInfo, new GetOrderCallBack() {
            @Override
            public void onCreateOrderResult(int code, String msg, final OrderResponse response) {
                if (code != KSCCommonService.K_RESPONSE_OK) {
                    mUserCallBack.onPayFail(payInfo, KSCStatusCode.PAY_FAILED_CREATE_ORDER_FAILED, msg);
                    return;
                }
                if (getChannelImpl() != null) {
                    getChannelImpl().pay(activity, payInfo, response);
                } else {
                    printErrorLogNonChannelImpl();
                }
            }
        });
        KSCLog.d(MessageFormat.format("end to pay. activity={0}, payInfo={1}, payCallBack={2}", activity, payInfo.toString(), payCallBack));
    }

    @Override
    public void exit(Activity activity, ExitCallBack exitCallBack) {
        KSCLog.d(MessageFormat.format("begin to exit. activity={0}, exitCallBack={1}", activity, exitCallBack));
        if (activity == null) {
            KSCLog.e("exit param error, activity can not be null, please check!");
            return;
        }
        if (exitCallBack == null) {
            KSCLog.e("exit param error, exitCallBack can not be null, please check!");
            return;
        } else {
            mUserCallBack.setExitCallBack(exitCallBack);
        }
        if (getChannelImpl() != null) {
            getChannelImpl().exit(activity);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to exit. activity={0}, exitCallBack={1}", activity, exitCallBack));
    }

    @Override
    public String getChannelID() {
        KSCLog.d("begin to getChannel");
        return KSCSDKInfo.getChannelId();
    }

    @Override
    public String getVersion() {
        KSCLog.d("begin to getSDKVersion");
        return KSCSDKInfo.getKSCVersion();
    }

    @Override
    public String getAuthInfo() {
        KSCLog.d("begin to getAuthInfo");
        if (getChannelImpl() != null) {
            return getChannelImpl().getAuthInfo();
        } else {
            printErrorLogNonChannelImpl();
        }
        return null;
    }

    @Override
    public void onCreate(Activity activity) {
        KSCLog.d(MessageFormat.format("begin to onCreate. activity={0}", activity));
        if (getChannelImpl() != null) {
            getChannelImpl().onCreate(activity);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to onCreate. activity={0}", activity));
    }

    @Override
    public void onStart(Activity activity) {
        KSCLog.d(MessageFormat.format("begin to onStart. activity={0}", activity));
        if (getChannelImpl() != null) {
            getChannelImpl().onStart(activity);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to onStart. activity={0}", activity));
    }

    @Override
    public void onRestart(Activity activity) {
        KSCLog.d(MessageFormat.format("begin to onRestart. activity={0}", activity));
        if (getChannelImpl() != null) {
            getChannelImpl().onRestart(activity);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to onRestart. activity={0}", activity));
    }

    @Override
    public void onResume(Activity activity) {
        KSCLog.d(MessageFormat.format("begin to onResume. activity={0}", activity));
        if (getChannelImpl() != null) {
            getChannelImpl().onResume(activity);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to onResume. activity={0}", activity));
    }

    @Override
    public void onPause(Activity activity) {
        KSCLog.d(MessageFormat.format("begin to onPause. activity={0}", activity));
        if (getChannelImpl() != null) {
            getChannelImpl().onPause(activity);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to onPause. activity={0}", activity));
    }

    @Override
    public void onStop(Activity activity) {
        KSCLog.d(MessageFormat.format("begin to onStop. activity={0}", activity));
        if (getChannelImpl() != null) {
            getChannelImpl().onStop(activity);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to onStop. activity={0}", activity));
    }

    @Override
    public void onDestroy(Activity activity) {
        KSCLog.d(MessageFormat.format("begin to onDestroy. activity={0}", activity));
        if (getChannelImpl() != null) {
            getChannelImpl().onDestroy(activity);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to onDestroy. activity={0}", activity));
    }

    @Override
    public void onNewIntent(Activity activity, Intent intent) {
        KSCLog.d(MessageFormat.format("begin to onNewIntent. activity={0}", activity));
        if (getChannelImpl() != null) {
            getChannelImpl().onNewIntent(activity, intent);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to onNewIntent. activity={0}", activity));
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        KSCLog.d(MessageFormat.format("begin to onActivityResult. activity={0}, requestCode={1}, resultCode={2}, data={3}", activity, requestCode, resultCode, data));
        if (getChannelImpl() != null) {
            getChannelImpl().onActivityResult(activity, requestCode, resultCode, data);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to onActivityResult. activity={0}, requestCode={1}, resultCode={2}, data={3}", activity, requestCode, resultCode, data));
    }

    @Override
    public void onBackPressed(Activity activity) {
        KSCLog.d(MessageFormat.format("begin to onBackPressed. activity={0}", activity));
        if (getChannelImpl() != null) {
            getChannelImpl().onBackPressed(activity);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to onBackPressed. activity={0}", activity));
    }

    @Override
    public void onConfigurationChanged(Activity activity, Configuration newConfig) {
        KSCLog.d(MessageFormat.format("begin to onConfigurationChanged. activity={0}, ", activity));
        if (getChannelImpl() != null) {
            getChannelImpl().onConfigurationChanged(activity, newConfig);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to onConfigurationChanged. activity={0}", activity));
    }

    @Override
    public void onSaveInstanceState(Activity activity, Bundle outState) {
        KSCLog.d(MessageFormat.format("begin to onSaveInstanceState. activity={0}", activity));
        if (getChannelImpl() != null) {
            getChannelImpl().onSaveInstanceState(activity, outState);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to onSaveInstanceState. activity={0}", activity));
    }

    @Override
    public void onApplicationCreate(Context context) {
        KSCLog.d(MessageFormat.format("begin to onApplicationCreate. context={0}", context));
        if (getChannelImpl() != null) {
            getChannelImpl().onApplicationCreate(context);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to onApplicationCreate. context={0}", context));
    }

    @Override
    public void onApplicationAttachBaseContext(Context context) {
        KSCLog.d(MessageFormat.format("begin to onApplicationAttachBaseContext. context={0}", context));
        KSCFactory.load(context);
        if (getChannelImpl() != null) {
            getChannelImpl().setUserCallBackWrapper(mUserCallBack);
            getChannelImpl().onApplicationAttachBaseContext(context);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to onApplicationAttachBaseContext. context={0}", context));
    }

    @Override
    public void onApplicationTerminate(Context context) {
        KSCLog.d(MessageFormat.format("begin to onApplicationTerminate. context={0}", context));
        if (getChannelImpl() != null) {
            getChannelImpl().onApplicationTerminate(context);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to onApplicationTerminate. context={0}", context));
    }

    @Override
    public void switchAccount(Activity activity, SwitchAccountCallBack switchAccountCallBack) {
        KSCLog.d(MessageFormat.format("begin to switchAccount. activity={0}, switchAccountCallBack={2}", activity, switchAccountCallBack));
        if (activity == null) {
            KSCLog.e("switchAccount param error, activity can not be null, please check!");
            return;
        }
        if (switchAccountCallBack == null) {
            KSCLog.e("switchAccount param error, switchAccountCallBack can not be null, please check!");
            return;
        } else {
            mUserCallBack.setSwitchAccountCallBack(switchAccountCallBack);
        }
        if (getChannelImpl() != null) {
            getChannelImpl().switchAccount(activity);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to switchAccount. activity={0}, switchAccountCallBack={2}", activity, switchAccountCallBack));
    }

    @Override
    public void openUserCenter(Activity activity) {
        KSCLog.d(MessageFormat.format("begin to openUserCenter. activity={0}", activity));
        if (activity == null) {
            KSCLog.e("openUserCenter param error, activity can not be null, please check!");
            return;
        }
        if (getChannelImpl() != null) {
            getChannelImpl().openUserCenter(activity);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to openUserCenter. activity={0}", activity));
    }

    @Override
    public boolean isMethodSupport(String methodName) {
        KSCLog.d(MessageFormat.format("begin to openUserCenter. methodName={0}", methodName));
        if (getChannelImpl() != null) {
            return getChannelImpl().isMethodSupport(methodName);
        } else {
            printErrorLogNonChannelImpl();
        }
        return false;
    }

    @Override
    public void onCreateRole(RoleInfo roleInfo) {
        KSCLog.d(MessageFormat.format("begin to onCreateRole. roleInfo={0}", roleInfo.toString()));
        if (getChannelImpl() != null) {
            getChannelImpl().onCreateRole(roleInfo);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to onCreateRole. roleInfo={0}", roleInfo.toString()));
    }

    @Override
    public void onEnterGame(RoleInfo roleInfo) {
        KSCLog.d(MessageFormat.format("begin to onEnterGame. roleInfo={0}", roleInfo.toString()));
        if (getChannelImpl() != null) {
            getChannelImpl().onEnterGame(roleInfo);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to onEnterGame. roleInfo={0}", roleInfo.toString()));
    }

    @Override
    public void onRoleLevelUp(RoleInfo roleInfo) {
        KSCLog.d(MessageFormat.format("begin to onRoleLevelUp. roleInfo={0}", roleInfo.toString()));
        if (getChannelImpl() != null) {
            getChannelImpl().onRoleLevelUp(roleInfo);
        } else {
            printErrorLogNonChannelImpl();
        }
        KSCLog.d(MessageFormat.format("end to onRoleLevelUp. roleInfo={0}", roleInfo.toString()));
    }

    private ChannelBase getChannelImpl() {
        final String CATCH_UNEXPECTED_EXCEPTION = "catch unexpected exception";
        try {
            return KSCFactory.getChannel();
        } catch (Exception e) {
            KSCLog.e(CATCH_UNEXPECTED_EXCEPTION, e);
        }
        return null;
    }

    private void printErrorLogNonChannelImpl() {
        KSCLog.e("can not find channel implement, please check if your main activity is inherited from KSCActivity or call KSCApplication lifecyle interfaces(such as onCreate, onStart and etc.)");
    }

}

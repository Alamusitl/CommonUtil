package com.ksc.client.core.inner.callbackwrapper;

import com.ksc.client.core.base.callback.ExitCallBack;
import com.ksc.client.core.base.callback.InitCallBack;
import com.ksc.client.core.base.callback.LoginCallBack;
import com.ksc.client.core.base.callback.LogoutCallBack;
import com.ksc.client.core.base.callback.PayCallBack;
import com.ksc.client.core.base.callback.SwitchAccountCallBack;
import com.ksc.client.core.base.entity.PayInfo;

/**
 * Created by Alamusi on 2016/6/29.
 */
public class UserCallBackWrapper implements InitCallBack, ExitCallBack, LoginCallBack, LogoutCallBack, PayCallBack, SwitchAccountCallBack {

    private InitCallBack mInitCallBack;
    private LoginCallBack mLoginCallBack;
    private LogoutCallBack mLogoutCallBack;
    private PayCallBack mPayCallBack;
    private SwitchAccountCallBack mSwitchAccountCallBack;
    private ExitCallBack mExitCallBack;

    public void setInitCallBack(InitCallBack initCallBack) {
        mInitCallBack = initCallBack;
    }

    public void setLoginCallBack(LoginCallBack loginCallBack) {
        mLoginCallBack = loginCallBack;
    }

    public void setLogoutCallBack(LogoutCallBack logoutCallBack) {
        mLogoutCallBack = logoutCallBack;
    }

    public void setPayCallBack(PayCallBack payCallBack) {
        mPayCallBack = payCallBack;
    }

    public void setSwitchAccountCallBack(SwitchAccountCallBack switchAccountCallBack) {
        mSwitchAccountCallBack = switchAccountCallBack;
    }

    public void setExitCallBack(ExitCallBack exitCallBack) {
        mExitCallBack = exitCallBack;
    }


    @Override
    public void doExit() {
        if (mExitCallBack != null) {
            mExitCallBack.doExit();
        }
    }

    @Override
    public void onNoChannelExit() {
        if (mExitCallBack != null) {
            mExitCallBack.onNoChannelExit();
        }
    }

    @Override
    public void onInitFail(int code, String msg) {
        if (mInitCallBack != null) {
            mInitCallBack.onInitFail(code, msg);
        }
    }

    @Override
    public void onInitSuccess(int code, String msg) {
        if (mInitCallBack != null) {
            mInitCallBack.onInitSuccess(code, msg);
        }
    }

    @Override
    public void onLoginSuccess(int code, String msg) {
        if (mLoginCallBack != null) {
            mLoginCallBack.onLoginSuccess(code, msg);
        }
    }

    @Override
    public void onLoginFail(int code, String msg) {
        if (mLoginCallBack != null) {
            mLoginCallBack.onLoginFail(code, msg);
        }
    }

    @Override
    public void onLoginCancel(int code, String msg) {
        if (mLoginCallBack != null) {
            mLoginCallBack.onLoginCancel(code, msg);
        }
    }

    @Override
    public void onLogoutSuccess(int code, String msg) {
        if (mLogoutCallBack != null) {
            mLogoutCallBack.onLogoutSuccess(code, msg);
        }
    }

    @Override
    public void onLogoutFail(int code, String msg) {
        if (mLogoutCallBack != null) {
            mLogoutCallBack.onLogoutFail(code, msg);
        }
    }

    @Override
    public void onPaySuccess(PayInfo payInfo, int code, String msg) {
        if (mPayCallBack != null) {
            mPayCallBack.onPaySuccess(payInfo, code, msg);
        }
    }

    @Override
    public void onPayFail(PayInfo payInfo, int code, String msg) {
        if (mPayCallBack != null) {
            mPayCallBack.onPayFail(payInfo, code, msg);
        }
    }

    @Override
    public void onPayCancel(PayInfo payInfo, int code, String msg) {
        if (mPayCallBack != null) {
            mPayCallBack.onPayCancel(payInfo, code, msg);
        }
    }

    @Override
    public void onPayOthers(PayInfo payInfo, int code, String msg) {
        if (mPayCallBack != null) {
            mPayCallBack.onPayOthers(payInfo, code, msg);
        }
    }

    @Override
    public void onPayProgress(PayInfo payInfo, int code, String msg) {
        if (mPayCallBack != null) {
            mPayCallBack.onPayProgress(payInfo, code, msg);
        }
    }

    @Override
    public void onSwitchAccountSuccess(int code, String msg) {
        if (mSwitchAccountCallBack != null) {
            mSwitchAccountCallBack.onSwitchAccountSuccess(code, msg);
        }
    }

    @Override
    public void onSwitchAccountFail(int code, String msg) {
        if (mSwitchAccountCallBack != null) {
            mSwitchAccountCallBack.onSwitchAccountFail(code, msg);
        }
    }

    @Override
    public void onSwitchAccountCancel(int code, String msg) {
        if (mSwitchAccountCallBack != null) {
            mSwitchAccountCallBack.onSwitchAccountCancel(code, msg);
        }
    }
}

package com.ksc.client.core.inner.callbackwrapper;

import android.app.Activity;

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

    private Activity mCurrentActivity;
    private InitCallBack mInitCallBack;
    private LoginCallBack mLoginCallBack;
    private LogoutCallBack mLogoutCallBack;
    private PayCallBack mPayCallBack;
    private SwitchAccountCallBack mSwitchAccountCallBack;
    private ExitCallBack mExitCallBack;

    public void setActivity(Activity activity) {
        mCurrentActivity = activity;
    }

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
        if (mExitCallBack != null && mCurrentActivity != null) {
            mCurrentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mExitCallBack.doExit();
                    mCurrentActivity = null;
                }
            });
        }
    }

    @Override
    public void onNoChannelExit() {
        if (mExitCallBack != null && mCurrentActivity != null) {
            mCurrentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mExitCallBack.onNoChannelExit();
                    mCurrentActivity = null;
                }
            });
        }
    }

    @Override
    public void onInitFail(final int code, final String msg) {
        if (mInitCallBack != null && mCurrentActivity != null) {
            mCurrentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mInitCallBack.onInitFail(code, msg);
                }
            });
        }
    }

    @Override
    public void onInitSuccess(final int code, final String msg) {
        if (mInitCallBack != null && mCurrentActivity != null) {
            mCurrentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mInitCallBack.onInitSuccess(code, msg);
                }
            });
        }
    }

    @Override
    public void onLoginSuccess(final int code, final String msg) {
        if (mLoginCallBack != null && mCurrentActivity != null) {
            mCurrentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLoginCallBack.onLoginSuccess(code, msg);
                }
            });
        }
    }

    @Override
    public void onLoginFail(final int code, final String msg) {
        if (mLoginCallBack != null && mCurrentActivity != null) {
            mCurrentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLoginCallBack.onLoginFail(code, msg);
                }
            });
        }
    }

    @Override
    public void onLoginCancel(final int code, final String msg) {
        if (mLoginCallBack != null && mCurrentActivity != null) {
            mCurrentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLoginCallBack.onLoginCancel(code, msg);
                }
            });
        }
    }

    @Override
    public void onLogoutSuccess(final int code, final String msg) {
        if (mLogoutCallBack != null && mCurrentActivity != null) {
            mCurrentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLogoutCallBack.onLogoutSuccess(code, msg);
                }
            });
        }
    }

    @Override
    public void onLogoutFail(final int code, final String msg) {
        if (mLogoutCallBack != null && mCurrentActivity != null) {
            mCurrentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLogoutCallBack.onLogoutFail(code, msg);
                }
            });
        }
    }

    @Override
    public void onPaySuccess(final PayInfo payInfo, final int code, final String msg) {
        if (mPayCallBack != null && mCurrentActivity != null) {
            mCurrentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPayCallBack.onPaySuccess(payInfo, code, msg);
                }
            });
        }
    }

    @Override
    public void onPayFail(final PayInfo payInfo, final int code, final String msg) {
        if (mPayCallBack != null && mCurrentActivity != null) {
            mCurrentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPayCallBack.onPayFail(payInfo, code, msg);
                }
            });
        }
    }

    @Override
    public void onPayCancel(final PayInfo payInfo, final int code, final String msg) {
        if (mPayCallBack != null && mCurrentActivity != null) {
            mCurrentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPayCallBack.onPayCancel(payInfo, code, msg);
                }
            });
        }
    }

    @Override
    public void onPayOthers(final PayInfo payInfo, final int code, final String msg) {
        if (mPayCallBack != null && mCurrentActivity != null) {
            mCurrentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPayCallBack.onPayOthers(payInfo, code, msg);
                }
            });
        }
    }

    @Override
    public void onPayProgress(final PayInfo payInfo, final int code, final String msg) {
        if (mPayCallBack != null && mCurrentActivity != null) {
            mCurrentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPayCallBack.onPayProgress(payInfo, code, msg);
                }
            });
        }
    }

    @Override
    public void onSwitchAccountSuccess(final int code, final String msg) {
        if (mSwitchAccountCallBack != null && mCurrentActivity != null) {
            mCurrentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSwitchAccountCallBack.onSwitchAccountSuccess(code, msg);
                }
            });
        }
    }

    @Override
    public void onSwitchAccountFail(final int code, final String msg) {
        if (mSwitchAccountCallBack != null && mCurrentActivity != null) {
            mCurrentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSwitchAccountCallBack.onSwitchAccountFail(code, msg);
                }
            });
        }
    }

    @Override
    public void onSwitchAccountCancel(final int code, final String msg) {
        if (mSwitchAccountCallBack != null && mCurrentActivity != null) {
            mCurrentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSwitchAccountCallBack.onSwitchAccountCancel(code, msg);
                }
            });
        }
    }
}

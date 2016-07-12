package com.ksc.client.core.inner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;

import com.ksc.client.core.config.KSCStatusCode;
import com.ksc.client.core.base.entity.AppInfo;
import com.ksc.client.core.base.entity.PayInfo;
import com.ksc.client.core.base.entity.RoleInfo;
import com.ksc.client.core.inner.callbackwrapper.UserCallBackWrapper;
import com.ksc.client.core.api.entity.OrderResponse;

/**
 * Created by Alamusi on 2016/6/22.
 */
public abstract class ChannelBase {

    private String mAuthInfo = null;
    protected UserCallBackWrapper mUserCallBack;

    /********************
     * Base Interface
     ********************/
    public abstract void init(final Activity activity, AppInfo appInfo, String channelInfo);

    public abstract void login(final Activity activity);

    public void logout(final Activity activity) {
        if (mUserCallBack != null) {
            setAuthInfo(null);
            mUserCallBack.onLogoutSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
        }
    }

    public abstract void switchAccount(final Activity activity);

    public void openUserCenter(final Activity activity) {
    }

    public void exit(final Activity activity) {
        if (mUserCallBack != null) {
            mUserCallBack.onNoChannelExit();
        }
    }

    public abstract void pay(final Activity activity, PayInfo payInfo, OrderResponse response);

    public boolean isMethodSupport(String methodName) {
        return false;
    }

    public String getAuthInfo() {
        return mAuthInfo;
    }

    public void setAuthInfo(String authInfo) {
        mAuthInfo = authInfo;
    }

    public void setUserCallBackWrapper(UserCallBackWrapper userCallBackWrapper) {
        mUserCallBack = userCallBackWrapper;
    }

    public boolean isLogin() {
        if (TextUtils.isEmpty(getAuthInfo())) {
            return false;
        } else {
            return true;
        }
    }

    /*******************
     * Activity Interface
     *******************/
    public void onCreate(final Activity activity) {
    }

    public void onStart(final Activity activity) {
    }

    public void onRestart(final Activity activity) {
    }

    public void onResume(final Activity activity) {
    }

    public void onPause(final Activity activity) {
    }

    public void onStop(final Activity activity) {
    }

    public void onDestroy(final Activity activity) {
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    }

    public void onNewIntent(final Activity activity, final Intent intent) {
    }

    public void onBackPressed(Activity activity) {
    }

    public void onConfigurationChanged(Activity activity, Configuration newConfig) {
    }

    public void onSaveInstanceState(Activity activity, Bundle outState) {
    }

    /********************
     * Application Interface
     ********************/
    public void onApplicationCreate(final Context context) {
    }

    public void onApplicationAttachBaseContext(final Context context) {
    }

    public void onApplicationTerminate(final Context context) {
    }

    /********************
     * Extend Interface
     ********************/
    public void onCreateRole(final RoleInfo info) {
    }

    public void onEnterGame(final RoleInfo roleInfo) {
    }

    public void onRoleLevelUp(final RoleInfo roleInfo) {
    }


}

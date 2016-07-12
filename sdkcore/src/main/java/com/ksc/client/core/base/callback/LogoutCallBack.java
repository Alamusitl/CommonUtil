package com.ksc.client.core.base.callback;

/**
 * Created by Alamusi on 2016/6/22.
 */
public interface LogoutCallBack {
    // 登出完成，建议游戏返回账号登录界面
    void onLogoutSuccess(int code, String msg);

    // 登出失败
    void onLogoutFail(int code, String msg);
}

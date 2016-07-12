package com.ksc.client.core.base.callback;

/**
 * Created by Alamusi on 2016/6/21.
 */
public interface LoginCallBack {
    //登录成功,游戏需要根据authInfo到服务器进行登录验证
    void onLoginSuccess(int code, String msg);

    //登录失败,建议游戏返回账号登录界面
    void onLoginFail(int code, String msg);

    //登录取消,建议游戏返回账号登录界面
    void onLoginCancel(int code, String msg);
}

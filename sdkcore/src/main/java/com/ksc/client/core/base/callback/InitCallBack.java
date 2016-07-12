package com.ksc.client.core.base.callback;

/**
 * Created by Alamusi on 2016/6/21.
 */
public interface InitCallBack {
    //渠道初始化失败，建议重启游戏
    void onInitFail(int code, String msg);

    //渠道初始化成功，游戏可以启动登陆
    void onInitSuccess(int code, String msg);
}

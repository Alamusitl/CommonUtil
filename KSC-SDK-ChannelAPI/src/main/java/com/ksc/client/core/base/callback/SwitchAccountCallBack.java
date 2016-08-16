package com.ksc.client.core.base.callback;

/**
 * Created by Alamusi on 2016/6/21.
 */
public interface SwitchAccountCallBack {

    // 切换账号成功，游戏可以做重新加载角色等操作
    void onSwitchAccountSuccess(int code, String msg);

    // 切换账号失败，游戏不用做处理
    void onSwitchAccountFail(int code, String msg);

    // 取消切换账号，游戏不用做处理
    void onSwitchAccountCancel(int code, String msg);
}

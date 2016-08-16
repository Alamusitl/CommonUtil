package com.ksc.client.core.base.callback;

/**
 * Created by Alamusi on 2016/6/21.
 */
public interface ExitCallBack {
    //玩家已经确认退出，建议游戏直接执行退出操作
    void doExit();

    //没有渠道的退出界面，游戏可以实现自己的退出界面
    void onNoChannelExit();
}

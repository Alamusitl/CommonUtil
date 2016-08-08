package com.ksc.client.update.callback;

import com.ksc.client.update.entity.KSCUpdateInfo;

import java.util.ArrayList;

/**
 * Created by Alamusi on 2016/7/26.
 */
public interface CheckUpdateCallBack {

    /**
     * 检查更新失败
     *
     * @param error 错误信息
     */
    void onError(String error);

    /**
     * 检查更新成功，返回一个更新列表
     *
     * @param hasUpdate  true表示有更新， false表示没有更新、false时updateList为空
     * @param updateList 更新列表
     */
    void onSuccess(boolean hasUpdate, ArrayList<KSCUpdateInfo> updateList);

}

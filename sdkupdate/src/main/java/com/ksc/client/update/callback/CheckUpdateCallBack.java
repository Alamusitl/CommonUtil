package com.ksc.client.update.callback;

import com.ksc.client.update.entity.KSCUpdateInfo;

import java.util.ArrayList;

/**
 * Created by Alamusi on 2016/7/26.
 */
public interface CheckUpdateCallBack {

    /**
     * 检查更新失败或更新失败
     *
     * @param error 错误信息
     */
    void onError(String error);

    /**
     * 检查更新成功，返回一个更新情况
     *
     * @param hasUpdate  true表示有更新， false表示没有更新
     * @param updateList 更新列表
     * @return 是否更新, KSCUpdate.EVENT_UPDATE_START开始更新/KSCUpdate.EVENT_UPDATE_CANCEL取消更新
     */
    int onSuccess(boolean hasUpdate, ArrayList<KSCUpdateInfo> updateList);

}

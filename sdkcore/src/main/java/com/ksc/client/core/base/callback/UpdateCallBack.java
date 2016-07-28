package com.ksc.client.core.base.callback;

/**
 * Created by Alamusi on 2016/7/26.
 */
public interface UpdateCallBack {

    /**
     * 检查更新失败或更新失败
     *
     * @param error 错误信息
     */
    void onError(String error);

    /**
     * 检查更新成功，返回一个更新情况
     *
     * @param hasUpdate true表示有更新， false表示没有更新
     * @param version   更新版本号
     * @param force     是否强制更新
     * @param msg       更新信息
     * @return 是否更新, KSCUpdate.EVENT_UPDATE_START开始更新/KSCUpdate.EVENT_UPDATE_CANCEL取消更新
     */
    int onSuccess(boolean hasUpdate, String version, boolean force, String msg);

    /**
     * 单个开始更新
     *
     * @param version 更新版本号
     * @param force   是否强制更新
     * @param msg     更新信息
     * @return
     */
    int onStart(String version, boolean force, String msg);

    /**
     * 单个更新完成
     *
     * @param version 更新版本
     * @return
     */
    int onFinish(String version);

    /**
     * 所有的更新完成
     *
     * @return
     */
    int onOver();
}

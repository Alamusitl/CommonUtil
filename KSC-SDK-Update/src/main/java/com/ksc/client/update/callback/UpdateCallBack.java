package com.ksc.client.update.callback;

/**
 * Created by Alamusi on 2016/7/29.
 */
public interface UpdateCallBack {

    /**
     * 开始更新
     *
     * @param name      更新包名称
     * @param totalSize 更新包大小, 单位为字节
     * @param Size      转化为字符串的更新包大小, B、KB、MB
     */
    void onUpdateStart(String name, int totalSize, String Size);

    /**
     * 下载进度
     *
     * @param name        更新包名称
     * @param currentSize 当前下载进度大小
     * @param Size        当前下载的更新包大小
     * @param present     当前下载的比例
     */
    void onUpdating(String name, int currentSize, String Size, float present);

    /**
     * 更新错误
     *
     * @param name 更新包名称
     */
    void onUpdateError(String name);

    /**
     * 取消更新
     *
     * @param name 更新包名称
     */
    void onUpdateCancel(String name);

    /**
     * 更新完成
     *
     * @param name 更新包名称
     */
    void onUpdateFinish(String name);

    /**
     * 所有更新结束
     */
    void onUpdateOver();
}

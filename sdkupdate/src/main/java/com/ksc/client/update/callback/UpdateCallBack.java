package com.ksc.client.update.callback;

/**
 * Created by Alamusi on 2016/7/29.
 */
public interface UpdateCallBack {

    /**
     * 开始更新
     */
    void onStartUpdate();

    /**
     * 下载进度
     */
    void onProcess(int present);

    /**
     * 更新错误
     */
    void onError(String version);

    /**
     * 更新完成
     */
    void onFinishUpdate();
}

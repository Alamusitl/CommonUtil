package com.ksc.client.ads.callback;

/**
 * 广告事件监听器
 * Created by Alamusi on 2016/8/23.
 */
public interface KSCAdEventListener {

    /**
     * 是否有广告
     *
     * @param isAdExist 有广告为true，反之false
     */
    void onAdExist(boolean isAdExist);

    /**
     * 广告视频是否缓存了
     *
     * @param isCached 已缓存为true，反之false
     */
    void onVideoCached(boolean isCached);

    /**
     * 开始播放视频
     */
    void onVideoStart();

    /**
     * 视频播放完成
     */
    void onVideoCompletion();

    /**
     * 关闭广告
     *
     * @param currentPosition 当前播放进度
     */
    void onVideoClose(int currentPosition);

    /**
     * 视频播放错误
     *
     * @param reason 错误原因
     */
    void onVideoError(String reason);

    /**
     * 显示落地页成功
     *
     * @param showSuccess 是否显示成功
     */
    void onLoadingPageShow(boolean showSuccess);

    /**
     * 落地页关闭
     */
    void onLoadingPageClose();

    /**
     * 网络错误
     *
     * @param error
     */
    void onNetRequestError(String error);
}

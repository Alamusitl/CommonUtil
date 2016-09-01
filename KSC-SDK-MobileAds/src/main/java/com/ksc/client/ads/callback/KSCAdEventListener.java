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
     * @param code      错误码
     */
    void onAdExist(boolean isAdExist, long code);

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
     * 落地页关闭
     *
     * @param status 直接关闭为false，点击关闭为true
     */
    void onLandingPageClose(boolean status);

    /**
     * 网络错误
     *
     * @param error 错误原因
     */
    void onNetRequestError(String error);
}

package com.ksc.client.ads.callback;

/**
 * 视频播放进度回调
 * Created by Alamusi on 2016/8/18.
 */
public interface KSCVideoPlayCallBack {

    /**
     * 视频Prepared完成
     */
    void onPrepared();

    /**
     * 开始播放视频
     */
    void onStart();

    /**
     * 全屏播放视频
     */
    void onFullScreen();

    /**
     * 视频播放完成
     */
    void onCompletion();

    /**
     * 视频播放错误
     *
     * @param what  错误Code
     * @param extra 错误extra
     */
    void onError(int what, int extra);

    /**
     * 视频播放器错误
     *
     * @param errorMsg 错误信息
     */
    void onMediaPlayerError(String errorMsg);

}

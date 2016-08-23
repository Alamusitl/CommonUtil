package com.ksc.client.ads.callback;

import android.media.MediaPlayer;

/**
 * 视频播放进度回调
 * Created by Alamusi on 2016/8/18.
 */
public interface KSCVideoPlayCallBack {

    // 视频准备完成
    void onPrepared();

    // 开始播放
    void onStart();

    // 全屏播放
    void onFullScreen();

    // 播放完成
    void onCompletion(MediaPlayer mediaPlayer);

    // 错误
    void onError(MediaPlayer mediaPlayer, int what, int extra);

}

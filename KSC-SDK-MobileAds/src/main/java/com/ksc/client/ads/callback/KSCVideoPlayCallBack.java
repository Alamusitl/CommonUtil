package com.ksc.client.ads.callback;

import android.media.MediaPlayer;

/**
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

    // 点击广告
    void onClickAd();

    // 关闭视频
    void onCloseVideo(int progress);
}

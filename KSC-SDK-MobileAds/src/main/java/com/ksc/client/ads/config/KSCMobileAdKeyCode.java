package com.ksc.client.ads.config;

/**
 * Created by Alamusi on 2016/8/22.
 */
public class KSCMobileAdKeyCode {

    public static final String VIDEO_TYPE = "videoType";
    public static final String VIDEO_PATH = "videoPath";
    public static final String VIDEO_H5_PATH = "videoH5Path";
    public static final String VIDEO_IN_CACHE = "videoInCache";
    public static final String VIDEO_IN_STREAM = "videoInStream";

    public static final String IMG_VIDEO_VIEW_CLOSE = "ksc_controller_close.png";
    public static final String IMG_VIDEO_VIEW_MUTE = "ksc_controller_mute.png";
    public static final String IMG_VIDEO_VIEW_VOLUME_RESUME = "ksc_controller_volume_resume.png";

    public static final String MOBILE_AD_URL = "http://120.92.44.245/api/def";

    /**
     * 视频准备完成
     */
    public static final int KEY_VIDEO_PREPARED = 0;
    /**
     * 视频开始播放
     */
    public static final int KEY_VIDEO_START = 1;
    /**
     * 视频正在播放
     */
    public static final int KEY_VIDEO_PLAYING = 2;
    /**
     * 视频暂停播放
     */
    public static final int KEY_VIDEO_PAUSE = 3;
    /**
     * 视频恢复播放
     */
    public static final int KEY_VIDEO_RESUME = 4;
    /**
     * 视频关闭
     */
    public static final int KEY_VIDEO_CLOSE = 5;
    /**
     * 视频播放完成
     */
    public static final int KEY_VIDEO_COMPLETION = 6;
    /**
     * 视频静音或取消静音
     */
    public static final int KEY_VIDEO_MUTE = 7;
    /**
     * 视频播放错误
     */
    public static final int KEY_VIDEO_ERROR = 8;
    /**
     * 弹出关闭按钮
     */
    public static final int KEY_VIEW_SHOW_VIDEO_CLOSE = 9;
    /**
     * 显示关闭视频弹框
     */
    public static final int KEY_VIEW_SHOW_CLOSE_CONFIRM = 10;
    /**
     * 关闭落地页
     */
    public static final int KEY_VIEW_H5_CLOSE = 11;
    /**
     * H5界面点击下载
     */
    public static final int KEY_VIEW_H5_CLICK = 12;
    /**
     * 开始下载
     */
    public static final int KEY_DOWNLOAD_START = 13;
    /**
     * 下载成功
     */
    public static final int KEY_DOWNLOAD_SUCCESS = 14;
    /**
     * 下载失败
     */
    public static final int KEY_DOWNLOAD_FAIL = 15;

}

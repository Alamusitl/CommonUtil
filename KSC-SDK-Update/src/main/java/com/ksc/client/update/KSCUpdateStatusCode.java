package com.ksc.client.update;

/**
 * Created by Alamusi on 2016/8/8.
 */
public class KSCUpdateStatusCode {

    public static final int EVENT_UPDATE_START = 1003;// 开始更新
    public static final int EVENT_UPDATE_CANCEL = 1004;// 取消更新
    public static final int EVENT_UPDATE_ERROR = 1005;// 更新错误
    public static final int EVENT_UPDATE_DOWNLOAD_START = 1006;// 开始下载
    public static final int EVENT_UPDATE_DOWNLOAD_BACKGROUND = 1007;// 后台下载
    public static final int EVENT_UPDATE_DOWNLOADING = 1008;// 正在下载
    public static final int EVENT_UPDATE_DOWNLOAD_FAIL = 1009;// 下载失败
    public static final int EVENT_UPDATE_DOWNLOAD_FINISH = 1010;// 下载完成
    public static final int EVENT_UPDATE_DOWNLOAD_STOP = 1011;// 停止下载
    public static final int EVENT_UPDATE_FINISH = 1012;// 更新完成
    public static final int EVENT_UPDATE_OVER = 1013;// 更新结束
    protected static final int EVENT_UPDATE_HAS_UPDATE = 1000;// 检查有更新
    protected static final int EVENT_UPDATE_NO_UPDATE = 1001;// 检查无更新
    protected static final int EVENT_UPDATE_CHECK_FAIL = 1002;// 检查失败
}

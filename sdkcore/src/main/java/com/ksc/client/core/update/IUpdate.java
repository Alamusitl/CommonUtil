package com.ksc.client.core.update;

import android.app.Activity;

import com.ksc.client.update.callback.CheckUpdateCallBack;
import com.ksc.client.update.callback.UpdateCallBack;
import com.ksc.client.update.entity.KSCUpdateInfo;

import java.util.ArrayList;

/**
 * Created by Alamusi on 2016/8/1.
 */
public interface IUpdate {

    /**
     * 检查更新
     *
     * @param activity            上下文
     * @param resourcePath        资源更新路径
     * @param isUseSelf           是否使用自己的更新视图
     * @param checkUpdateCallBack 检查更新回调
     */
    void checkUpdate(Activity activity, String resourcePath, boolean isUseSelf, CheckUpdateCallBack checkUpdateCallBack);

    /**
     * 开始更新
     *
     * @param activity       上下文
     * @param updateInfoList 需要更新的信息List
     * @param updateCallBack 更新回调
     */
    void startUpdate(Activity activity, ArrayList<KSCUpdateInfo> updateInfoList, UpdateCallBack updateCallBack);
}

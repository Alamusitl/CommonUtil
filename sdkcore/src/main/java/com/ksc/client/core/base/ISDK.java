package com.ksc.client.core.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.ksc.client.core.base.callback.ExitCallBack;
import com.ksc.client.core.base.callback.InitCallBack;
import com.ksc.client.core.base.callback.LoginCallBack;
import com.ksc.client.core.base.callback.LogoutCallBack;
import com.ksc.client.core.base.callback.PayCallBack;
import com.ksc.client.core.base.callback.SwitchAccountCallBack;
import com.ksc.client.core.base.entity.AppInfo;
import com.ksc.client.core.base.entity.PayInfo;
import com.ksc.client.core.base.entity.RoleInfo;

/**
 * Created by Alamusi on 2016/6/21.
 */
public interface ISDK {

    /*********
     * 必须实现的SDK接口
     *********/
    /**
     * 初始化SDK
     *
     * @param activity     当前Activity
     * @param appInfo      应用信息
     * @param initCallBack 初始化回调接口。SDK会在UI线程将初始化结果通过此对象回调给游戏
     */
    void init(Activity activity, AppInfo appInfo, InitCallBack initCallBack);

    /**
     * 登录接口
     *
     * @param activity      当前Activity
     * @param loginCallBack 登录结果回调对象。SDK会在UI线程将登录结果通过此对象回调给游戏
     */
    void login(Activity activity, LoginCallBack loginCallBack);

    /**
     * 登出接口
     *
     * @param activity       当前Activity
     * @param logoutCallBack 登出结果回调对象。SDK会在UI线程将登出结果通过此对象回调给游戏
     */
    void logout(Activity activity, LogoutCallBack logoutCallBack);

    /**
     * 支付接口
     *
     * @param activity    当前Activity
     * @param payInfo     支付信息，请参考PayInfo类定义
     * @param payCallBack 支付结果回调对象。SDK会在主线程（UI线程）中将支付结果通过此对象回调给游戏
     */
    void pay(final Activity activity, final PayInfo payInfo, final PayCallBack payCallBack);

    /**
     * 拉起退出界面（如果渠道有退出界面，会调用渠道的退出界面）
     *
     * @param activity     当前活跃的Activity
     * @param exitCallBack 退出回调对象。SDK会在主线程（UI线程)中调用此对象的方法退出游戏
     */
    void exit(Activity activity, ExitCallBack exitCallBack);

    /**
     * 获得当前的渠道ID
     *
     * @return 当前的渠道ID，具体渠道编码请参考西瓜文档中心
     */
    String getChannelID();

    /**
     * 获得当前的版本号
     *
     * @return 当前的版本ID
     */
    String getVersion();

    /**
     * 获得当前登录账号的验证信息
     *
     * @return
     */
    String getAuthInfo();

    /*********
     * Activity接口(建议采用继承SDK提供的Activity来调用)
     **********/
    void onCreate(Activity activity);

    void onStart(Activity activity);

    void onRestart(Activity activity);

    void onResume(Activity activity);

    void onPause(Activity activity);

    void onStop(Activity activity);

    void onDestroy(Activity activity);

    void onNewIntent(Activity activity, Intent intent);

    void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data);

    void onBackPressed(Activity activity);

    void onConfigurationChanged(Activity activity, Configuration newConfig);

    void onSaveInstanceState(Activity activity, Bundle outState);

    /********
     * Application接口(建议采用继承SDK提供的Application来调用)
     *********/
    void onApplicationCreate(final Context context);

    void onApplicationAttachBaseContext(final Context context);

    void onApplicationTerminate(final Context context);

    /*********
     * 渠道扩展接口
     *********/
    /**
     * 切换账号
     *
     * @param activity              当前Activity
     * @param switchAccountCallBack 切换账号的回调
     */
    void switchAccount(Activity activity, SwitchAccountCallBack switchAccountCallBack);

    /**
     * 打开渠道提供的用户中心（在渠道支持的情况下)
     *
     * @param activity 当前Activity
     */
    void openUserCenter(Activity activity);

    /**
     * 测试上述的扩展方法，渠道是否支持，目前保留
     *
     * @param methodName - 方法名(openUserCenter/switchAccount等)
     * @return true - 渠道支持此方法
     */
    boolean isMethodSupport(String methodName);

    /**
     * 建议在玩家新创建游戏角色的时候，调用此接口，上报角色数据。
     *
     * @param roleInfo -  角色信息，请参考RoleInfo的定义
     */
    void onCreateRole(RoleInfo roleInfo);

    /**
     * 建议在登录完成后，进入游戏界面时，调用此接口，上报角色数据
     *
     * @param roleInfo － 角色信息，请参考RoleInfo的定义
     */
    void onEnterGame(RoleInfo roleInfo);

    /**
     * 建议在角色等级升级时，调用此接口，上报角色数据
     *
     * @param roleInfo － 角色信息，请参考RoleInfo的定义
     */
    void onRoleLevelUp(RoleInfo roleInfo);
}

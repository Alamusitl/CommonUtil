package com.ksc.sdk.demo;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ksc.client.core.KSCSDK;
import com.ksc.client.core.base.activity.KSCActivity;
import com.ksc.client.core.base.callback.ExitCallBack;
import com.ksc.client.core.base.callback.InitCallBack;
import com.ksc.client.core.base.callback.LoginCallBack;
import com.ksc.client.core.base.callback.LogoutCallBack;
import com.ksc.client.core.base.callback.PayCallBack;
import com.ksc.client.core.base.callback.SwitchAccountCallBack;
import com.ksc.client.core.base.entity.AppInfo;
import com.ksc.client.core.base.entity.PayInfo;
import com.ksc.client.toolbox.HttpError;
import com.ksc.client.toolbox.HttpErrorListener;
import com.ksc.client.toolbox.HttpListener;
import com.ksc.client.toolbox.HttpRequestManager;
import com.ksc.client.toolbox.HttpRequestParam;
import com.ksc.client.toolbox.HttpResponse;
import com.ksc.client.util.KSCHelpUtils;
import com.ksc.client.util.KSCLog;

import java.net.HttpURLConnection;

public class MainActivity extends KSCActivity implements View.OnClickListener {

    private InitCallBack mInitCallBack = null;
    private LoginCallBack mLoginCallBack = null;
    private LogoutCallBack mLogoutCallBack = null;
    private SwitchAccountCallBack mSwitchAccountCallBack = null;
    private PayCallBack mPayCallBack = null;
    private ExitCallBack mExitCallBack = null;

    private TextView mTvLoginResult;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        setListener();
        AppInfo appInfo = new AppInfo();
        appInfo.setAppId(SDKConfig.APPID);
        appInfo.setAppKey(SDKConfig.APPKEY);
        appInfo.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        KSCSDK.getInstance().init(this, appInfo, mInitCallBack);
        KSCSDK.getInstance().setDebug(true);
    }

    private void initView() {
        mTvLoginResult = (TextView) findViewById(R.id.tv_loginResult);
    }

    private void initData() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    private void setListener() {
        mInitCallBack = new InitCallBack() {
            @Override
            public void onInitFail(int code, String msg) {
                KSCLog.i("init fail :" + code + ":" + msg);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "初始化失败", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onInitSuccess(int code, String msg) {
                KSCLog.i("init success :" + code + ":" + msg);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "初始化成功", Toast.LENGTH_LONG).show();
                    }
                });
            }
        };
        mLoginCallBack = new LoginCallBack() {
            @Override
            public void onLoginSuccess(int code, String msg) {
                KSCLog.i("login success :" + code + ", " + msg);
                verifyLoginToken(KSCSDK.getInstance().getAuthInfo());
            }

            @Override
            public void onLoginFail(int code, String msg) {
                KSCLog.i("login fail :" + code + ", " + msg);
            }

            @Override
            public void onLoginCancel(int code, String msg) {
                KSCLog.i("login cancel :" + code + ", " + msg);
            }
        };
        mLogoutCallBack = new LogoutCallBack() {
            @Override
            public void onLogoutSuccess(int code, String msg) {
                KSCLog.i("logout success :" + code + ", " + msg);
            }

            @Override
            public void onLogoutFail(int code, String msg) {
                KSCLog.i("logout fail :" + code + ", " + msg);
            }
        };
        mSwitchAccountCallBack = new SwitchAccountCallBack() {
            @Override
            public void onSwitchAccountSuccess(int code, String msg) {
                KSCLog.i("switch success :" + code + ":" + msg);
            }

            @Override
            public void onSwitchAccountFail(int code, String msg) {
                KSCLog.i("switch fail :" + code + ":" + msg);
            }

            @Override
            public void onSwitchAccountCancel(int code, String msg) {
                KSCLog.i("switch cancel :" + code + ":" + msg);
            }
        };
        mPayCallBack = new PayCallBack() {
            @Override
            public void onPaySuccess(PayInfo payInfo, int code, String msg) {
                KSCLog.i("pay success :" + code + ", " + msg);
            }

            @Override
            public void onPayFail(PayInfo payInfo, int code, String msg) {
                KSCLog.i("pay fail :" + code + ", " + msg);
            }

            @Override
            public void onPayCancel(PayInfo payInfo, int code, String msg) {
                KSCLog.i("pay cancel :" + code + ", " + msg);
            }

            @Override
            public void onPayOthers(PayInfo payInfo, int code, String msg) {
                KSCLog.i("pay other :" + code + ", " + msg);
            }

            @Override
            public void onPayProgress(PayInfo payInfo, int code, String msg) {
                KSCLog.i("pay progress :" + code + ", " + msg);
            }
        };
        mExitCallBack = new ExitCallBack() {
            @Override
            public void doExit() {
                finish();
            }

            @Override
            public void onNoChannelExit() {
                finish();
            }
        };
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                KSCSDK.getInstance().login(this, mLoginCallBack);
                break;
            case R.id.btnLogout:
                KSCSDK.getInstance().logout(this, mLogoutCallBack);
                break;
            case R.id.btnPay:
                PayInfo payInfo = new PayInfo();
                payInfo.setPrice("1");
                payInfo.setOrder(String.valueOf(System.currentTimeMillis()));
                payInfo.setProductId("1");
                payInfo.setProductName("test");
                payInfo.setProductDest("测试");
                payInfo.setProductQuantity(1);
                payInfo.setProductUnit("金币");
                payInfo.setCurrencyName("CNY");
                payInfo.setCustomInfo("Test");
                payInfo.setRate("10");
                payInfo.setUid("123456789");

                payInfo.setRoleId("123456789");
                payInfo.setRoleName("test");
                payInfo.setRoleLevel("1");
                payInfo.setRoleVip("1");
                payInfo.setZoneId("1");
                payInfo.setZoneName("1");
                KSCSDK.getInstance().pay(this, payInfo, mPayCallBack);
                break;
            case R.id.btnSwitchAccount:
                KSCSDK.getInstance().switchAccount(this, mSwitchAccountCallBack);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            KSCSDK.getInstance().exit(this, mExitCallBack);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 登录验证，模拟服务器操作
     */
    private void verifyLoginToken(String token) {
        String url = "http://192.168.116.104:8080/springmvc/channel/login/?r=";
        String param = "gameid=" + SDKConfig.APPID + "&channelid=" + KSCSDK.getInstance().getChannelID() + "&sessionid=" + token;
        final String key = "5dr7WEb2fo20ZF9U";
        url += KSCHelpUtils.encodeParam(param, key);
        KSCLog.d(url);
        HttpRequestParam requestParam = new HttpRequestParam(url);
        HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(final HttpResponse response) {
                if (response.getCode() == HttpURLConnection.HTTP_OK) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mTvLoginResult.setText("登录验证成功：" + KSCHelpUtils.decodeParam(response.getBodyString(), key));
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mTvLoginResult.setText("登录验证失败:" + KSCHelpUtils.decodeParam(response.getBodyString(), key));
                        }
                    });
                }
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mTvLoginResult.setText("登录验证异常");
                            }
                        });
                    }
                });
            }
        });
    }
}

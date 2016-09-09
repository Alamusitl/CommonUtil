package com.ksc.mobile.ads.demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.ksc.client.ads.KSCADAgent;
import com.ksc.client.ads.callback.KSCAdEventListener;

public class MainActivity extends Activity {

    private Button mShowVideo;
    private TextView mTvRest;
    private TextView mLogMsg;
    private int mCurrentRest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mShowVideo = (Button) findViewById(R.id.btnShowVideoAd);
        mTvRest = (TextView) findViewById(R.id.tvRest);
        mLogMsg = (TextView) findViewById(R.id.tvLogMsg);
        mLogMsg.setMovementMethod(new ScrollingMovementMethod());
        mShowVideo.setEnabled(false);
        mShowVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KSCADAgent.getInstance().showAdVideo(MainActivity.this);
            }
        });

        KSCADAgent.getInstance().setDebug(true);// 正式发布时请改为false，true时调试模式，将打印debug log
        String appId = "4qby7kup";// 测试参数， 正式请替换自己的渠道
        String adSlotId = "pe2otiaf";// 测试参数，正式请替换自己的参数
        KSCADAgent.getInstance().init(this, appId, adSlotId, new KSCAdEventListener() {

            @Override
            public void onAdExist(boolean isAdExist, long code) {
                if (isAdExist) {
                    Toast("有广告");
                } else {
                    Toast("没有广告");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mShowVideo.setEnabled(true);
                        }
                    });
                }
            }

            @Override
            public void onVideoCached(boolean isCached) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mShowVideo.setEnabled(true);
                    }
                });
                if (isCached) {
                    Toast("已缓存广告视频");
                } else {
                    Toast("缓存广告视频失败");
                }
            }

            @Override
            public void onVideoStart() {
                Toast("开始播放");
            }

            @Override
            public void onVideoCompletion() {
                Toast("播放完成");
                mCurrentRest = Integer.parseInt(String.valueOf(mTvRest.getText())) + 20;
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvRest.setText(String.valueOf(mCurrentRest));
                    }
                });
                Toast("奖励已发放");
            }

            @Override
            public void onVideoClose(int currentPosition) {
                Toast("关闭广告视频，当前进度[" + currentPosition / 1000 + "]秒");
            }

            @Override
            public void onVideoError(String reason) {
                Toast("视频播放错误，错误信息[" + reason + "]");
            }

            @Override
            public void onLandingPageClose(boolean status) {
                Toast("落地页关闭");
            }

            @Override
            public void onNetRequestError(String error) {
                Toast("网络请求错误，错误信息[" + error + "]");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        KSCADAgent.getInstance().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        KSCADAgent.getInstance().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KSCADAgent.getInstance().onDestroy();
    }

    private void Toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLogMsg.setText(mLogMsg.getText() + "\n" + msg);
            }
        });
    }

}

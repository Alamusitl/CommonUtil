package com.ksc.mobile.ads.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.ksc.client.ads.KSCADAgent;
import com.ksc.client.ads.callback.KSCAdEventListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        KSCADAgent.getInstance().setDebug(true);
        String appId = "ayce05f9";// 测试参数， 正式请替换自己的渠道
        String adSlotId = "47435394";// 测试参数，正式请替换自己的参数
        KSCADAgent.getInstance().init(this, appId, adSlotId, new KSCAdEventListener() {

            @Override
            public void onAdExist(boolean isAdExist, long code) {
                if (isAdExist) {
                    Toast("有广告");
                } else {
                    Toast("没有广告");
                }
            }

            @Override
            public void onVideoCached(boolean isCached) {
                if (isCached) {
                    Toast("已缓存广告视频");
                } else {
                    Toast("缓存广告视频失败");
                }
            }

            @Override
            public void onVideoStart() {
//                Toast("开始播放");
            }

            @Override
            public void onVideoCompletion() {
                Toast("播放完成");
            }

            @Override
            public void onVideoClose(int currentPosition) {
                Toast("关闭广告视频，当前进度[" + currentPosition / 1000 + "]");
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

        findViewById(R.id.btnShowVideoAd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KSCADAgent.getInstance().showAdVideo(MainActivity.this);
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
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

}

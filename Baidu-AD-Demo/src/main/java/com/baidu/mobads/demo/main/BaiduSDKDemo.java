package com.baidu.mobads.demo.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baidu.mobads.BaiduManager;
import com.baidu.mobads.demo.main.basevideo.PrerollActivity;
import com.baidu.mobads.demo.main.feeds.HTMLFeedChuChuangActivity;
import com.baidu.mobads.demo.main.feeds.HTMLFeedLunBoActivity;
import com.baidu.mobads.demo.main.feeds.ListViewActivity;
import com.baidu.mobads.demo.main.feeds.NativeOriginActivity;
import com.baidu.mobads.demo.main.feeds.VideoFeedActivity;

public class BaiduSDKDemo extends Activity {

    public static final String TAG = "BaiduSDKDemo";

    Button simpleCoding;
    Button simpleVideo;
    Button simpleInter;
    Button simpleInterForVideoApp;
    Button prerollVideo;
    Button simpleIcon;
    Button simpleRecomAd;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // 广告展现前先调用sdk初始化方法，可以有效缩短广告第一次展现所需时间
        BaiduManager.init(this);

        Button btnListView = (Button) this.findViewById(R.id.btnList);
        btnListView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开启信息流广告形式
                startActivity(new Intent(BaiduSDKDemo.this, ListViewActivity.class));
            }

        });
        Button btnOrigin = (Button) this.findViewById(R.id.btnOrigin);
        btnOrigin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开启原生字段形式
                startActivity(new Intent(BaiduSDKDemo.this, NativeOriginActivity.class));
            }

        });

        Button btnVideoFeed = (Button) this.findViewById(R.id.btnVideoFeed);
        btnVideoFeed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开启非WiFi下二次确认下载activity
                startActivity(new Intent(BaiduSDKDemo.this, VideoFeedActivity.class));
            }
        });

        Button btnHtmlFeedLunBo = (Button) this.findViewById(R.id.btnHTMLFeedLunBo);
        btnHtmlFeedLunBo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BaiduSDKDemo.this, HTMLFeedLunBoActivity.class);
                startActivity(intent);
            }

        });

        Button btnHtmlFeedChuChuang = (Button) this.findViewById(R.id.btnHTMLFeedChuChuang);
        btnHtmlFeedChuChuang.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BaiduSDKDemo.this, HTMLFeedChuChuangActivity.class);
                startActivity(intent);
            }

        });


        simpleCoding = (Button) findViewById(R.id.simple_coding);
        simpleCoding.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(simpleCoding.getContext(), BannerAdActivity.class);
                startActivity(intent);
            }
        });

        simpleInter = (Button) findViewById(R.id.simple_inters);
        simpleInter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(simpleInter.getContext(), InterstitialAdActivity.class);
                startActivity(intent);
            }
        });

        simpleInterForVideoApp = (Button) findViewById(R.id.simple_inters_for_videoapp);
        simpleInterForVideoApp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(simpleInterForVideoApp.getContext(),
                        InterstitialAdForVideoAppActivity.class);
                startActivity(intent);
            }
        });

        prerollVideo = (Button) findViewById(R.id.preroll);
        prerollVideo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String url = "http://211.151.146.65:8080/wlantest/shanghai_sun/Cherry/dahuaxiyou.mp4";
                Intent intent = new Intent(simpleInterForVideoApp.getContext(), PrerollActivity.class);
                intent.putExtra(PrerollActivity.EXTRA_CONTENT_VIDEO_URL, url);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        Button simpleNRLM = (Button) findViewById(R.id.nrlm);
        simpleNRLM.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(simpleInterForVideoApp.getContext(), CpuAdActivity.class);
                startActivity(intent);
            }
        });

        Button dubao = (Button) findViewById(R.id.dubao);
        dubao.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(simpleInterForVideoApp.getContext(), DubaoActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");

        // 您可以在这里执行您的业务逻辑，比如发统计给服务器，让服务器统计退出概率， 或者APP运行时长.

        /**
         * 百度广告联盟建议您在退出APP前做两件事情
         *
         * 1. 通过BaiduXAdSDKContext.exit()来告知AdSDK，以便AdSDK能够释放资源.
         *
         * 2. 使用下面两行代码种的任意一行来冷酷无情的强制退出当前进程，以确保App本身资源得到释放。
         *      android.os.Process.killProcess(android.os.Process.myPid());
         *      System.exit(0);
         */
        // baidu-xadsdk will release all resource
        com.baidu.mobads.production.BaiduXAdSDKContext.exit();
        // kill current process
        // android.os.Process.killProcess(android.os.Process.myPid());
        // System.exit(0);

        super.onDestroy();
    }
}
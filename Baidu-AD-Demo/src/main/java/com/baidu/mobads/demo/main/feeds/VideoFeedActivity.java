package com.baidu.mobads.demo.main.feeds;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.baidu.mobad.feeds.BaiduNative;
import com.baidu.mobad.feeds.BaiduNative.BaiduNativeNetworkListener;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobad.feeds.NativeResponse.MaterialType;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.demo.main.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class VideoFeedActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "VideoFeedActivity";
    // private static String YOUR_AD_PLACE_ID = "2154834"; // 双引号中填写自己的广告位ID
    private static String YOUR_AD_PLACE_ID = "2362913";
    private static int CHECK_VISIBILITY = 0;
    FSVideoLayout videoLayout;
    AQuery aq;
    FrameLayout container;
    boolean firstPlay = true;
    List<NativeResponse> nrAdList = new ArrayList<NativeResponse>();
    private RelativeLayout rl;
    private NativeResponse mNrAd;
    private View mPlayBtnView;
    private boolean isAutoPlay = false;
    private boolean isReady = false;
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == CHECK_VISIBILITY) {
                startConfig(mNrAd);
                isReady = true;
                try {
                    int showState = VisibilityChecker.getViewState(container);
                    if (showState == VisibilityChecker.SHOW_STATE_SHOW) {

                        mNrAd.recordImpression(container);

                        if (isAutoPlay) {
                            mPlayBtnView.setVisibility(View.GONE);
                            videoLayout.setVisibility(View.VISIBLE);
                            realPlay();
                        }
                    } else {
                        myHandler.sendEmptyMessageDelayed(CHECK_VISIBILITY, 1000);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            super.handleMessage(msg);
        }
    };
    private float density;
    private FSVideoLayout.VideoPlayCallbackImpl mVideoPlayCallback = new FSVideoLayout.VideoPlayCallbackImpl() {
        @Override
        public void onCloseVideo(int progress) {
            if (mNrAd != null) {
                mNrAd.onClose(VideoFeedActivity.this, progress);
            }
        }

        @Override
        public void onFullScreen(int progress) {
            if (mNrAd != null) {
                mNrAd.onFullScreen(VideoFeedActivity.this, progress);
            }
        }

        @Override
        public void onStart() {
            if (mNrAd != null) {
                mNrAd.onStart(VideoFeedActivity.this);
            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mNrAd != null) {
                mNrAd.onComplete(VideoFeedActivity.this);
            }
            mPlayBtnView.setVisibility(View.VISIBLE);
            videoLayout.setVisibility(View.GONE);
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                videoLayout.toggleFullScreen();
            }
        }

        @Override
        public void onError(MediaPlayer mp, int what, int extra) {
            if (mNrAd != null) {
                mNrAd.onError(VideoFeedActivity.this, what, extra);
            }
        }

        @Override
        public void onClickAd() {
            if (mNrAd != null) {
                mNrAd.onClickAd(VideoFeedActivity.this);
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videofeed);
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        density = metric.density;

        videoLayout = (FSVideoLayout) findViewById(R.id.videoview);
        videoLayout.setActivity(this);

        mPlayBtnView = findViewById(R.id.play_btn);
        mPlayBtnView.setOnClickListener(this);
        container = (FrameLayout) findViewById(R.id.container);
        videoLayout.setVideoPlayCallback(mVideoPlayCallback);

        rl = (RelativeLayout) findViewById(R.id.native_outer_view);

        fetchAd(this);
    }

    private void startConfig(NativeResponse ad) {
        configAd(ad);
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNrAd.handleClick(v, videoLayout.getCurrentPosition());
            }
        });
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        videoLayout.pause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mPlayBtnView.setVisibility(View.VISIBLE);
        videoLayout.setVisibility(View.GONE);

        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            videoLayout.toggleFullScreen();
        }
    }

    void realPlay() {
        Uri videoUri = Uri.parse(mNrAd.getVideoUrl());
        try {
            videoLayout.setVideoURI(videoUri);
            firstPlay = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fetchAd(Activity activity) {
        /**
         * Step 1. 创建BaiduNative对象，参数分别为：
         * 上下文context，广告位ID, BaiduNativeNetworkListener监听（监听广告请求的成功与失败）
         * 注意：请将YOUR_AD_PALCE_ID替换为自己的广告位ID
         */
        BaiduNative baidu = new BaiduNative(activity, YOUR_AD_PLACE_ID,
                new BaiduNativeNetworkListener() {

                    @Override
                    public void onNativeFail(NativeErrorCode arg0) {
                        Toast.makeText(VideoFeedActivity.this, "没有收到视频广告，请检查", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNativeLoad(List<NativeResponse> arg0) {
                        if (arg0 != null && arg0.size() > 0) {
                            nrAdList = arg0;

                            mNrAd = nrAdList.get(0);

                            android.util.Log.i(TAG, "mNrAd.getMaterialType() is " + mNrAd.getMaterialType());
                            if (mNrAd.getMaterialType() == MaterialType.VIDEO) {
                                Toast.makeText(VideoFeedActivity.this, "收到视频广告，开始播放", Toast.LENGTH_LONG).show();

                                android.util.Log.i(TAG, "send message ");
                                myHandler.sendEmptyMessage(CHECK_VISIBILITY);
                            } else {
                                Toast.makeText(VideoFeedActivity.this, "收到广告，但不是视频广告，请检查", Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                });

        /**
         * Step 2. 创建requestParameters对象，并将其传给baidu.makeRequest来请求广告
         */
        RequestParameters requestParameters = new RequestParameters.Builder()
                .setWidth((int) (640 * density))
                .setHeight((int) (360 * density))
                .downloadAppConfirmPolicy(
                        RequestParameters.DOWNLOAD_APP_CONFIRM_ONLY_MOBILE) // 用户点击下载类广告时，是否弹出提示框让用户选择下载与否
                .build();

        baidu.makeRequest(requestParameters);
    }

    private void configAd(NativeResponse ad) {
        aq = new AQuery(VideoFeedActivity.this);
        videoLayout.configTipView(ad.isDownloadApp());
        videoLayout.setTipViewClickedListener(new FSVideoLayout.TipViewClickedListener() {

            @Override
            public void onTipViewClicked(View v) {
                mNrAd.handleClick(v, videoLayout.getCurrentPosition());
            }

        });
        aq.id(R.id.big_pic).image(ad.getImageUrl(), false, true);
        aq.id(R.id.native_icon_image).image(ad.getIconUrl(), false, true);
        aq.id(R.id.native_text).text(ad.getDesc());
        aq.id(R.id.native_title).text(ad.getTitle());
    }

    @Override
    public void onClick(View v) {
        if (isReady) {
            mPlayBtnView.setVisibility(View.GONE);
            videoLayout.setVisibility(View.VISIBLE);
            if (firstPlay) {
                realPlay();
            } else {
                videoLayout.start();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                videoLayout.toggleFullScreen();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

}


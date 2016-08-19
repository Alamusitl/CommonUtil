package com.baidu.mobads.demo.main.basevideo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnInfoListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baidu.mobad.video.XAdManager;
import com.baidu.mobads.demo.main.R;
import com.baidu.mobads.interfaces.IXAdConstants4PDK;
import com.baidu.mobads.interfaces.IXAdConstants4PDK.SlotState;
import com.baidu.mobads.interfaces.IXAdContext;
import com.baidu.mobads.interfaces.IXAdManager;
import com.baidu.mobads.interfaces.IXAdProd;
import com.baidu.mobads.openad.interfaces.event.IOAdEvent;
import com.baidu.mobads.openad.interfaces.event.IOAdEventListener;

/**
 * Implements a very simple video player that handles preroll advertising.
 *
 * @author baiduer
 */
@SuppressLint("NewApi")
public class PrerollActivity extends Activity implements MediaPlayer.OnErrorListener {

    public static final String TAG = "PrerollActivity";

    public static final String EXTRA_CONTENT_VIDEO_URL = "EXTRA_CONTENT_VIDEO_URL";
    // ***NOTE***
    // Each client gets their own network ID, AdManager URLs, and ad server URLs
    // Please see your BaidDu account manager or sales engineer for the
    // appropriate values to use in your production player.
    private static final String BAIDU_PUBLISHER_ID = "please_place_your_publisher_id_here";
    private static final String BAIDU_AD_PLACEMENT_ID_OF_PREROLL = "2058634";
    // Video file to play
    private Uri videoURI = null;
    private BDVideoView videoPlayer = null;
    private RelativeLayout mAdSlotBase = null;
    private RelativeLayout mLoading = null;
    private MediaController mediaController = null;
    private double videoDuration = 0.0;
    private boolean isAdReady = false;

    // Ideally, your IXAdManager instance is reused throughout your app
    private IXAdManager mBaiduAdManager = null;
    private IXAdContext mBaiduAdContext = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preroll);

        this.findViewById(R.id.pause_resume_ad_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (isAdReady == true) {
                    IXAdProd slot = mBaiduAdContext.getSlotById(BAIDU_AD_PLACEMENT_ID_OF_PREROLL);
                    if (slot != null && slot.getSlotState() == SlotState.PLAYING) {
                        slot.pause();
                    } else {
                        slot.resume();
                    }
                }
            }
        });
        this.findViewById(R.id.resize_ad_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (isAdReady == true) {
                    IXAdProd slot = mBaiduAdContext.getSlotById(BAIDU_AD_PLACEMENT_ID_OF_PREROLL);


                    //
//                    ViewGroup.LayoutParams params = mAdSlotBase.getLayoutParams();
//                    params.width = 400;
//                    params.height = 300;
//
//                    mAdSlotBase.setLayoutParams(params);

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                }
            }
        });

        // 
        String tmpURL = getIntent().getStringExtra(EXTRA_CONTENT_VIDEO_URL);
        videoURI = Uri.parse(tmpURL);

        // Get and setup our video player
        videoPlayer = (BDVideoView) findViewById(R.id.videoView);
        mAdSlotBase = (RelativeLayout) findViewById(R.id.baiduAdHolderView);

        mLoading = (RelativeLayout) findViewById(R.id.loading);
        mLoading.setVisibility(View.GONE);
        mediaController = new MediaController(this);
        mediaController.setMediaPlayer(videoPlayer);
        mediaController.hide();
        videoPlayer.setMediaController(mediaController);

        buildVideoData();
    }

    /**
     * Obtain any needed asset metadata
     */
    private void buildVideoData() {
        // Obtain duration by preparing the video content via setVideoPath

        // Once main video content is prepared our OnPreparedListener will
        // execute

        videoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                videoDuration = mp.getDuration() / 1000;
                Log.d(TAG, "Got video duration " + videoDuration);
                // videoPlayer.setOnPreparedListener(null);
                // mLoading.setVisibility(View.GONE);
                // initBaiduAdSDK();
            }
        });

        videoPlayer.setOnInfoListener(new OnInfoListener() {

            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                mLoading.setVisibility(View.GONE);
                return false;
            }
        });

        Log.d(TAG, "Attempting to obtain video duration from content");
        videoPlayer.setOnErrorListener(this);
        initBaiduAdSDK();
        // videoPlayer.setVideoURI(videoURI);
    }

    private void initBaiduAdSDK() {

        Log.d(TAG, "Loading AdManager");

        mBaiduAdManager = XAdManager.getInstance(PrerollActivity.this.getApplicationContext());

        mBaiduAdContext = mBaiduAdManager.newAdContext();
        mBaiduAdContext.setActivity(PrerollActivity.this);
        // 设置广告视频播放的View容器
        mBaiduAdContext.setVideoDisplayBase(mAdSlotBase);
        // 设置View容器的宽
        mBaiduAdContext.setVideoDisplayBaseWidth(getResources().getDisplayMetrics().widthPixels);
        // 设置View容器的高
        mBaiduAdContext.setVideoDisplayBaseHeight((int) (250 * getResources().getDisplayMetrics().density));
        // 设置请求广告服务器的超时时间，单位为毫秒
        mBaiduAdContext.setAdServerRequestingTimeout(1000);
        // 设置加载广告视频资源的超时时间，单位为毫秒
        mBaiduAdContext.setAdCreativeLoadingTimeout(3000);
        // 设置前贴片广告的广告位id（apid），最大广告时长，最大广告个数。目前，前贴片广告Demo只支持1个广告视频，即后两个参数暂时无效
        mBaiduAdContext.newPrerollAdSlot(BAIDU_AD_PLACEMENT_ID_OF_PREROLL, 120, 4);
        //
        mBaiduAdContext.addEventListener(IXAdConstants4PDK.EVENT_ERROR, new IOAdEventListener() {
            public void run(IOAdEvent e) {
                new Handler(PrerollActivity.this.getMainLooper()).post(new Runnable() {
                    public void run() {
                        Toast.makeText(PrerollActivity.this, "no available preroll video", Toast.LENGTH_SHORT).show();
                        mLoading.setVisibility(View.VISIBLE);
                    }
                });
                playMainVideo();
            }
        });
        mBaiduAdContext.addEventListener(IXAdConstants4PDK.EVENT_REQUEST_COMPLETE, new IOAdEventListener() {
            public void run(IOAdEvent e) {
                isAdReady = true;
                handleAdManagerRequestComplete();
            }
        });
        mBaiduAdContext.addEventListener(IXAdConstants4PDK.EVENT_SLOT_STARTED, new IOAdEventListener() {
            public void run(IOAdEvent e) {
            }
        });
        mBaiduAdContext.addEventListener(IXAdConstants4PDK.EVENT_SLOT_CLICKED, new IOAdEventListener() {
            public void run(IOAdEvent e) {
            }
        });
        mBaiduAdContext.addEventListener(IXAdConstants4PDK.EVENT_SLOT_ENDED, new IOAdEventListener() {
            public void run(IOAdEvent e) {
                new Handler(PrerollActivity.this.getMainLooper()).post(new Runnable() {
                    public void run() {
                        mLoading.setVisibility(View.VISIBLE);
                    }
                });
                playMainVideo();
            }
        });
        mBaiduAdContext.submitRequest();
    }

    private void handleAdManagerRequestComplete() {
        Log.d(TAG, "Playing preroll slots");

        videoPlayer.setBaiduAdContext(mBaiduAdContext);

        // Hide video player so that advertising is visible then start playing preroll
        // Android VideoView visibility can only be set by thread that created the VideoView instance
        new Handler(this.getMainLooper()).post(new Runnable() {
            public void run() {
                videoPlayer.setVisibility(View.GONE);
                IXAdProd slot = mBaiduAdContext.getSlotById(BAIDU_AD_PLACEMENT_ID_OF_PREROLL);
                slot.start();
            }
        });
    }

    public void playMainVideo() {
        Log.d(TAG, "Starting main video");

        isAdReady = false;
        videoPlayer.setOnErrorListener(this);
        new Handler(this.getMainLooper()).post(new Runnable() {
            public void run() {
                mediaController.show(3);
                videoPlayer.setVisibility(View.VISIBLE);
                // videoPlayer.start();
                videoPlayer.setVideoURI(videoURI);
                videoPlayer.start();
            }
        });
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "MediaPlayer encountered an unexpected error. what=" + what + ", extra=" + extra);
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBaiduAdContext != null) {
            mBaiduAdContext.setActivityState(IXAdConstants4PDK.ActivityState.PAUSE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBaiduAdContext != null) {
            mBaiduAdContext.setActivityState(IXAdConstants4PDK.ActivityState.RESUME);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mBaiduAdContext != null) {
            mBaiduAdContext.setActivityState(IXAdConstants4PDK.ActivityState.START);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBaiduAdContext != null) {
            mBaiduAdContext.setActivityState(IXAdConstants4PDK.ActivityState.STOP);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mBaiduAdContext != null) {
            mBaiduAdContext.setActivityState(IXAdConstants4PDK.ActivityState.RESTART);
        }
    }
}

package com.ksc.client.ads.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.ksc.client.ads.KSCBlackBoard;
import com.ksc.client.ads.KSCMediaState;
import com.ksc.client.ads.KSCMobileAdKeyCode;
import com.ksc.client.ads.KSCViewUtils;
import com.ksc.client.ads.callback.KSCVideoPlayCallBack;

import java.io.IOException;

public class KSCMobileAdActivity extends Activity {

    private static final String TAG = KSCMobileAdActivity.class.getSimpleName();
    private RelativeLayout mRootView;
    private ImageView mCloseView;
    private ImageView mMuteView;
    private KSCCountDownView mCountDownTimeView;
    private KSCVideoView mMediaPlayer;
    private boolean mIsMute = false;
    private boolean mCanConfigChange = false;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            KSCBlackBoard.getTransformHandler().sendEmptyMessage(msg.what);
            switch (msg.what) {
                case KSCMobileAdKeyCode.KEY_VIDEO_PREPARED:
                    refreshCountDownTimeView(msg.arg1, msg.arg2);
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_PLAYING:
                    if (((mMediaPlayer.getCurrentPosition() + 1000) / (float) mMediaPlayer.getDuration()) > (1 / (float) 3)) {
                        showCloseView();
                    }
                    refreshCountDownTimeView(msg.arg1, msg.arg2);
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_PAUSE:
                    if (mMediaPlayer != null && mMediaPlayer.getCurrentState() == KSCMediaState.STARTED) {
                        mHandler.removeCallbacks(mGetVideoProgressTask);
                        mMediaPlayer.pause();
                    }
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_RESUME:
                    if (mMediaPlayer != null && mMediaPlayer.getCurrentState() == KSCMediaState.PAUSED) {
                        mHandler.post(mGetVideoProgressTask);
                        mMediaPlayer.start();
                    }
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_CLOSE:
                    mHandler.removeCallbacks(mGetVideoProgressTask);
                    mMediaPlayer.stop();
                    closeView(KSCMobileAdKeyCode.KEY_ACTIVITY_CLOSE_VIDEO);
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_COMPLETION:
                    showLandingPage();
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_MUTE:
                    muteVideoVolume();
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_ERROR:
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_CLOSE:
                    closeView(KSCMobileAdKeyCode.KEY_ACTIVITY_CLOSE_H5);
                    break;
            }
        }
    };
    private Runnable mGetVideoProgressTask = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(mGetVideoProgressTask);
            Message message = mHandler.obtainMessage();
            message.what = KSCMobileAdKeyCode.KEY_VIDEO_PLAYING;
            message.arg1 = mMediaPlayer.getDuration();
            message.arg2 = mMediaPlayer.getCurrentPosition();
            mHandler.sendMessage(message);
            mHandler.postDelayed(mGetVideoProgressTask, 100);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initView();
        initListener();

        Intent intent = getIntent();
        String type = intent.getStringExtra(KSCMobileAdKeyCode.VIDEO_TYPE);
        String path = intent.getStringExtra(KSCMobileAdKeyCode.VIDEO_PATH);

        try {
            if (type.equals(KSCMobileAdKeyCode.VIDEO_IN_CACHE)) {
                mMediaPlayer.setVideoPath(path);
            } else if (type.equals(KSCMobileAdKeyCode.VIDEO_IN_STREAM)) {
                mMediaPlayer.setVideoURI(Uri.parse(path));
            }
        } catch (IOException e) {
            Log.e(TAG, "onCreate: set MediaPlayer DataSource exception", e);
        }
    }

    private void initView() {
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mRootView = new RelativeLayout(this);
        addContentView(mRootView, lp);

        // 播放器
        mMediaPlayer = new KSCVideoView(this);
        mMediaPlayer.setBackgroundColor(Color.parseColor("#B2000000"));
        mRootView.addView(mMediaPlayer, lp);

        // 关闭按钮
        lp = new LayoutParams(100, 100);
        mCloseView = new ImageView(this);
        mCloseView.setId(KSCViewUtils.generateViewId());
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.setMargins(0, 20, 20, 0);
        mCloseView.setImageBitmap(KSCViewUtils.getBitmapFromAssets(this, KSCMobileAdKeyCode.VIDEO_VIEW_CLOSE));
        mCloseView.setBackgroundColor(Color.TRANSPARENT);
        mCloseView.setVisibility(View.GONE);
        mRootView.addView(mCloseView, lp);

        // 倒计时视图
        lp = new LayoutParams(100, 100);
        mCountDownTimeView = new KSCCountDownView(this);
        mCountDownTimeView.setId(KSCViewUtils.generateViewId());
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lp.setMargins(20, 0, 0, 20);
        mCountDownTimeView.setBackgroundColor(Color.TRANSPARENT);
        mCountDownTimeView.setOutLineWidth(2);
        mCountDownTimeView.setOutLineColor(Color.BLACK);
        mCountDownTimeView.setProgressLineWidth(8);
        mCountDownTimeView.setProgressLineColor(Color.WHITE);
        mCountDownTimeView.setInnerCircleColor(Color.BLACK);
        mCountDownTimeView.setTotalCountDownTime(15);
        mCountDownTimeView.setCurrentCountDownTime(15);
        mCountDownTimeView.setContentColor(Color.WHITE);
        mCountDownTimeView.setContentSize(50);
        mCountDownTimeView.setProgressType(KSCCountDownView.ProgressType.COUNT);
        mRootView.addView(mCountDownTimeView, lp);

        // 静音按钮
        lp = new LayoutParams(100, 100);
        mMuteView = new ImageView(this);
        mMuteView.setId(KSCViewUtils.generateViewId());
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.setMargins(0, 0, 20, 20);
        mMuteView.setBackgroundColor(Color.TRANSPARENT);
        mMuteView.setImageBitmap(KSCViewUtils.getBitmapFromAssets(this, KSCMobileAdKeyCode.VIDEO_VIEW_VOLUME_RESUME));
        mRootView.addView(mMuteView, lp);
    }

    private void initListener() {
        mCloseView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIDEO_PAUSE);
                showCloseConfirmDialog();
            }
        });
        mMuteView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIDEO_MUTE);
            }
        });
        mMediaPlayer.setVideoPlayCallBack(new KSCVideoPlayCallBack() {
            @Override
            public void onPrepared() {
                Message message = mHandler.obtainMessage();
                message.what = KSCMobileAdKeyCode.KEY_VIDEO_PREPARED;
                message.arg1 = mMediaPlayer.getDuration();
                message.arg2 = mMediaPlayer.getCurrentPosition();
                mHandler.sendMessage(message);
            }

            @Override
            public void onStart() {
                Log.i(TAG, "onStart: ");
                mHandler.post(mGetVideoProgressTask);
            }

            @Override
            public void onFullScreen() {
                Log.i(TAG, "onFullScreen: ");
            }

            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.i(TAG, "onCompletion: ");
                mHandler.removeCallbacks(mGetVideoProgressTask);
                mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIDEO_COMPLETION);
            }

            @Override
            public void onError(MediaPlayer mediaPlayer, int what, int extra) {
                Log.i(TAG, "onError: what=" + what + ", extra=" + extra);
                mHandler.removeCallbacks(mGetVideoProgressTask);
                mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIDEO_ERROR);
            }

        });
    }

    private void refreshCountDownTimeView(int duration, int currentPosition) {
        mCountDownTimeView.setTotalCountDownTime(duration);
        mCountDownTimeView.setCurrentCountDownTime(currentPosition);
    }

    private void showCloseView() {
        mCloseView.setVisibility(View.VISIBLE);
    }

    private void closeView(String value) {
        Intent intent = new Intent();
        intent.putExtra(KSCMobileAdKeyCode.KEY_ACTIVITY_CLOSE, value);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showCloseConfirmDialog() {
        mCloseView.setEnabled(false);
        mMuteView.setEnabled(false);
        final KSCCloseVideoPromptView alertDialogView = new KSCCloseVideoPromptView(this);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        lp.setMargins(300, 200, 300, 200);
        alertDialogView.setPromptMsgText("注意: 视频播放不完全，将得不到奖励\n您确定要提前关闭吗？");
        alertDialogView.setCloseButtonText("关闭");
        alertDialogView.setContinueButtonText("继续观看");
        alertDialogView.setCloseButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIDEO_CLOSE);
            }
        });
        alertDialogView.setContinueButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mRootView.removeView(alertDialogView);
                mCloseView.setEnabled(true);
                mMuteView.setEnabled(true);
                mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIDEO_RESUME);
            }
        });
        mRootView.addView(alertDialogView, lp);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void showLandingPage() {
        mRootView.removeView(mCountDownTimeView);
        mRootView.removeView(mMuteView);
        mRootView.removeView(mMediaPlayer);
        mCloseView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIEW_CLOSE);
            }
        });
        WebView mLandingPage = new WebView(this);
        mLandingPage.setWebViewClient(new WebViewClient());
        mLandingPage.setWebChromeClient(new WebChromeClient());
        WebSettings settings = mLandingPage.getSettings();
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setAppCacheEnabled(true);
        settings.setJavaScriptEnabled(true);
        mLandingPage.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
                Uri uri = Uri.parse(s);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        mLandingPage.loadUrl("http://shouji.baidu.com/game/9794980.html");
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.BELOW, mCloseView.getId());
        lp.setMargins(0, 20, 0, 0);
        mRootView.addView(mLandingPage, lp);
    }

    private void muteVideoVolume() {
        if (mIsMute) {
            mIsMute = false;
            mMuteView.setImageBitmap(KSCViewUtils.getBitmapFromAssets(this, KSCMobileAdKeyCode.VIDEO_VIEW_VOLUME_RESUME));
            mMediaPlayer.resumeVolume();
        } else {
            mIsMute = true;
            mMuteView.setImageBitmap(KSCViewUtils.getBitmapFromAssets(this, KSCMobileAdKeyCode.VIDEO_VIEW_MUTE));
            mMediaPlayer.closeVolume();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mMediaPlayer.setFullScreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIDEO_RESUME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIDEO_PAUSE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

}

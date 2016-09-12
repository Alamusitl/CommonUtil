package com.ksc.client.ads.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.ksc.client.ads.KSCBlackBoard;
import com.ksc.client.ads.KSCMediaState;
import com.ksc.client.ads.callback.KSCVideoPlayCallBack;
import com.ksc.client.ads.config.KSCMobileAdKeyCode;
import com.ksc.client.util.KSCNetUtils;
import com.ksc.client.util.KSCViewUtils;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class KSCMobileAdActivity extends Activity {

    private static final String TAG = KSCMobileAdActivity.class.getSimpleName();
    private RelativeLayout mRootView;
    private ImageView mCloseView;
    private ImageView mMuteView;
    private KSCLandingPageView mLandingPageView;
    private KSCCountDownView mCountDownTimeView;
    private KSCVideoView mMediaPlayer;
    private boolean mIsMute = false;
    private boolean mPopCloseView = false;
    private Timer mTimer;
    private String mH5Path;
    private int mControlViewSize;
    private int mDialogViewWidth;
    private int mDialogViewHeight;
    private float mDensity;
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
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KSCMobileAdKeyCode.KEY_VIDEO_PREPARED:
                    showControlView();
                    refreshCountDownTimeView(msg.arg1, msg.arg2);
                    openTimer(msg.arg1);
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_START:
                    dispatchMsg(msg);
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_PLAYING:
                    if (((msg.arg2 + 1000) / (float) msg.arg1) > (1 / (float) 3)) {
                        showCloseVideoView();
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
                        if (!mPopCloseView) {
                            mMediaPlayer.start();
                        }
                    }
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_CLOSE:
                    dispatchMsg(msg);
                    mHandler.removeCallbacks(mGetVideoProgressTask);
                    mMediaPlayer.stop();
                    closeActivity();
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_COMPLETION:
                    dispatchMsg(msg);
                    showLandingPage();
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_MUTE:
                    muteVideoVolume();
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_ERROR:
                    dispatchMsg(msg);
                    closeActivity();
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_SHOW_VIDEO_CLOSE:
                    showCloseVideoView();
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_SHOW_CLOSE_CONFIRM:
                    showCloseVideoConfirmDialog();
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_H5_SHOW:
                    dispatchMsg(msg);
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_H5_CLOSE:
                    dispatchMsg(msg);
                    closeActivity();
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_H5_CLICK:
                    dispatchMsg(msg);
                    disposeDownload();
                    break;
                case KSCMobileAdKeyCode.KEY_DOWNLOAD_START:
                    dispatchMsg(msg);
                    closeActivity();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
        mControlViewSize = (int) (33 * dm.density);
        mDialogViewWidth = (int) (320 * dm.density);
        mDialogViewHeight = (int) (165 * dm.density);

        Intent intent = getIntent();
        String type = intent.getStringExtra(KSCMobileAdKeyCode.VIDEO_TYPE);
        String path = intent.getStringExtra(KSCMobileAdKeyCode.VIDEO_PATH);
        mH5Path = intent.getStringExtra(KSCMobileAdKeyCode.VIDEO_H5_PATH);
        if (mH5Path == null) {
            mH5Path = new String(new byte[0]);
        }
        Log.d(TAG, "onCreate: type=" + type + ", path=" + path);

        initView();
        initListener();

        try {
            if (path != null && !path.equals("")) {
                mMediaPlayer.setVideoSource(path);
            }
            if (type.equals(KSCMobileAdKeyCode.VIDEO_IN_STREAM)) {
                mTimer = new Timer();
            }
        } catch (IOException e) {
            Log.e(TAG, "onCreate: set MediaPlayer DataSource exception", e);
            Message message = mHandler.obtainMessage();
            message.what = KSCMobileAdKeyCode.KEY_VIDEO_ERROR;
            message.obj = "视频文件错误";
            mHandler.sendMessage(message);
        }
    }

    private void initView() {
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mRootView = new RelativeLayout(this);
        addContentView(mRootView, lp);

        createLandingPage();

        // 播放器
        mMediaPlayer = new KSCVideoView(this);
        mMediaPlayer.setBackgroundColor(Color.BLACK);
        mRootView.addView(mMediaPlayer, lp);
    }

    private void initListener() {
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
                if (mMediaPlayer != null && mMediaPlayer.getCurrentPosition() == 0) {
                    mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIDEO_START);
                }
            }

            @Override
            public void onFullScreen() {
                Log.i(TAG, "onFullScreen: ");
            }

            @Override
            public void onCompletion() {
                Log.i(TAG, "onCompletion: ");
                mHandler.removeCallbacks(mGetVideoProgressTask);
                Message message = mHandler.obtainMessage();
                message.what = KSCMobileAdKeyCode.KEY_VIDEO_COMPLETION;
                message.arg1 = mMediaPlayer.getDuration();
                mHandler.sendMessage(message);
            }

            @Override
            public void onError(int what, int extra) {
                Log.i(TAG, "onError: what=" + what + ", extra=" + extra);
                mHandler.removeCallbacks(mGetVideoProgressTask);
                Message message = mHandler.obtainMessage();
                message.what = KSCMobileAdKeyCode.KEY_VIDEO_ERROR;
                message.obj = what + ":" + extra;
                mHandler.sendMessage(message);
            }

            @Override
            public void onMediaPlayerError(String errorMsg) {
                Log.i(TAG, "onMediaPlayerError: errorMsg=" + errorMsg);
                mHandler.removeCallbacks(mGetVideoProgressTask);
                Message message = mHandler.obtainMessage();
                message.what = KSCMobileAdKeyCode.KEY_VIDEO_ERROR;
                message.obj = errorMsg;
                mHandler.sendMessage(message);
            }

            @Override
            public void onSeekCompletion() {
            }
        });
    }

    private void showControlView() {
        int outLineWidth = (int) (2 / (float) 3 * mDensity);
        int processWidth = (int) (8 / 3 * mDensity);
        int textSize = (int) (50 / 3 * mDensity);
        // 关闭按钮
        LayoutParams lp = new LayoutParams(mControlViewSize, mControlViewSize);
        mCloseView = new ImageView(this);
        mCloseView.setId(KSCViewUtils.generateViewId());
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.setMargins(0, 20, 20, 0);
        mCloseView.setImageBitmap(KSCViewUtils.getBitmapFromAssets(this, KSCMobileAdKeyCode.IMG_VIDEO_VIEW_CLOSE));
        mCloseView.setBackgroundColor(Color.TRANSPARENT);
        mCloseView.setVisibility(View.GONE);
        mRootView.addView(mCloseView, lp);

        // 倒计时视图
        lp = new LayoutParams(mControlViewSize, mControlViewSize);
        mCountDownTimeView = new KSCCountDownView(this);
        mCountDownTimeView.setId(KSCViewUtils.generateViewId());
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lp.setMargins(20, 0, 0, 20);
        mCountDownTimeView.setBackgroundColor(Color.TRANSPARENT);
        mCountDownTimeView.setOutLineWidth(outLineWidth);
        mCountDownTimeView.setOutLineColor(Color.BLACK);
        mCountDownTimeView.setProgressLineWidth(processWidth);
        mCountDownTimeView.setProgressLineColor(Color.WHITE);
        mCountDownTimeView.setInnerCircleColor(Color.BLACK);
        mCountDownTimeView.setTotalCountDownTime(15);
        mCountDownTimeView.setCurrentCountDownTime(15);
        mCountDownTimeView.setContentColor(Color.WHITE);
        mCountDownTimeView.setContentSize(textSize);
        mCountDownTimeView.setProgressType(KSCCountDownView.ProgressType.COUNT);
        mRootView.addView(mCountDownTimeView, lp);

        // 静音按钮
        lp = new LayoutParams(mControlViewSize, mControlViewSize);
        mMuteView = new ImageView(this);
        mMuteView.setId(KSCViewUtils.generateViewId());
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.setMargins(0, 0, 20, 20);
        mMuteView.setBackgroundColor(Color.TRANSPARENT);
        mMuteView.setImageBitmap(KSCViewUtils.getBitmapFromAssets(this, KSCMobileAdKeyCode.IMG_VIDEO_VIEW_VOLUME_RESUME));
        mRootView.addView(mMuteView, lp);

        // 设置监听
        mCloseView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIDEO_PAUSE);
                mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIEW_SHOW_CLOSE_CONFIRM);
            }
        });
        mMuteView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIDEO_MUTE);
            }
        });
    }

    private void refreshCountDownTimeView(int duration, int currentPosition) {
        mCountDownTimeView.setTotalCountDownTime(duration);
        mCountDownTimeView.setCurrentCountDownTime(currentPosition);
    }

    private void showCloseVideoView() {
        if (!mCloseView.isShown()) {
            mCloseView.setVisibility(View.VISIBLE);
        }
    }

    private void closeActivity() {
        mRootView.removeAllViews();
        if (mTimer != null) {
            mTimer.cancel();
        }
        finish();
    }

    private void showCloseVideoConfirmDialog() {
        mCloseView.setEnabled(false);
        mMuteView.setEnabled(false);
        mPopCloseView = true;

        final KSCCloseVideoPromptView alertDialogView = new KSCCloseVideoPromptView(this);

        LayoutParams lp = new LayoutParams(mDialogViewWidth, mDialogViewHeight);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        alertDialogView.setText("现在关闭将无法得到奖励，确定关闭？");
        alertDialogView.setCloseButtonText("关闭");
        alertDialogView.setContinueButtonText("继续观看");
        alertDialogView.setCloseButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopCloseView = false;
                Message message = mHandler.obtainMessage();
                message.what = KSCMobileAdKeyCode.KEY_VIDEO_CLOSE;
                message.arg1 = mCountDownTimeView.getCurrentCountDownTime();
                mHandler.sendMessage(message);
            }
        });
        alertDialogView.setContinueButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopCloseView = false;
                mRootView.removeView(alertDialogView);
                mCloseView.setEnabled(true);
                mMuteView.setEnabled(true);
                mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIDEO_RESUME);
            }
        });
        mRootView.addView(alertDialogView, lp);
    }

    private void createLandingPage() {
        // 落地页
        mLandingPageView = new KSCLandingPageView(this);
        mLandingPageView.setSize(mControlViewSize);
        mLandingPageView.setLandingViewData(mH5Path);
        mLandingPageView.setCloseViewClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIEW_H5_CLOSE);
            }
        });
        mLandingPageView.setLandingViewDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String s1, String s2, String s3, long l) {
                Message message = mHandler.obtainMessage();
                message.what = KSCMobileAdKeyCode.KEY_VIEW_H5_CLICK;
                mHandler.sendMessage(message);
            }
        });
        mLandingPageView.setVisibility(View.GONE);
        mRootView.addView(mLandingPageView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    private void showLandingPage() {
        // 移除所有的View
        mRootView.removeView(mCloseView);
        mRootView.removeView(mMuteView);
        mRootView.removeView(mCountDownTimeView);
        mRootView.removeView(mMediaPlayer);
        mLandingPageView.setVisibility(View.VISIBLE);
        mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIEW_H5_SHOW);
    }

    private void showNetPromptView(final int i, String msg) {
        KSCNetPromptView netPromptView = new KSCNetPromptView(this);
        netPromptView.setText(msg, "确定");
        netPromptView.setSize(mDialogViewWidth, mDialogViewHeight);
        netPromptView.setConfirmViewClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (i == 2) {
                    mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_DOWNLOAD_START);
                } else {
                    mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_DOWNLOAD_FAIL);
                }
            }
        });
        LayoutParams lp = new LayoutParams(mDialogViewWidth, mDialogViewHeight);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mRootView.addView(netPromptView, lp);
    }

    private void muteVideoVolume() {
        if (mIsMute) {
            mIsMute = false;
            mMuteView.setImageBitmap(KSCViewUtils.getBitmapFromAssets(this, KSCMobileAdKeyCode.IMG_VIDEO_VIEW_VOLUME_RESUME));
            mMediaPlayer.resumeVolume();
        } else {
            mIsMute = true;
            mMuteView.setImageBitmap(KSCViewUtils.getBitmapFromAssets(this, KSCMobileAdKeyCode.IMG_VIDEO_VIEW_MUTE));
            mMediaPlayer.closeVolume();
        }
    }

    private void disposeDownload() {
        if (!KSCNetUtils.isNetworkAvailable(KSCMobileAdActivity.this)) {
            showNetPromptView(1, "当前网络环境不佳，请稍后再试");
            return;
        }
        int netType = KSCNetUtils.getNetType(KSCMobileAdActivity.this);
        if (netType != KSCNetUtils.NETWORK_TYPE_WIFI) {
            showNetPromptView(2, "当前处于非wifi环境，确认下载？");
        } else {
            mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_DOWNLOAD_START);
        }
    }

    private void openTimer(int duration) {
        if (mTimer != null) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIEW_SHOW_VIDEO_CLOSE);
                }
            };
            mTimer.schedule(task, duration / 3);
        }
    }

    private void dispatchMsg(Message msg) {
        if (KSCBlackBoard.getTransformHandler() != null) {
            Message message = KSCBlackBoard.getTransformHandler().obtainMessage();
            message.what = msg.what;
            message.arg1 = msg.arg1;
            message.arg2 = msg.arg2;
            message.obj = msg.obj;
            KSCBlackBoard.getTransformHandler().sendMessage(message);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mMediaPlayer.getCurrentState() != KSCMediaState.END) {
            mMediaPlayer.setFullScreen();
            if (mPopCloseView) {
                mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIDEO_PAUSE);
            }
        }
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
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

}

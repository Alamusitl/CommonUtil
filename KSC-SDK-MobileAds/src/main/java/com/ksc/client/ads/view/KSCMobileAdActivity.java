package com.ksc.client.ads.view;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
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
import com.ksc.client.ads.config.KSCColor;
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
    private byte[] mH5Path;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (KSCBlackBoard.getTransformHandler() != null) {
                Message message = KSCBlackBoard.getTransformHandler().obtainMessage();
                message.what = msg.what;
                message.arg1 = msg.arg1;
                message.arg2 = msg.arg2;
                message.obj = msg.obj;
                KSCBlackBoard.getTransformHandler().sendMessage(message);
            }
            switch (msg.what) {
                case KSCMobileAdKeyCode.KEY_VIDEO_PREPARED:
                    refreshCountDownTimeView(msg.arg1, msg.arg2);
                    openTimer(msg.arg1);
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_PLAYING:
                    if (((mMediaPlayer.getCurrentPosition() + 1000) / (float) mMediaPlayer.getDuration()) > (1 / (float) 3)) {
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
                        mHandler.post(mGetVideoProgressTask);
                        mMediaPlayer.start();
                    }
                    if (mPopCloseView) {
                        mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIDEO_PAUSE);
                    }
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_CLOSE:
                    mHandler.removeCallbacks(mGetVideoProgressTask);
                    mMediaPlayer.stop();
                    closeActivity();
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_COMPLETION:
                    showLandingPage();
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_MUTE:
                    muteVideoVolume();
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_ERROR:
                    closeActivity();
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_SHOW_VIDEO_CLOSE:
                    showCloseVideoView();
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_SHOW_CLOSE_CONFIRM:
                    showCloseVideoConfirmDialog();
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_H5_CLOSE:
                    closeActivity();
                    break;
                case KSCMobileAdKeyCode.KEY_VIEW_H5_CLICK:
                    disposeDownload();
                    break;
                case KSCMobileAdKeyCode.KEY_DOWNLOAD_START:
                    closeActivity();
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        String type = intent.getStringExtra(KSCMobileAdKeyCode.VIDEO_TYPE);
        String path = intent.getStringExtra(KSCMobileAdKeyCode.VIDEO_PATH);
        mH5Path = intent.getByteArrayExtra(KSCMobileAdKeyCode.VIDEO_H5_PATH);
        Log.d(TAG, "onCreate: type=" + type + ", path=" + path);

        initView();
        initListener();

        try {
            if (type.equals(KSCMobileAdKeyCode.VIDEO_IN_CACHE)) {
                mMediaPlayer.setVideoPath(path);
            } else if (type.equals(KSCMobileAdKeyCode.VIDEO_IN_STREAM)) {
                mMediaPlayer.setVideoURI(Uri.parse(path));
                mTimer = new Timer();
            }
        } catch (IOException e) {
            Log.e(TAG, "onCreate: set MediaPlayer DataSource exception", e);
            Message message = mHandler.obtainMessage();
            message.what = KSCMobileAdKeyCode.KEY_VIDEO_ERROR;
            message.obj = e.getMessage();
            mHandler.sendMessage(message);
        }
    }

    private void initView() {
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mRootView = new RelativeLayout(this);
        addContentView(mRootView, lp);

        createLandingPage();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int size = 33 * (int) dm.density;
        int outLineWidth = (int) (2 / (float) 3 * dm.density);
        int processWidth = 8 / 3 * (int) dm.density;
        int textSize = (int) (50 / 3 * dm.density);

        // 播放器
        mMediaPlayer = new KSCVideoView(this);
        mMediaPlayer.setBackgroundColor(KSCColor.TRANSPARENT_BLACK_7);
        mRootView.addView(mMediaPlayer, lp);

        // 关闭按钮
        lp = new LayoutParams(size, size);
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
        lp = new LayoutParams(size, size);
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
        lp = new LayoutParams(size, size);
        mMuteView = new ImageView(this);
        mMuteView.setId(KSCViewUtils.generateViewId());
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.setMargins(0, 0, 20, 20);
        mMuteView.setBackgroundColor(Color.TRANSPARENT);
        mMuteView.setImageBitmap(KSCViewUtils.getBitmapFromAssets(this, KSCMobileAdKeyCode.IMG_VIDEO_VIEW_VOLUME_RESUME));
        mRootView.addView(mMuteView, lp);
    }

    private void initListener() {
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
                mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIDEO_START);
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
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        final KSCCloseVideoPromptView alertDialogView = new KSCCloseVideoPromptView(this);
        int width = 320 * (int) dm.density;
        int height = 165 * (int) dm.density;

        LayoutParams lp = new LayoutParams(width, height);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        alertDialogView.setText("注意: 视频播放不完全，将得不到奖励！您确定要提前关闭吗？");
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
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int size = 33 * (int) dm.density;
        mLandingPageView = new KSCLandingPageView(this);
        mLandingPageView.setSize(size);
        mLandingPageView.setLandingViewData(new String(mH5Path));
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
    }

    private void showNetPromptView(String msg) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = 320 * (int) dm.density;
        int height = 165 * (int) dm.density;
        KSCNetPromptView netPromptView = new KSCNetPromptView(this);
        netPromptView.setText(msg, "确定");
        netPromptView.setSize(width, height);
        netPromptView.setConfirmViewClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_DOWNLOAD_START);
            }
        });
        LayoutParams lp = new LayoutParams(width, height);
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
            showNetPromptView("当前网络不可用，请检查!");
            return;
        }
        int netType = KSCNetUtils.getNetType(KSCMobileAdActivity.this);
        if (netType != KSCNetUtils.NETWORK_TYPE_WIFI) {
            showNetPromptView("当前处于非WIFI环境下，您确定要下载吗？");
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

}

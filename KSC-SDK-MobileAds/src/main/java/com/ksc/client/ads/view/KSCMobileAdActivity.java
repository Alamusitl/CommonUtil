package com.ksc.client.ads.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.ksc.client.ads.KSCMediaState;
import com.ksc.client.ads.KSCMobileAdKeyCode;
import com.ksc.client.ads.KSCViewUtils;
import com.ksc.client.ads.callback.KSCVideoPlayCallBack;

import java.io.IOException;

public class KSCMobileAdActivity extends Activity {

    private static final String TAG = KSCMobileAdActivity.class.getSimpleName();
    private ImageView mCloseView;
    private ImageView mMuteView;
    private KSCCountDownView mCountDownTimeView;
    private KSCVideoView mMediaPlayer;
    private WebView mLandingPage;
    private boolean mIsMute = false;
    private boolean mCanConfigChange = false;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
//            KSCBlackBoard.getTransformHandler().sendEmptyMessage(msg.what);
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
                    mMediaPlayer.pause();
                    break;
                case KSCMobileAdKeyCode.KEY_VIDEO_RESUME:
                    mMediaPlayer.start();
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
        RelativeLayout mRootView = new RelativeLayout(this);
        addContentView(mRootView, lp);

        // 播放器
        mMediaPlayer = new KSCVideoView(this);
        mMediaPlayer.setBackgroundColor(Color.WHITE);
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
        mMuteView.setImageBitmap(KSCViewUtils.getBitmapFromAssets(this, KSCMobileAdKeyCode.VIDEO_VIEW_MUTE));
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
        mCountDownTimeView.setCurrentCountDownTime(duration - currentPosition);
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
        final AlertDialog dialog = new AlertDialog.Builder(this).setMessage("现在关闭将不会获得奖励!").create();
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.cancel();
                mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIDEO_CLOSE);
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "继续", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.cancel();
                mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIDEO_RESUME);
            }
        });
        dialog.show();
    }

    private void showLandingPage() {
        mHandler.sendEmptyMessage(KSCMobileAdKeyCode.KEY_VIDEO_CLOSE);
//        mRootView.removeView(mCountDownTimeView);
//        mRootView.removeAllViews();
//        mLandingPage = new WebView(this);
//        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//        mRootView.addView(mLandingPage, lp);
    }

    private void muteVideoVolume() {
        if (mIsMute) {
            mIsMute = false;
            mMediaPlayer.resumeVolume();
        } else {
            mIsMute = true;
            mMediaPlayer.closeVolume();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMediaPlayer.getCurrentState() == KSCMediaState.PAUSED) {
            mMediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer.getCurrentState() == KSCMediaState.STARTED) {
            mMediaPlayer.pause();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

}

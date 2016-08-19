package com.baidu.mobads.demo.main.feeds;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.baidu.mobads.demo.main.R;

public class FSVideoLayout extends FSVideoView implements View.OnClickListener, SeekBar.OnSeekBarChangeListener,
        MediaPlayer.OnPreparedListener, View.OnTouchListener {

    // Counter
    protected static final Handler TIME_THREAD = new Handler();
    private static final String TAG = "FSVideoLayout";
    // Control views
    protected View videoControlsView;
    protected SeekBar seekBar;
    protected ImageButton imgplay;
    protected ImageButton imgfullscreen;
    protected TextView textTotal;
    protected TextView textElapsed;
    protected OnTouchListener touchListener;
    protected Runnable updateTimeRunnable = new Runnable() {
        public void run() {
            updateCounter();

            TIME_THREAD.postDelayed(this, 200);
        }
    };
    RelativeLayout.LayoutParams mParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT);
    private int controlShowTime = 4000;
    private TipViewClickedListener mListener = null;
    private ImageView mTipView = null;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 10) {
                // showOrHideController();
                hideControls();
            }
            return false;
        }
    });

    public FSVideoLayout(Context context) {
        super(context);
    }

    public FSVideoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FSVideoLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        Log.d(TAG, "init");

        super.init();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.videoControlsView = inflater.inflate(R.layout.view_videocontrols, null);
        videoControlsView.setId(1);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(ALIGN_PARENT_BOTTOM);
        addView(videoControlsView, params);

        this.seekBar = (SeekBar) this.videoControlsView.findViewById(R.id.vcv_seekbar);
        this.imgfullscreen = (ImageButton) this.videoControlsView.findViewById(R.id.vcv_img_fullscreen);
        this.imgplay = (ImageButton) this.videoControlsView.findViewById(R.id.vcv_img_play);
        this.textTotal = (TextView) this.videoControlsView.findViewById(R.id.vcv_txt_total);
        this.textElapsed = (TextView) this.videoControlsView.findViewById(R.id.vcv_txt_elapsed);

        // We need to add it to show/hide the controls
        super.setOnTouchListener(this);

        this.imgplay.setOnClickListener(this);
        this.imgfullscreen.setOnClickListener(this);
        this.seekBar.setOnSeekBarChangeListener(this);

        // Start controls invisible. Make it visible when it is prepared
        this.videoControlsView.setVisibility(View.INVISIBLE);
    }

    protected void startCounter() {
        Log.d(TAG, "startCounter");

        TIME_THREAD.postDelayed(updateTimeRunnable, 200);
    }

    protected void stopCounter() {
        Log.d(TAG, "stopCounter");

        TIME_THREAD.removeCallbacks(updateTimeRunnable);
    }

    protected void updateCounter() {
        int elapsed = getCurrentPosition();
        // getCurrentPosition is a little bit buggy :(
        if (elapsed > 0 && elapsed < getDuration()) {
            seekBar.setProgress(elapsed);

            elapsed = Math.round(elapsed / 1000.f);
            long s = elapsed % 60;
            long m = (elapsed / 60) % 60;
            long h = (elapsed / (60 * 60)) % 24;

            if (h > 0) {
                textElapsed.setText(String.format("%d:%02d:%02d", h, m, s));
            } else {
                textElapsed.setText(String.format("%02d:%02d", m, s));
            }
        }
    }

    @Override
    public void setOnTouchListener(View.OnTouchListener l) {
        touchListener = l;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion");

        super.onCompletion(mp);
        stopCounter();
        updateCounter();
        updateControls();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        boolean result = super.onError(mp, what, extra);
        stopCounter();
        updateControls();
        return result;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (getCurrentState() == State.END) {
            Log.d(TAG, "onDetachedFromWindow END");
            stopCounter();
        }
    }

    @Override
    protected void tryToPrepare() {
        Log.d(TAG, "tryToPrepare");
        super.tryToPrepare();

        if (getCurrentState() == State.PREPARED || getCurrentState() == State.STARTED) {
            int total = getDuration();
            if (total > 0) {
                seekBar.setMax(total);
                seekBar.setProgress(0);

                total = total / 1000;
                long s = total % 60;
                long m = (total / 60) % 60;
                long h = (total / (60 * 60)) % 24;
                if (h > 0) {
                    textElapsed.setText("00:00:00");
                    textTotal.setText(String.format("%d:%02d:%02d", h, m, s));
                } else {
                    textElapsed.setText("00:00");
                    textTotal.setText(String.format("%02d:%02d", m, s));
                }
            }

            videoControlsView.setVisibility(View.VISIBLE);
            mHandler.removeMessages(10);

            mHandler.sendEmptyMessageDelayed(10, controlShowTime);
        }
    }

    public void setTipViewClickedListener(TipViewClickedListener listener) {
        mListener = listener;
    }

    public void configTipView(boolean isDownloadApp) {
        mParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mParams.addRule(RelativeLayout.ABOVE, videoControlsView.getId());
        mParams.setMargins(0, 0, 10, 5);
        mTipView = new ImageView(context);
        if (isDownloadApp) {
            mTipView.setImageDrawable(getResources().getDrawable(R.drawable.dl));
        } else {
            mTipView.setImageDrawable(getResources().getDrawable(R.drawable.lp));
        }
        mTipView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onTipViewClicked(v);
                }
            }
        });
        addView(mTipView, mParams);
    }

    @Override
    public void start() throws IllegalStateException {
        Log.d(TAG, "start");

        if (!isPlaying()) {
            super.start();
            startCounter();
            updateControls();
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        Log.d(TAG, "pause");

        if (isPlaying()) {
            stopCounter();
            super.pause();
            updateControls();
        }
    }

    @Override
    public void reset() {
        Log.d(TAG, "reset");

        super.reset();
        stopCounter();
        updateControls();
    }

    @Override
    public void stop() throws IllegalStateException {
        Log.d(TAG, "stop");

        super.stop();
        stopCounter();
        updateControls();
    }

    protected void updateControls() {
        Drawable icon;
        if (getCurrentState() == State.STARTED) {
            icon = context.getResources().getDrawable(R.drawable.biz_video_pause);
        } else {
            icon = context.getResources().getDrawable(R.drawable.biz_video_play);
        }
        imgplay.setBackgroundDrawable(icon);
    }

    public void hideControls() {
        Log.d(TAG, "hideControls");
        if (videoControlsView != null) {
            videoControlsView.setVisibility(View.INVISIBLE);
        }
    }

    public void showControls() {
        Log.d(TAG, "showControls");
        if (videoControlsView != null) {
            videoControlsView.setVisibility(View.VISIBLE);
        }

        mHandler.removeMessages(10);
        mHandler.sendEmptyMessageDelayed(10, controlShowTime);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (videoControlsView != null) {
                if (videoControlsView.getVisibility() == View.VISIBLE) {
                    mHandler.removeMessages(10);
                    hideControls();
                } else {
                    showControls();
                }
            }
        }

        if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            return true;
        }

        if (touchListener != null) {
            return touchListener.onTouch(FSVideoLayout.this, event);
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.vcv_img_play) {
            if (isPlaying()) {
                pause();
            } else {
                start();
            }
        } else {
            toggleFullScreen();
        }
    }

    /**
     * SeekBar Listener
     */

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d(TAG, "onProgressChanged");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        stopCounter();
        Log.d(TAG, "onStartTrackingTouch");

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        seekTo(progress);
        Log.d(TAG, "onStopTrackingTouch");

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed == true && mTipView != null) {
            if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                mParams.width = getWidth() / 8;
                mParams.height = getHeight() / 5;
                mParams.setMargins(0, 0, 10, -50);
            } else {
                mParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                mParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                mParams.setMargins(0, 0, 10, 5);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void toggleFullScreen() {
        if (isPlaying()) {
            if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                imgfullscreen.setBackground(getResources().getDrawable(R.drawable.ic_media_fullscreen_stretch));
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                mParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                mParams.setMargins(0, 0, 10, 5);
            } else {
                mVideoPlayCallback.onFullScreen(getCurrentPosition());
                imgfullscreen.setBackground(getResources().getDrawable(R.drawable.ic_media_fullscreen_shrink));
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            }
            pause();
            fullscreen();
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    start();
                }
            });
        } else {
            if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                imgfullscreen.setBackground(getResources().getDrawable(R.drawable.ic_media_fullscreen_stretch));
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                mParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                mParams.setMargins(0, 0, 10, 5);
            } else {
                mVideoPlayCallback.onFullScreen(getCurrentPosition());
                imgfullscreen.setBackground(getResources().getDrawable(R.drawable.ic_media_fullscreen_shrink));
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            }
            fullscreen();
        }

    }

    public interface TipViewClickedListener {
        void onTipViewClicked(View v);
    }
}

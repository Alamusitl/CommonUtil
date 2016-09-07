package com.ksc.client.ads.view;

import android.app.Service;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.ksc.client.ads.KSCMediaState;
import com.ksc.client.ads.callback.KSCVideoPlayCallBack;

import java.io.IOException;

/**
 * Created by Alamusi on 2016/8/18.
 */
public class KSCVideoView extends RelativeLayout implements SurfaceHolder.Callback, OnPreparedListener, OnErrorListener, OnSeekCompleteListener, OnCompletionListener, OnInfoListener, OnBufferingUpdateListener {

    private static final String TAG = KSCVideoView.class.getSimpleName();
    private Context mContext;
    private MediaPlayer mMediaPlayer;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private View mLoadingView;
    private boolean mSurfaceIsReady;
    private boolean mVideoIsReady;
    private boolean mAutoPlay;
    private boolean mIsCompleted;
    private int mInitialVideoWidth;
    private int mInitialVideoHeight;
    private int mBufferProgress;
    private KSCMediaState mCurrentState;
    private KSCMediaState mLastState;
    private KSCVideoPlayCallBack mVideoPlayCallBack;

    public KSCVideoView(Context context) {
        super(context);
        mContext = context;
        initView(context);
    }

    public KSCVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView(context);
    }

    public KSCVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView(context);
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow called");
        super.onDetachedFromWindow();
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnPreparedListener(null);
            mMediaPlayer.setOnErrorListener(null);
            mMediaPlayer.setOnSeekCompleteListener(null);
            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.setOnInfoListener(null);
            mMediaPlayer.setOnBufferingUpdateListener(null);
            if (isPlaying()) {
                stop();
            }
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mVideoIsReady = false;
        mSurfaceIsReady = false;
        mCurrentState = KSCMediaState.END;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceCreated Called");
        mMediaPlayer.setDisplay(mHolder);
        mSurfaceIsReady = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d(TAG, "surfaceChanged Called");
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                resize();
            }
        });
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceDestroyed Called");
        mSurfaceIsReady = false;
        if (isPlaying()) {
            pause();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onCompletion Called, isLooping=" + mediaPlayer.isLooping());
        mIsCompleted = true;
        if (!isLooping()) {
            mCurrentState = KSCMediaState.PLAYBACKCOMPLETED;
        } else {
            start();
        }
        if (mVideoPlayCallBack != null) {
            mVideoPlayCallBack.onCompletion();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.d(TAG, "onError Called, what=" + i + ", extra=" + i1);
        stopLoading();
        mCurrentState = KSCMediaState.ERROR;
        if (mVideoPlayCallBack != null) {
            mVideoPlayCallBack.onError(i, i1);
        }
        return false;
    }

    @Override
    public synchronized void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPrepared Called");
        mVideoIsReady = true;
        tryToPrepare();
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onSeekComplete Called");
        stopLoading();
        if (mLastState != null) {
            switch (mLastState) {
                case STARTED:
                    start();
                    break;
                case PAUSED:
                    pause();
                    break;
                case PREPARED:
                    mCurrentState = KSCMediaState.PREPARED;
                    break;
                case PLAYBACKCOMPLETED:
                    mCurrentState = KSCMediaState.PLAYBACKCOMPLETED;
                    break;
            }
        }

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        mBufferProgress = i;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    /**
     * Initializes the UI
     *
     * @param context Context
     */
    protected void initView(Context context) {
        Log.d(TAG, "initView called");
        if (isInEditMode()) {
            return;
        }
        mAutoPlay = true;
        mCurrentState = KSCMediaState.IDLE;
        setBackgroundColor(Color.BLACK);

        // Initialize mediaPlayer
        mMediaPlayer = new MediaPlayer();
        reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // Initialize the surfaceView
        mSurfaceView = new SurfaceView(context);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mSurfaceView.setLayoutParams(layoutParams);
        addView(mSurfaceView);

        // Initialize the surfaceHolder
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
            // noinspection deprecation
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        // Initialize the loadingView
        mLoadingView = new ProgressBar(context);
        layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLoadingView.setLayoutParams(layoutParams);
        addView(mLoadingView);
    }

    /**
     * try to call state prepared, Only SurfaceView is already created、mediaPlayer is prepared、video is loaded, video can play.
     */
    private void tryToPrepare() {
        if (mSurfaceIsReady && mVideoIsReady) {
            if (mMediaPlayer != null) {
                mInitialVideoWidth = mMediaPlayer.getVideoWidth();
                mInitialVideoHeight = mMediaPlayer.getVideoHeight();
            }

            resize();
            stopLoading();
            mCurrentState = KSCMediaState.PREPARED;
            if (mVideoPlayCallBack != null) {
                mVideoPlayCallBack.onPrepared();
            }

            if (mAutoPlay) {
                start();
            }
        }
    }

    /**
     * prepare for mediaPlayer
     */
    private void prepare() {
        startLoading();
        mVideoIsReady = false;
        mInitialVideoWidth = -1;
        mInitialVideoHeight = -1;
        mBufferProgress = 0;
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mCurrentState = KSCMediaState.PREPARING;
        mMediaPlayer.prepareAsync();
    }

    /**
     * resize surfaceView size
     */
    private void resize() {
        Log.d(TAG, "resize called");
        if (mInitialVideoWidth == -1 || mInitialVideoHeight == -1) {
            return;
        }
        View currentParent = (View) getParent();
        if (currentParent != null) {
            int screenWidth = currentParent.getWidth();
            int screenHeight = currentParent.getHeight();
            int newWidth = screenWidth;
            int newHeight = screenHeight;
            if (mContext.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                float videoProportion = (float) mInitialVideoWidth / (float) mInitialVideoHeight;
                float screenProportion = (float) screenWidth / (float) screenHeight;
                if (videoProportion > screenProportion) {
                    newWidth = screenWidth;
                    newHeight = (int) ((float) screenWidth / videoProportion);
                } else {
                    newWidth = (int) ((float) screenHeight * videoProportion);
                    newHeight = screenHeight;
                }
                Log.d(TAG, "resize: newWidth=" + newWidth + ", newHeight=" + newHeight);
            }
            if (newWidth == screenWidth && newHeight == screenHeight && mVideoPlayCallBack != null) {
                mVideoPlayCallBack.onFullScreen();
            }
            ViewGroup.LayoutParams layoutParams = mSurfaceView.getLayoutParams();
            if (layoutParams.width != newWidth || layoutParams.height != newHeight) {
                layoutParams.width = newWidth;
                layoutParams.height = newHeight;
                mSurfaceView.setLayoutParams(layoutParams);
            }
        }
    }

    /**
     * show progressBar
     */
    private void startLoading() {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * hide progressBar
     */
    private void stopLoading() {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(GONE);
        }
    }

    /**
     * get the current {@link KSCMediaState}
     *
     * @return currentState
     */
    public synchronized KSCMediaState getCurrentState() {
        return mCurrentState;
    }

    /**
     * set mediaController callback
     *
     * @param callback controller callback
     */
    public void setVideoPlayCallBack(KSCVideoPlayCallBack callback) {
        mVideoPlayCallBack = callback;
    }

    /**
     * get mediaPlayer is auto play flag
     *
     * @return
     */
    public boolean isAutoPlay() {
        return mAutoPlay;
    }

    /**
     * set mediaPlayer auto play flag
     *
     * @param autoPlay auto play state
     */
    public void setAutoPlay(boolean autoPlay) {
        mAutoPlay = autoPlay;
    }

    /**
     * Switch View to fullScreen mode
     * It saves currentState and call pause method.
     * When switchFullScreen is finished, it call the saves currentState before pause()
     */
    public void setFullScreen() {
        if (mMediaPlayer == null) {
            mediaPlayerError();
            return;
        }
        if (isPlaying()) {
            pause();
        }
        resize();
        if (!isPlaying()) {
            start();
        }
    }

    /**
     * {@link MediaPlayer} getCurrentPosition
     *
     * @return loaded video current position
     */
    public int getCurrentPosition() {
        if (mMediaPlayer != null && mCurrentState != KSCMediaState.IDLE) {
            return mMediaPlayer.getCurrentPosition();
        } else if (mCurrentState == KSCMediaState.IDLE) {
            return 0;
        } else {
            mediaPlayerError();
            return 0;
        }
    }

    /**
     * {@link MediaPlayer} getDuration
     *
     * @return loaded video duration
     */
    public int getDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        } else {
            mediaPlayerError();
            return 0;
        }
    }

    /**
     * {@link MediaPlayer} getVideoWidth
     *
     * @return loaded video width
     */
    public int getVideoWidth() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getVideoWidth();
        } else {
            mediaPlayerError();
            return 0;
        }
    }

    /**
     * {@link MediaPlayer} getVideoHeight
     *
     * @return loaded video height
     */
    public int getVideoHeight() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getVideoHeight();
        } else {
            mediaPlayerError();
            return 0;
        }
    }

    /**
     * {@link MediaPlayer} get video looping state
     *
     * @return loaded video looping state
     */
    public boolean isLooping() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isLooping();
        } else {
            mediaPlayerError();
            return false;
        }
    }

    /**
     * {@link MediaPlayer} set video looping state
     *
     * @param looping need to set looping state
     */
    public void setLooping(boolean looping) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(looping);
        } else {
            mediaPlayerError();
        }
    }

    /**
     * {@link MediaPlayer} get video playing state
     *
     * @return loaded video playing state
     */
    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        } else {
            mediaPlayerError();
            return false;
        }
    }

    /**
     * {@link MediaPlayer} reset mediaPlayer, Change currentState to IDLE
     */
    public void reset() {
        Log.d(TAG, "reset called");
        if (mMediaPlayer != null) {
            mCurrentState = KSCMediaState.IDLE;
            mMediaPlayer.reset();
        } else {
            mediaPlayerError();
        }
    }

    /**
     * {@link MediaPlayer} start play video
     */
    public void start() {
        Log.d(TAG, "start called");
        if (mMediaPlayer != null) {
            mCurrentState = KSCMediaState.STARTED;
            mMediaPlayer.setOnCompletionListener(this);
            if (mIsCompleted) {
                mIsCompleted = false;
                mMediaPlayer.seekTo(0);
            }
            mMediaPlayer.start();
            mVideoPlayCallBack.onStart();
        } else {
            mediaPlayerError();
        }
    }

    /**
     * {@link MediaPlayer} pause playing video
     */
    public void pause() {
        Log.d(TAG, "pause called");
        if (mMediaPlayer != null) {
            mCurrentState = KSCMediaState.PAUSED;
            mMediaPlayer.pause();
        } else {
            mediaPlayerError();
        }
    }

    /**
     * {@link MediaPlayer} stop video
     */
    public void stop() {
        Log.d(TAG, "stop called");
        if (mMediaPlayer != null) {
            mCurrentState = KSCMediaState.STOPPED;
            mMediaPlayer.stop();
        } else {
            mediaPlayerError();
        }
    }

    /**
     * {@link MediaPlayer} seeks to specified time position
     * It call method pause before call seekTo
     *
     * @param position the offset from start
     */
    public void seekTo(int position) {
        Log.d(TAG, "seekTo called, position=" + position);
        if (mMediaPlayer != null) {
            if (mMediaPlayer.getDuration() > -1 && position < mMediaPlayer.getDuration()) {
                mLastState = mCurrentState;
                pause();
                mMediaPlayer.seekTo(position);
                startLoading();
            }
        } else {
            mediaPlayerError();
        }
    }

    /**
     * {@link MediaPlayer} set the data source
     *
     * @param path the path of local file
     * @throws IOException
     */
    public void setVideoPath(String path) throws IOException {
        Log.d(TAG, "setVideoPath called, path:" + path);
        if (mMediaPlayer != null) {
            if (mCurrentState != KSCMediaState.IDLE) {
                mediaPlayerError();
                return;
            }
            mCurrentState = KSCMediaState.INITIALIZED;
            mMediaPlayer.setDataSource(path);
            prepare();
        } else {
            mediaPlayerError();
        }
    }

    /**
     * {@link MediaPlayer} set the data source
     *
     * @param uri http or rtsp url of stream
     * @throws IOException
     */
    public void setVideoURI(Uri uri) throws IOException {
        Log.d(TAG, "setVideoPath called, uri:" + uri.toString());
        if (mMediaPlayer != null) {
            if (mCurrentState != KSCMediaState.IDLE) {
                mediaPlayerError();
                return;
            }
            mCurrentState = KSCMediaState.INITIALIZED;
            mMediaPlayer.setDataSource(mContext, uri);
            prepare();
        } else {
            mediaPlayerError();
        }
    }

    /**
     * Sets the volume on this player.
     *
     * @param left  left volume scalar
     * @param right right volume scalar
     */
    public void setVolume(float left, float right) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(left, right);
        } else {
            mediaPlayerError();
        }
    }

    /**
     * close the volume on this player.
     */
    public void closeVolume() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Service.AUDIO_SERVICE);
        if (VERSION.SDK_INT > VERSION_CODES.M) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        }
    }

    /**
     * open the volume on this player.
     */
    public void resumeVolume() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Service.AUDIO_SERVICE);
        if (mMediaPlayer != null) {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        }
    }

    /**
     * 获得缓冲视频进度
     *
     * @return 缓冲百分比
     */
    public int getBufferProgress() {
        return mBufferProgress;
    }

    /**
     * 视频播放器错误
     */
    private void mediaPlayerError() {
        Log.e(TAG, "mediaPlayerError");
        if (mVideoPlayCallBack != null) {
            mVideoPlayCallBack.onMediaPlayerError("media player is not initial");
        }
    }

    /**
     * 释放MediaPlayer
     */
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}

package com.ksc.client.ads.view;

import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
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
import android.view.ViewParent;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.ksc.client.ads.KSCMediaState;
import com.ksc.client.ads.callback.KSCVideoPlayCallBack;

import java.io.IOException;

/**
 * Created by Alamusi on 2016/8/18.
 */
public class KSCVideoView extends RelativeLayout implements SurfaceHolder.Callback, OnPreparedListener, OnErrorListener, OnSeekCompleteListener, OnCompletionListener {

    private static final String TAG = KSCVideoView.class.getSimpleName();
    private Context mContext;
    private MediaPlayer mMediaPlayer;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private View mLoadingView;
    private ViewGroup mParentView;
    private ViewGroup.LayoutParams mLastLayoutParams;
    private boolean mSurfaceIsReady;
    private boolean mVideoIsReady;
    private boolean mAutoPlay;
    private boolean mIsFullScreen;
    private boolean mIsCompleted;
    private boolean mDetachedByFullScreen;
    private int mInitialVideoWidth;
    private int mInitialVideoHeight;
    private KSCMediaState mCurrentState;
    private KSCMediaState mLastState;
    private KSCVideoPlayCallBack mVideoPlayCallBack;

    public KSCVideoView(Context context) {
        super(context);
        mContext = context;
        initUI(context);
    }

    public KSCVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initUI(context);
    }

    public KSCVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initUI(context);
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow called, detachedByFullScreen=" + mDetachedByFullScreen);
        super.onDetachedFromWindow();
        if (!mDetachedByFullScreen) {
            if (mMediaPlayer != null) {
                if (mCurrentState != KSCMediaState.IDLE && mVideoPlayCallBack != null) {
                    mVideoPlayCallBack.onCloseVideo(getCurrentPosition());
                }
                mMediaPlayer.setOnPreparedListener(null);
                mMediaPlayer.setOnErrorListener(null);
                mMediaPlayer.setOnSeekCompleteListener(null);
                mMediaPlayer.setOnCompletionListener(null);
                if (isPlaying()) {
                    stop();
                }
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            mVideoIsReady = false;
            mSurfaceIsReady = false;
            mCurrentState = KSCMediaState.END;
        }
        mDetachedByFullScreen = false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceCreated Called");
        mMediaPlayer.setDisplay(mHolder);
        if (!mSurfaceIsReady) {
            mSurfaceIsReady = true;
            if (mCurrentState != KSCMediaState.PREPARED && mCurrentState != KSCMediaState.STARTED
                    && mCurrentState != KSCMediaState.PAUSED && mCurrentState != KSCMediaState.PLAYBACKCOMPLETED) {
                tryToPrepare();
            }
        }
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
            mVideoPlayCallBack.onCompletion(mediaPlayer);
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.d(TAG, "onError Called, what=" + i + ", extra=" + i1);
        stopLoading();
        mCurrentState = KSCMediaState.ERROR;
        if (mVideoPlayCallBack != null) {
            mVideoPlayCallBack.onError(mediaPlayer, i, i1);
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

    /**
     * Initializes the UI
     *
     * @param context Context
     */
    protected void initUI(Context context) {
        Log.d(TAG, "initUI called");
        if (isInEditMode()) {
            return;
        }
        mAutoPlay = true;
        mIsFullScreen = false;
        mCurrentState = KSCMediaState.IDLE;
        setBackgroundColor(Color.BLACK);

        // Initialize mediaPlayer
        mMediaPlayer = new MediaPlayer();

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
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
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

            if (mAutoPlay) {
                start();
            }
        }
    }

    /**
     * prepare for mediaPlayer
     */
    private void prepare() {
        if (mVideoPlayCallBack != null) {
            mVideoPlayCallBack.onClickAd();
        }
        startLoading();
        mVideoIsReady = false;
        mInitialVideoWidth = -1;
        mInitialVideoHeight = -1;
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
            float videoProportion = (float) mInitialVideoWidth / (float) mInitialVideoHeight;
            int screenWidth = currentParent.getWidth();
            int screenHeight = currentParent.getHeight();
            float screenProportion = (float) screenWidth / (float) screenHeight;

            int newWidth;
            int newHeight;
            if (videoProportion > screenProportion) {
                newWidth = screenWidth;
                newHeight = (int) ((float) screenWidth / videoProportion);
            } else {
                newWidth = (int) ((float) screenHeight * videoProportion);
                newHeight = screenHeight;
            }
            Log.d(TAG, "resize: newWidth=" + newWidth + ", newHeight=" + newHeight);
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
            throw new RuntimeException("Media Player is not initialized");
        }
        if (isPlaying()) {
            pause();
        }
        if (!mIsFullScreen) {
            mIsFullScreen = true;
            View rootView = getRootView();
            View v = rootView.findViewById(android.R.id.content);
            ViewParent viewParent = getParent();
            if (viewParent instanceof ViewGroup) {
                if (mParentView == null) {
                    mParentView = (ViewGroup) viewParent;
                }
                // Prevents MediaPlayer to became invalidated and released
                mDetachedByFullScreen = true;
                // Saves the last state (LayoutParams) of view to restore after
                mLastLayoutParams = this.getLayoutParams();
                mParentView.removeView(this);
            } else {
                Log.e(TAG, "Parent View is not a ViewGroup");
            }
            if (v instanceof ViewGroup) {
                ((ViewGroup) v).addView(this);
            } else {
                Log.e(TAG, "RootView is not a ViewGroup");
            }
        } else {
            mIsFullScreen = false;
            ViewParent viewParent = getParent();
            if (viewParent instanceof ViewGroup) {
                // Check if parent view is still available
                boolean parentHasParent = false;
                if (mParentView != null && mParentView.getParent() != null) {
                    parentHasParent = true;
                    mDetachedByFullScreen = true;
                }
                ((ViewGroup) viewParent).removeView(this);
                if (parentHasParent) {
                    mParentView.addView(this);
                    this.setLayoutParams(mLastLayoutParams);
                }
            }
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
            throw new RuntimeException("Media Player is not initialized");
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
            throw new RuntimeException("Media Player is not initialized");
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
            throw new RuntimeException("Media Player is not initialized");
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
            throw new RuntimeException("Media Player is not initialized");
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
            throw new RuntimeException("Media Player is not initialized");
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
            throw new RuntimeException("Media Player is not initialized");
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
            throw new RuntimeException("Media Player is not initialized");
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
            throw new RuntimeException("Media Player is not initialized");
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
            throw new RuntimeException("Media Player is not initialized");
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
            throw new RuntimeException("Media Player is not initialized");
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
            throw new RuntimeException("Media Player is not initialized");
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
            throw new RuntimeException("Media Player is not initialized");
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
                throw new RuntimeException("FSVideoView Invalid State: " + mCurrentState);
            }
            mCurrentState = KSCMediaState.INITIALIZED;
            mMediaPlayer.setDataSource(path);
            prepare();
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    /**
     * {@link MediaPlayer} set the data source
     *
     * @param uri http or rtsp url of stream
     * @throws IOException
     */
    public void setVideoURI(Uri uri) throws IOException {
        if (mMediaPlayer != null) {
            if (mCurrentState != KSCMediaState.IDLE) {
                throw new RuntimeException("FSVideoView Invalid State: " + mCurrentState);
            }
            mCurrentState = KSCMediaState.INITIALIZED;
            mMediaPlayer.setDataSource(mContext, uri);
            prepare();
        } else {
            throw new RuntimeException("Media Player is not initialized");
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
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    /**
     * close the volume on this player.
     */
    public void closeVolume() {
        setVolume(0, 0);
    }

    /**
     * open the volume on this player.
     */
    public void resumeVolume() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Service.AUDIO_SERVICE);
        setVolume(audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM), audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
    }
}

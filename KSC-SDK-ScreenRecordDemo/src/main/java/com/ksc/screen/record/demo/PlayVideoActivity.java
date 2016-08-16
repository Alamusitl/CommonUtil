package com.ksc.screen.record.demo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PlayVideoActivity extends AppCompatActivity {

    private static final String TAG = PlayVideoActivity.class.getSimpleName();
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);

        mSurfaceView = (SurfaceView) findViewById(R.id.playVideo);
        mHolder = mSurfaceView.getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                mPlayer.setDisplay(mHolder);
                mPlayer.prepareAsync();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                Log.i(TAG, "surfaceChanged: i=" + i + ", i1=" + i1 + ", i2=" + i2);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                Log.i(TAG, "surfaceDestroyed: ");
                mPlayer.pause();
            }
        });
        mPlayer = new MediaPlayer();
        MediaCallback mMediaCallback = new MediaCallback();
        mPlayer.setOnCompletionListener(mMediaCallback);
        mPlayer.setOnErrorListener(mMediaCallback);
        mPlayer.setOnInfoListener(mMediaCallback);
        mPlayer.setOnPreparedListener(mMediaCallback);
        mPlayer.setOnSeekCompleteListener(mMediaCallback);
        mPlayer.setOnVideoSizeChangedListener(mMediaCallback);

        String videoPath = ((DemoApplication) getApplication()).getVideoPath();
        List<String> videoList = new LinkedList<>();
        for (File file : new File(videoPath).listFiles()) {
            if (file.isFile() && file.getName().endsWith(".mp4")) {
                videoList.add(file.getAbsolutePath());
            }
        }
        try {
            mPlayer.setDataSource(videoList.get(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    class MediaCallback implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener
            , MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnVideoSizeChangedListener {

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            Log.i(TAG, "onCompletion: ");
            finish();
        }

        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            Log.i(TAG, "onError: i = " + i + ", i1 = " + i1);
            return false;
        }

        @Override
        public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
            Log.i(TAG, "onInfo: i = " + i + ", i1 = " + i1);
            return false;
        }

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            int videoWidth = mediaPlayer.getVideoWidth();
            int videoHeight = mediaPlayer.getVideoHeight();

            DisplayMetrics matrix = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(matrix);

            if (videoWidth > matrix.widthPixels || videoHeight > matrix.heightPixels) {
                float wRatio = (float) videoWidth / (float) matrix.widthPixels;
                float hRatio = (float) videoHeight / (float) matrix.heightPixels;
                float ratio = Math.max(wRatio, hRatio);

                videoWidth = (int) Math.ceil((float) videoWidth / ratio);
                videoHeight = (int) Math.ceil((float) videoHeight / ratio);

                mSurfaceView.setLayoutParams(new LinearLayout.LayoutParams(videoWidth, videoHeight));
                mediaPlayer.start();
            }
        }

        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            Log.i(TAG, "onSeekComplete: ");
        }

        @Override
        public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
            Log.i(TAG, "onVideoSizeChanged: i=" + i + ", i1=" + i1);
        }
    }
}

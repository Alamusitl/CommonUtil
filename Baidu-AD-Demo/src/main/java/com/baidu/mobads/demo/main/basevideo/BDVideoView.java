package com.baidu.mobads.demo.main.basevideo;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

import com.baidu.mobads.interfaces.IXAdConstants4PDK;
import com.baidu.mobads.interfaces.IXAdContext;

/**
 * This class extends the standard Android VideoView. If a BaiDu IXAdContext is set, then update the video state in
 * response to start(), play(), pause() calls.
 *
 * @author baiduer
 */
public class BDVideoView extends VideoView {

    private static final String TAG = "FWVideoView";

    private IXAdContext mBaiduAdContext = null;

    private OnCompletionListener externalOnCompletionListener = null;

    public BDVideoView(Context androidContext) {
        super(androidContext);
    }

    public BDVideoView(Context androidContext, AttributeSet attributeSet) {
        super(androidContext, attributeSet);
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener l) {
        externalOnCompletionListener = l;

        // Register a listener that fires the provided listener l
        // as well as sets the video state to complete if a
        // Baidu IXAdContext is provided
        super.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                if (externalOnCompletionListener != null) {
                    externalOnCompletionListener.onCompletion(mp);
                }

                if (mBaiduAdContext != null) {
                    Log.d(TAG, "Setting video state to completed");
                    mBaiduAdContext.setContentVideoState(IXAdConstants4PDK.VideoState.COMPLETED);
                }
            }
        });
    }

    /**
     * Provide a BaiDu ad context to be used to communicate video playback state changes
     *
     * @param context
     */
    public void setBaiduAdContext(IXAdContext context) {
        this.mBaiduAdContext = context;
        if (context != null) {
            // Do this in case a completion listener never gets set
            this.setOnCompletionListener(null);
        }
    }

    @Override
    public void pause() {
        super.pause();
        if (mBaiduAdContext != null) {
            Log.d(TAG, "pause(): Setting video state to paused");
            mBaiduAdContext.setContentVideoState(IXAdConstants4PDK.VideoState.PAUSED);
        }
    }

    @Override
    public void resume() {
        super.resume();
        if (mBaiduAdContext != null) {
            Log.d(TAG, "resume(): Setting video state to playing");

            mBaiduAdContext.setContentVideoState(IXAdConstants4PDK.VideoState.PLAYING);
        }
    }

    @Override
    public void start() {
        super.start();
        if (mBaiduAdContext != null) {
            Log.d(TAG, "start(): Setting video state to playing");
            mBaiduAdContext.setContentVideoState(IXAdConstants4PDK.VideoState.PLAYING);
        }
    }
}

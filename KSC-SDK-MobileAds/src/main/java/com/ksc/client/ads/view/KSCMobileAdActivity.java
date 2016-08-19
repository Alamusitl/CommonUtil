package com.ksc.client.ads.view;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.ksc.client.ads.KSCViewUtils;
import com.ksc.client.ads.callback.KSCVideoPlayCallBack;

import java.io.File;
import java.io.IOException;

public class KSCMobileAdActivity extends Activity {

    private static final String TAG = KSCMobileAdActivity.class.getSimpleName();

    private View.OnClickListener mCloseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            closeView();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        RelativeLayout rootView = new RelativeLayout(this);
        addContentView(rootView, lp);

        KSCVideoView mediaPlayer = new KSCVideoView(this);
        rootView.addView(mediaPlayer, lp);

        lp = new LayoutParams(100, 100);
        ImageView viewClose = new ImageView(this);
        viewClose.setId(KSCViewUtils.generateViewId());
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.setMargins(0, 15, 15, 0);
        viewClose.setOnClickListener(mCloseClickListener);
        viewClose.setImageBitmap(KSCViewUtils.getBitmapFromAssets(this, "ksc_controller_close.png"));
        viewClose.setBackgroundColor(Color.WHITE);
        rootView.addView(viewClose, lp);

        mediaPlayer.setVideoPlayCallBack(new KSCVideoPlayCallBack() {
            @Override
            public void onStart() {
                Log.i(TAG, "onStart: ");
            }

            @Override
            public void onFullScreen() {
                Log.i(TAG, "onFullScreen: ");
            }

            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.i(TAG, "onCompletion: ");
            }

            @Override
            public void onError(MediaPlayer mediaPlayer, int what, int extra) {
                Log.i(TAG, "onError: what=" + what + ", extra=" + extra);
            }

            @Override
            public void onClickAd() {
                Log.i(TAG, "onClickAd: ");
            }

            @Override
            public void onCloseVideo(int progress) {

            }
        });
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "test.mp4";
        try {
            mediaPlayer.setVideoPath(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeView() {
        setResult(RESULT_CANCELED);
        finish();
    }

}

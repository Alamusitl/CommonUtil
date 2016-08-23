package com.ksc.mobile.ads.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.ksc.client.ads.KSCADAgent;
import com.ksc.client.ads.KSCMobileAdKeyCode;
import com.ksc.client.ads.callback.KSCAdEventListener;
import com.ksc.client.ads.view.KSCMobileAdActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        KSCADAgent.getInstance().setDebug(true);
        KSCADAgent.getInstance().init(this, "", "", "", new KSCAdEventListener() {
            @Override
            public void onAdExist(boolean isAdExist) {

            }

            @Override
            public void onVideoCached(boolean isCached) {

            }

            @Override
            public void onVideoStart() {

            }

            @Override
            public void onVideoCompletion() {

            }

            @Override
            public void onVideoClose(int currentPosition) {

            }

            @Override
            public void onVideoError(String reason) {

            }

            @Override
            public void onLoadingPageShow(boolean showSuccess) {

            }

            @Override
            public void onLoadingPageClose() {

            }

            @Override
            public void onNetRequestError(String error) {

            }
        });

        findViewById(R.id.btnShowVideoAd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "test.mp4";
                Intent intent = new Intent(MainActivity.this, KSCMobileAdActivity.class);
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_TYPE, KSCMobileAdKeyCode.VIDEO_IN_CACHE);
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_PATH, path);
                startActivityForResult(intent, KSCMobileAdKeyCode.KEY_ACTIVITY_REQUEST);
            }
        });
        findViewById(R.id.btnStreamVideoAd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, KSCMobileAdActivity.class);
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_TYPE, KSCMobileAdKeyCode.VIDEO_IN_STREAM);
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_PATH, "http://v1.mukewang.com/a45016f4-08d6-4277-abe6-bcfd5244c201/L.mp4");
                startActivityForResult(intent, KSCMobileAdKeyCode.KEY_ACTIVITY_REQUEST);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        KSCADAgent.getInstance().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        KSCADAgent.getInstance().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KSCADAgent.getInstance().onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        KSCADAgent.getInstance().onActivityResult(requestCode, resultCode, data);
    }
}

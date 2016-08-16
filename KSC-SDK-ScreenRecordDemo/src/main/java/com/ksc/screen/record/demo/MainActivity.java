package com.ksc.screen.record.demo;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private MediaProjectionManager mMediaProjectionManager;
    private boolean mIsServiceRunning;
    private Button mBtnMediaService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnMediaService = (Button) findViewById(R.id.btnMediaService);
        findViewById(R.id.btnShowPic).setOnClickListener(this);
        findViewById(R.id.btnPlayVideo).setOnClickListener(this);
        mBtnMediaService.setOnClickListener(this);

        mMediaProjectionManager = ((DemoApplication) getApplication()).getMediaProjectionManager();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnMediaService:
                handleClickMediaService();
                break;
            case R.id.btnShowPic:
                showCapture();
                break;
            case R.id.btnPlayVideo:
                playRecordVideo();
                break;
            default:
                break;
        }
    }

    private void playRecordVideo() {
        startActivity(new Intent(this, VideoViewActivity.class));
    }

    private void showCapture() {
        startActivity(new Intent(this, ShowCaptureActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK) {
                ((DemoApplication) getApplication()).setResultCode(resultCode);
                ((DemoApplication) getApplication()).setData(data);
                startService(new Intent(this, MediaProjectionService.class));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaProjectionManager != null) {
            mMediaProjectionManager = null;
        }
    }

    private void handleClickMediaService() {
        if (mIsServiceRunning) {
            mIsServiceRunning = false;
            mBtnMediaService.setText("开启服务");
            stopService(new Intent(this, MediaProjectionService.class));
        } else {
            mIsServiceRunning = true;
            mBtnMediaService.setText("停止服务");
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
        }
    }
}

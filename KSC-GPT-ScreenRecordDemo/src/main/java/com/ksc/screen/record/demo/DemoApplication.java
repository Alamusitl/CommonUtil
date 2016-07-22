package com.ksc.screen.record.demo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by Alamusi on 2016/7/17.
 */
public class DemoApplication extends Application {

    private static final String TAG = DemoApplication.class.getSimpleName();
    private MediaProjectionManager mMediaProjectionManager;
    private String mImagePath;
    private String mVideoPath;
    private int mResultCode;
    private Intent mData;

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File externalDir = getExternalFilesDir(null);
            if (externalDir != null && !externalDir.exists()) {
                if (!externalDir.mkdirs()) {
                    Log.e(TAG, "onCreate: mkdirs dir " + externalDir.getAbsolutePath() + "is fail");
                }
            }
            if (externalDir != null) {
                mImagePath = externalDir.getAbsolutePath();
                mVideoPath = externalDir.getAbsolutePath();
            } else {
                mImagePath = getFilesDir().getAbsolutePath();
                mVideoPath = getFilesDir().getAbsolutePath();
            }
        }
    }

    public MediaProjectionManager getMediaProjectionManager() {
        return mMediaProjectionManager;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public String getVideoPath() {
        return mVideoPath;
    }

    public int getResultCode() {
        return mResultCode;
    }

    public void setResultCode(int resultCode) {
        mResultCode = resultCode;
    }

    public Intent getData() {
        return mData;
    }

    public void setData(Intent data) {
        mData = data;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mMediaProjectionManager = null;
    }
}

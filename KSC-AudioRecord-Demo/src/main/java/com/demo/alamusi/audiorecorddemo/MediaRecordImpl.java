package com.demo.alamusi.audiorecorddemo;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * Created by Alamusi on 2016/7/26.
 */
public class MediaRecordImpl extends Service {

    private MediaRecorder mMediaRecorder;
    private boolean mIsRecord = false;
    private String mSavePath = Util.getAmrFilePath();

    public int startRecord() {
        if (Util.isSDCardAvailable()) {
            if (mIsRecord) {
                return Code.STATE_RECORDING;
            } else {
                if (mMediaRecorder == null) {
                    createMediaRecorder();
                }
                try {
                    mMediaRecorder.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                    return Code.STATE_UNKNOWN;
                }
                mMediaRecorder.start();
                mIsRecord = true;
                return Code.STATE_SUCCESS;
            }
        }
        return Code.STATE_NOSDCARD;
    }

    private void createMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(Common.AUDIO_SOURCE);
        mMediaRecorder.setAudioSamplingRate(Common.SIMPLE_RATE_IN_HZ);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        File file = new File(mSavePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
    }

    private void close() {
        if (mMediaRecorder != null) {
            mIsRecord = false;
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createMediaRecorder();
        startRecord();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        close();
    }
}

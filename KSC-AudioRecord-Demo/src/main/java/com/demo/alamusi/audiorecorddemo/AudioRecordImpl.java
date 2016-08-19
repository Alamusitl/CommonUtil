package com.demo.alamusi.audiorecorddemo;

import android.app.Service;
import android.content.Intent;
import android.media.AudioRecord;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Alamusi on 2016/7/26.
 */
public class AudioRecordImpl extends Service {

    private AudioRecord mAudioRecord;
    private boolean mIsRecording = false;
    private int mBufferSizeInBytes;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createAudioRecord();
        startAudioRecord();
        new Thread() {
            @Override
            public void run() {
                try {
                    startSaveData(Util.getRawFilePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void createAudioRecord() {
        mAudioRecord = new AudioRecord(Common.AUDIO_SOURCE, Common.SIMPLE_RATE_IN_HZ, Common.CHANNEL_CONFIG, Common.AUDIO_FORMAT, Common.mBufferSizeInBytes);
    }

    private int startAudioRecord() {
        if (Util.isSDCardAvailable()) {
            if (mIsRecording) {
                return Code.STATE_RECORDING;
            } else {
                if (mAudioRecord == null) {
                    createAudioRecord();
                }
                mAudioRecord.startRecording();
                mIsRecording = true;
                return Code.STATE_SUCCESS;
            }
        } else {
            return Code.STATE_NOSDCARD;
        }
    }

    private void startSaveData(String path) throws IOException {
        byte[] audioData = new byte[mBufferSizeInBytes];
        int readSize;
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        while (mIsRecording) {
            readSize = mAudioRecord.read(audioData, 0, mBufferSizeInBytes);
            if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {
                fos.write(audioData);
            }
        }
        fos.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAudioRecord != null) {
            mIsRecording = false;
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }


}

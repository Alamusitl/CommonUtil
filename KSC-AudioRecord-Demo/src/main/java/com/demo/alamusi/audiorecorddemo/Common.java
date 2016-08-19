package com.demo.alamusi.audiorecorddemo;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Created by Alamusi on 2016/7/26.
 */
public class Common {

    public static final int SIMPLE_RATE_IN_HZ = 44100;
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int mBufferSizeInBytes = AudioRecord.getMinBufferSize(SIMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT);
}

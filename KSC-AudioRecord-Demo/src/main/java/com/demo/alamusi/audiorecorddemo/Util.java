package com.demo.alamusi.audiorecorddemo;

import android.os.Environment;

import java.io.File;

/**
 * Created by Alamusi on 2016/7/26.
 */
public class Util {

    private static String AUDIO_RAW_FILENAME = "RawAudio.raw";
    private static String AUDIO_WAV_FILENAME = "WavAudio.wav";
    private static String AUDIO_AMR_FILENAME = "AmrAudio.amr";

    public static boolean isSDCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static String getRawFilePath() {
        String audioRawPath = "";
        if (isSDCardAvailable()) {
            audioRawPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + AUDIO_RAW_FILENAME;
        }
        return audioRawPath;
    }

    public static String getWavFilePath() {
        String audioWavPath = "";
        if (isSDCardAvailable()) {
            audioWavPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + AUDIO_WAV_FILENAME;
        }
        return audioWavPath;
    }

    public static String getAmrFilePath() {
        String audioAmrPath = "";
        if (isSDCardAvailable()) {
            audioAmrPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + AUDIO_AMR_FILENAME;
        }
        return audioAmrPath;
    }

    public static long getFileSize(String path) {
        File file = new File(path);
        if (!file.exists() || file.isDirectory()) {
            return -1;
        }
        return file.length();
    }
}

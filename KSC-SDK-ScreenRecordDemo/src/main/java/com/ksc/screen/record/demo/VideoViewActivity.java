package com.ksc.screen.record.demo;

import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.VideoView;

import com.ksc.screen.record.demo.util.FileSizeUtil;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class VideoViewActivity extends AppCompatActivity {

    private static final String TAG = VideoViewActivity.class.getSimpleName();
    private Spinner mSpinner;
    private TextView mTVFileInfo;
    private MediaMetadataRetriever mMediaMetadataRetriever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);

        final VideoView videoView = (VideoView) findViewById(R.id.vv_playVideo);
        mSpinner = (Spinner) findViewById(R.id.spinner);
        mTVFileInfo = (TextView) findViewById(R.id.tv_fileInfo);
        videoView.setMediaController(new MediaController(this));
        final List<String> mList = new LinkedList<>();
        List<String> mNameList = new LinkedList<>();
        for (File file : (new File(((DemoApplication) getApplication()).getVideoPath())).listFiles()) {
            if (file.isFile() && file.getName().endsWith(".mp4")) {
                mList.add(file.getAbsolutePath());
                mNameList.add(file.getName());
            }
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mNameList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(arrayAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setPath(mList.get(i));
                videoView.setVideoPath(mList.get(i));
                videoView.requestFocus();
                String msg = "大小：" + FileSizeUtil.getAutoFileOrFilesSize(mList.get(i));
                msg += "\n";
                msg += "长度: " + getDuration();
                msg += "\n";
                msg += "Mime_Type: " + getMIME_TYPE();
                msg += "\n";
                msg += "BitRate: " + getBitRate();
                msg += "\n";
                msg += "FrameRate: " + getFrameRate();

//                getInfo(mList.get(i));

                mTVFileInfo.setText(msg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mMediaMetadataRetriever = new MediaMetadataRetriever();
    }

    private String getDuration() {
        String duration = mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Log.i(TAG, "getDuration: " + duration);
        return duration;
    }

    private String getMIME_TYPE() {
        String mime_type = mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
        Log.i(TAG, "getMIME_TYPE: " + mime_type);
        return mime_type;
    }

    private String getBitRate() {
        String bit_rate = mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
        Log.i(TAG, "getBitRate: " + bit_rate);
        return bit_rate;
    }

    private String getFrameRate() {
        String frame_rate = mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE);
        Log.i(TAG, "getFrameRate: " + frame_rate);
        return frame_rate;
    }

    private void setPath(String url) {
        if (url != null) {
            mMediaMetadataRetriever.setDataSource(url);
        }
    }

    private void getInfo(String path) {
        File file = new File(path);
        Cursor cursor = getContentResolver().query(Uri.fromFile(file), null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
                String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                String path1 = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                long duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                Log.i(TAG, "getInfo: id=" + id);
                Log.i(TAG, "getInfo: title=" + title);
                Log.i(TAG, "getInfo: album=" + album);
                Log.i(TAG, "getInfo: artist=" + artist);
                Log.i(TAG, "getInfo: displayName=" + displayName);
                Log.i(TAG, "getInfo: mimeType=" + mimeType);
                Log.i(TAG, "getInfo: path=" + path1);
                Log.i(TAG, "getInfo: duration=" + duration);
                Log.i(TAG, "getInfo: size=" + size);
            }
        } else {
            Log.i(TAG, "getInfo: cursor is null");
        }
    }
}

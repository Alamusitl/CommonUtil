package com.ksc.screen.record.demo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Alamusi on 2016/7/17.
 */
public class MediaProjectionService extends Service {

    private static final String TAG = MediaProjectionService.class.getSimpleName();
    private String mImagePath;
    private String mVideoPath;
    private MediaProjection mMediaProjection;
    private int mWindowWidth;
    private int mWindowHeight;
    private int mScreenDensity;
    private ImageReader mImageReader;
    private VirtualDisplay mVirtualDisplay;
    private WindowManager mWindowManager;
    private LinearLayout mRootView;
    private boolean mIsRecording = true;
    private AtomicBoolean mQuit = new AtomicBoolean(false);
    private Surface mSurface;
    private MediaCodec mMediaCodec;
    private MediaMuxer mMediaMuxer;
    private boolean mMuxerStarted;
    private int mTrackIndex;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createEnvironment();
        createFloatView();
    }

    private void createEnvironment() {
        mImagePath = ((DemoApplication) getApplication()).getImagePath();
        mVideoPath = ((DemoApplication) getApplication()).getVideoPath();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        mScreenDensity = displayMetrics.densityDpi;
        mWindowWidth = displayMetrics.widthPixels;
        mWindowHeight = displayMetrics.heightPixels;
        mImageReader = ImageReader.newInstance(mWindowWidth, mWindowHeight, PixelFormat.RGBA_8888, 2);
    }

    private void createFloatView() {
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.RGBA_8888);
        params.x = 0;
        params.y = mWindowHeight / 2;
        params.gravity = Gravity.START | Gravity.TOP;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        mRootView = (LinearLayout) inflater.inflate(R.layout.float_view, null);
        LinearLayout ll_capture = (LinearLayout) mRootView.findViewById(R.id.ll_capture);
        LinearLayout ll_record = (LinearLayout) mRootView.findViewById(R.id.ll_record);
        final TextView tv_record = (TextView) ll_record.findViewById(R.id.tv_record);
        final ImageView iv_record = (ImageView) ll_record.findViewById(R.id.iv_record);
        mWindowManager.addView(mRootView, params);

        ll_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScreenCapture(view);
            }
        });
        ll_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsRecording = !mIsRecording;
                if (mIsRecording) {
                    iv_record.setImageResource(R.mipmap.ic_record);
                    ToastMsg("停止录屏");
                    tv_record.setText(getText(R.string.ksc_record));
                } else {
                    iv_record.setImageResource(R.mipmap.ic_recording);
                    ToastMsg("开始录屏");
                    tv_record.setText(getText(R.string.ksc_record_stop));
                }
                ScreenRecord(view);
            }
        });

    }

    private void ScreenCapture(final View view) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                createVirtualDisplay(view);
            }
        });
        int mDelayTime = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startCapture();
            }
        }, mDelayTime);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                destroyVirtualDisplay();
            }
        }, mDelayTime);

    }

    private void startCapture() {
        String imageName = "capture_" + System.currentTimeMillis() + ".png";
        Log.e(TAG, "image name is : " + imageName);
        Image image = mImageReader.acquireLatestImage();
        if (image == null) {
            Log.e(TAG, "startCapture: image is null");
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();

        if (bitmap != null) {
            Log.e(TAG, "bitmap  create success ");
            try {
                File fileFolder = new File(mImagePath);
                if (!fileFolder.exists()) {
                    fileFolder.mkdirs();
                }
                File file = new File(mImagePath, imageName);
                if (!file.exists()) {
                    Log.e(TAG, "file create success ");
                    file.createNewFile();
                }
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
                Log.e(TAG, "file save success ");
                ToastMsg("截图成功");
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    private void createVirtualDisplay(View view) {
        if (mMediaProjection == null) {
            int resultCode = ((DemoApplication) getApplication()).getResultCode();
            Intent data = ((DemoApplication) getApplication()).getData();
            mMediaProjection = ((DemoApplication) getApplication()).getMediaProjectionManager().getMediaProjection(resultCode, data);
        }
        if (view.getId() == R.id.ll_capture) {
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture", mWindowWidth, mWindowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mImageReader.getSurface(), null, null);
        } else if (view.getId() == R.id.ll_record) {
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenRecord", mWindowWidth, mWindowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mSurface, null, null);
        }
    }

    private void destroyVirtualDisplay() {
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
    }

    private void ToastMsg(final String msg) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void ScreenRecord(View view) {
        if (mIsRecording) {
            mQuit.set(true);
        } else {
            mQuit.set(false);
            configureMediaCodec();
            createVirtualDisplay(view);
            new Thread() {
                @Override
                public void run() {
                    startRecord();
                }
            }.start();
        }
    }

    private void configureMediaCodec() {
        //MediaFormat这个类是用来定义视频格式相关信息的
        //video/avc,这里的avc是高级视频编码Advanced Video Coding
        //mWidth和mHeight是视频的尺寸，这个尺寸不能超过视频采集时采集到的尺寸，否则会直接crash
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mWindowWidth, mWindowHeight);
        //设置码率，通常码率越高，视频越清晰，但是对应的视频也越大，这个值我默认设置成了2000000，也就是通常所说的2M，这已经不低了，如果你不想录制这么清晰的，你可以设置成500000，也就是500k
//        format.setInteger(MediaFormat.KEY_BIT_RATE, 6000000);//码率 6M，清晰度
//        format.setInteger(MediaFormat.KEY_BIT_RATE, 2000000);//码率 2M，清晰度
        format.setInteger(MediaFormat.KEY_BIT_RATE, 500000);//码率 500K，清晰度
        //设置帧率，通常这个值越高，视频会显得越流畅，一般默认我设置成30，你最低可以设置成24，不要低于这个值，低于24会明显卡顿
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        //COLOR_FormatSurface这里表明数据将是一个graphicbuffer元数据
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        //I_FRAME_INTERVAL是指的帧间隔，这是个很有意思的值，它指的是，关键帧的间隔时间。通常情况下，你设置成多少问题都不大。
        //比如你设置成10，那就是10秒一个关键帧。但是，如果你有需求要做视频的预览，那你最好设置成1
        //因为如果你设置成10，那你会发现，10秒内的预览都是一个截图
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
        try {
            mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mSurface = mMediaCodec.createInputSurface();
        mMediaCodec.start();
    }

    private void startRecord() {
        try {
            mMediaMuxer = new MediaMuxer(mVideoPath + File.separator + System.currentTimeMillis() + ".mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            recordVirtualDisplay();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            release();
        }
    }

    private void recordVirtualDisplay() {
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (!mQuit.get()) {
            int bufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
            Log.i(TAG, "dequeue output buffer index=" + bufferIndex);
            if (bufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {// 请求超时
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (bufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {// 后续输出格式变化
                if (mMuxerStarted) {
                    throw new IllegalStateException("output format already changed!");
                }
                mTrackIndex = mMediaMuxer.addTrack(mMediaCodec.getOutputFormat());
                if (!mMuxerStarted) {
                    mMediaMuxer.start();
                    mMuxerStarted = true;
                }
            } else if (bufferIndex >= 0) {
                if (!mMuxerStarted) {
                    throw new IllegalStateException("MediaMuxer dose not call addTrack(format) ");
                }
                ByteBuffer encodedData = mMediaCodec.getOutputBuffer(bufferIndex);
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0;
                }
                if (bufferInfo.size == 0) {
                    encodedData = null;
                }
                if (encodedData != null) {
                    encodedData.position(bufferInfo.offset);
                    encodedData.limit(bufferInfo.offset + bufferInfo.size);
                    mMediaMuxer.writeSampleData(mTrackIndex, encodedData, bufferInfo);// 写入
                }
                mMediaCodec.releaseOutputBuffer(bufferIndex, false);
            }
        }
    }

    private void release() {
        mQuit.set(true);
        mMuxerStarted = false;
        Log.i(TAG, " release() ");
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mMediaMuxer != null) {
            mMediaMuxer.stop();
            mMediaMuxer.release();
            mMediaMuxer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
        if (mWindowManager != null) {
            mWindowManager.removeView(mRootView);
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }
}

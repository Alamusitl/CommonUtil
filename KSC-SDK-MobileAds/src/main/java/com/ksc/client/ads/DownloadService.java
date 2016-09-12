package com.ksc.client.ads;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Alamusi on 2016/8/30.
 */
public class DownloadService extends IntentService {

    public static final String EXTRA_DOWNLOAD_URL = "downloadUrl";
    public static final String EXTRA_SHOW_NOTIFY = "downloadShowNotify";
    public static final String EXTRA_DOWNLOAD_PATH = "downloadPath";
    public static final String EXTRA_DOWNLOAD_APP_NAME = "downloadAppName";
    private static final String TAG = DownloadService.class.getSimpleName();

    private static final int KEY_NOTIFICATION_ID = 0;
    private static final int TIMEOUT = 10 * 1000;
    private static final int KEY_DOWNLOADING = 0;
    private static final int KEY_DOWNLOAD_FAIL = 1;
    private static final int KEY_DOWNLOAD_SUCCESS = 2;

    private NotificationManager mManager;
    private NotificationCompat.Builder mBuilder;
    private PendingIntent pendingIntent;
    private boolean mShowNotify = false;
    private int mLastPresent = 0;
    private String mDownloadAppName;

    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case KEY_DOWNLOADING:
                    if (mShowNotify) {
                        long currentSize = msg.arg1;
                        long totalSize = msg.arg2;
                        int present = (int) ((currentSize * 100) / (double) totalSize);
                        if (present - mLastPresent >= 1) {
                            showDownloadingNotification(present);
                        }
                        mLastPresent = present;
                    }
                    break;
                case KEY_DOWNLOAD_FAIL:
                    disposeDownloadFile(false, (String) msg.obj);
                    break;
                case KEY_DOWNLOAD_SUCCESS:
                    disposeDownloadFile(true, (String) msg.obj);
                    break;
            }
        }
    };

    public DownloadService() {
        this("DownloadService");
    }

    public DownloadService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String downloadUrl = intent.getStringExtra(EXTRA_DOWNLOAD_URL);
        mShowNotify = intent.getBooleanExtra(EXTRA_SHOW_NOTIFY, false);
        String downloadPath = intent.getStringExtra(EXTRA_DOWNLOAD_PATH);
        mDownloadAppName = intent.getStringExtra(EXTRA_DOWNLOAD_APP_NAME);
        if (mShowNotify) {
            showDownloadingNotification(0);
        }
        startDownload(downloadUrl, downloadPath);
    }

    private void showDownloadingNotification(int progress) {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (pendingIntent == null) {
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
        }
        if (mBuilder == null) {
            int iconId = android.R.drawable.stat_sys_download;
            String tickerText = "开始下载";
            mBuilder = new NotificationCompat.Builder(this).setSmallIcon(iconId).setTicker(tickerText).setContentIntent(pendingIntent).setContentTitle(mDownloadAppName);
        }
        mBuilder.setContentText("正在下载 " + progress + "%");
        mBuilder.setProgress(100, progress, false);
        mManager.notify(KEY_NOTIFICATION_ID, mBuilder.build());
    }

    private void showDownloadNotification(boolean status) {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (pendingIntent == null) {
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
        }
        if (mBuilder == null) {
            int iconId = android.R.drawable.stat_sys_download;
            String tickerText = "开始下载";
            mBuilder = new NotificationCompat.Builder(this).setSmallIcon(iconId).setTicker(tickerText).setContentIntent(pendingIntent).setContentTitle(mDownloadAppName);
        }
        mBuilder.setProgress(0, 0, false);
        mBuilder.setAutoCancel(true);
        if (status) {
            mBuilder.setContentText("下载成功");
        } else {
            mBuilder.setContentText("下载失败");
        }
        mManager.notify(KEY_NOTIFICATION_ID, mBuilder.build());
    }

    private void hideNotification() {
        if (mManager != null) {
            mManager.cancel(KEY_NOTIFICATION_ID);
        }
    }

    private void startDownload(String downloadUrl, String downloadPath) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl).openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                mHandler.sendMessage(mHandler.obtainMessage(KEY_DOWNLOAD_FAIL, downloadPath));
                return;
            }
            int totalSize = connection.getContentLength();
            BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
            FileOutputStream fos = new FileOutputStream(new File(downloadPath), false);
            int length;
            int currentSize = 0;
            byte[] buf = new byte[1024];
            while (((length = bis.read(buf)) != -1)) {
                currentSize += length;
                fos.write(buf, 0, length);
                mHandler.sendMessage(mHandler.obtainMessage(KEY_DOWNLOADING, currentSize, totalSize, null));
            }
            if (currentSize == totalSize) {
                mHandler.sendMessage(mHandler.obtainMessage(KEY_DOWNLOAD_SUCCESS, downloadPath));
            } else {
                mHandler.sendMessage(mHandler.obtainMessage(KEY_DOWNLOAD_FAIL, downloadPath));
            }
            connection.disconnect();
            fos.close();
            bis.close();
        } catch (IOException e) {
            Log.e(TAG, "startDownload: io exception=" + e.getMessage(), e);
            mHandler.sendEmptyMessage(KEY_DOWNLOAD_FAIL);
        }
    }

    private void disposeDownloadFile(boolean success, String path) {
        showDownloadNotification(success);
        if (path == null || path.equals("")) {
            return;
        }
        File downloadFile = new File(path);
        if (!downloadFile.exists()) {
            return;
        }
        if (success) {
            if (path.endsWith(".apk")) {
                installApk(downloadFile);
            }
        } else {
            deleteCacheFile(path);
        }
    }

    private void installApk(File downloadFile) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(downloadFile), getMIMEType(downloadFile));
        startActivity(intent);
    }

    private String getMIMEType(File downloadFile) {
        String type;
        String name = downloadFile.getName();
        String end = name.substring(name.lastIndexOf(".") + 1, name.length());
        if (end.equals("apk")) {
            type = "application/vnd.android.package-archive";
        } else {
            type = "*/*";
        }
        return type;
    }

    private void deleteCacheFile(String path) {
        File downloadFile = new File(path);
        if (!downloadFile.exists()) {
            return;
        }
        boolean result = downloadFile.delete();
        if (!result) {
            Log.e(TAG, "delete download file:" + path + " fail");
        }
    }

}

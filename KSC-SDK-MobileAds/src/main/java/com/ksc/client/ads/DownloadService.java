package com.ksc.client.ads;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

/**
 * Created by Alamusi on 2016/8/30.
 */
public class DownloadService extends Service {

    public static final String EXTRA_DOWNLOAD_URL = "downloadUrl";
    public static final String EXTRA_DOWNLOAD_PATH = "downloadPath";
    public static final String EXTRA_DOWNLOAD_APP_NAME = "downloadAppName";
    private static final String TAG = DownloadService.class.getSimpleName();

    private static final int TIMEOUT = 20 * 1000;
    private static final int KEY_DOWNLOADING = 0;
    private static final int KEY_DOWNLOAD_FAIL = 1;
    private static final int KEY_DOWNLOAD_SUCCESS = 2;

    private DownloadManager mDownloadManager;
    private String mDownloadPath;
    private String mDownloadUrl;
    private long mDownloadId;
    private CompleteReceiver mCompleteReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: new receiver");
        mCompleteReceiver = new CompleteReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: register receiver");
        registerReceiver(mCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        mDownloadUrl = intent.getStringExtra(EXTRA_DOWNLOAD_URL);
        mDownloadPath = intent.getStringExtra(EXTRA_DOWNLOAD_PATH);
        Log.d(TAG, "onHandleIntent: downloadUrl:" + mDownloadUrl);
        Log.d(TAG, "onHandleIntent: downloadPath:" + mDownloadPath);
        if (TextUtils.isEmpty(mDownloadUrl) || TextUtils.isEmpty(mDownloadPath)) {
            Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show();
            stopSelf();
        } else {
            startDownloadWithSystem(mDownloadUrl);
        }
        return 0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: unregister receiver");
        unregisterReceiver(mCompleteReceiver);
    }

    private void startDownloadWithSystem(String downloadUrl) {
        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(downloadUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("游戏包");
        File file = new File(mDownloadPath);
        request.setDestinationUri(Uri.fromFile(file));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        }
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setVisibleInDownloadsUi(true);
        mDownloadId = mDownloadManager.enqueue(request);
        Log.d(TAG, "startDownloadWithSystem: downloadId [" + mDownloadId + "]");
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

    class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            Log.d(TAG, "onReceive: " + completeDownloadId);
            if (completeDownloadId == mDownloadId) {
                File downloadFile = new File(mDownloadPath);
                installApk(downloadFile);
                stopSelf();
            }
        }
    }

}

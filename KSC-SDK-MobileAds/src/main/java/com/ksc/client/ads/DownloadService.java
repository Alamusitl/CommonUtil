package com.ksc.client.ads;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
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

    private Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");
    private String mDownloadPath;
    private String mDownloadUrl;
    private long mDownloadId;
    private DownloadManager mDownloadManager;
    private CompleteReceiver mCompleteReceiver;
    private DownloadChangeObserver mDownloadObserver;

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
        mDownloadObserver = new DownloadChangeObserver(null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: register receiver");
        registerReceiver(mCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        getContentResolver().registerContentObserver(CONTENT_URI, true, mDownloadObserver);
        mDownloadUrl = intent.getStringExtra(EXTRA_DOWNLOAD_URL);
        mDownloadPath = intent.getStringExtra(EXTRA_DOWNLOAD_PATH);
        Log.d(TAG, "onHandleIntent: downloadUrl:" + mDownloadUrl);
        Log.d(TAG, "onHandleIntent: downloadPath:" + mDownloadPath);
        if (TextUtils.isEmpty(mDownloadUrl) || TextUtils.isEmpty(mDownloadPath)) {
            Toast.makeText(this, "下载失败，参数错误", Toast.LENGTH_SHORT).show();
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
        getContentResolver().unregisterContentObserver(mDownloadObserver);
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

    private void queryDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(mDownloadId);
        Cursor cursor = mDownloadManager.query(query);
        if (cursor != null && cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            int reasonId = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
            int titleId = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE);
            int fileSizeId = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
            int bytesId = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
            String title = cursor.getString(titleId);
            int fileSize = cursor.getInt(fileSizeId);
            int bytesDL = cursor.getInt(bytesId);
            int reason = cursor.getInt(reasonId);
            Log.v(TAG, "queryDownloadStatus: title=" + title + ",size=" + fileSize + ",reason=" + reason + ",bytesDL=" + bytesDL);

            switch (status) {
                case DownloadManager.STATUS_FAILED:
                    Log.d(TAG, "queryDownloadStatus: STATUS_FAILED [" + mDownloadId + "]");
                    if (mDownloadManager != null) {
                        mDownloadManager.remove(mDownloadId);
                    }
                    break;
                case DownloadManager.STATUS_PAUSED:
                    Log.d(TAG, "queryDownloadStatus: STATUS_PAUSED");
                    break;
                case DownloadManager.STATUS_PENDING:
                    Log.d(TAG, "queryDownloadStatus: STATUS_PENDING");
                    break;
                case DownloadManager.STATUS_RUNNING:
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    Log.d(TAG, "queryDownloadStatus: STATUS_SUCCESSFUL");
                    break;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
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

    class DownloadChangeObserver extends ContentObserver {

        public DownloadChangeObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            queryDownloadStatus();
        }

    }

}

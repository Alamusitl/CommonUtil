package com.ksc.client.update;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.ksc.client.toolbox.HttpError;
import com.ksc.client.toolbox.HttpErrorListener;
import com.ksc.client.toolbox.HttpListener;
import com.ksc.client.toolbox.HttpRequestManager;
import com.ksc.client.toolbox.HttpRequestParam;
import com.ksc.client.toolbox.HttpRequestThread;
import com.ksc.client.toolbox.HttpResponse;
import com.ksc.client.update.entity.KSCUpdateInfo;
import com.ksc.client.update.view.KSCUpdateView;
import com.ksc.client.util.KSCLog;
import com.ksc.client.util.KSCMD5Utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Alamusi on 2016/7/28.
 */
public class KSCUpdateService extends Service {

    private static KSCUpdateInfo mCurUpdateInfo;
    private static List<KSCUpdateInfo> mUpdateList;
    private static int mTotalSize = 0;
    private static Context mContext;
    private static String mTmpPath;
    private static HttpRequestThread mCurThread;
    private static int mTime = 0;

    private Handler mServiceHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KSCUpdate.EVENT_UPDATE_START:
                    processSingleRequest();
                    break;
                case KSCUpdate.EVENT_UPDATE_OVER:
                    KSCUpdate.mHandler.sendMessage(KSCUpdate.mHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_OVER));
                    stopSelf();
                    break;
                case KSCUpdate.EVENT_UPDATE_DOWNLOAD_START:
                    KSCUpdateView.showUpdateProgress(mContext, mCurUpdateInfo.getIsForce(), mServiceHandler);
                    startDownloadFile();
                    break;
                case KSCUpdate.EVENT_UPDATE_DOWNLOAD_BACKGROUND:
                    KSCLog.d("start download background!");
                    break;
                case KSCUpdate.EVENT_UPDATE_DOWNLOADING:
                    break;
                case KSCUpdate.EVENT_UPDATE_DOWNLOAD_FAIL:
                    KSCLog.d("download file fail!");
                    retry();
                    break;
                case KSCUpdate.EVENT_UPDATE_DOWNLOAD_STOP:
                    KSCLog.d("stop download update file!");
                    clearCache();
                    moveToNext();
                    break;
                case KSCUpdate.EVENT_UPDATE_DOWNLOAD_FINISH:
                    processDownloadFile(mTmpPath);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_START:
                    KSCLog.d("start download file!");
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_CURRENT:
                    int currentSize = (int) msg.obj;
                    KSCUpdateView.updateProgress(currentSize, mTotalSize);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_FAIL:
                    KSCLog.d("download file fail!");
                    KSCUpdateView.updateProcessHide();
                    retry();
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_TOTAL:
                    mTotalSize = (int) msg.obj;
                    KSCUpdateView.updateProgress(0, mTotalSize);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_DONE:
                    KSCLog.d("download update file success!");
                    KSCUpdateView.updateProcessHide();
                    break;
                default:
                    break;
            }
        }
    };

    private void processSingleRequest() {
        if (mUpdateList == null) {
            return;
        }
        if (mUpdateList.size() > 0) {
            mCurUpdateInfo = mUpdateList.get(0);
            mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_DOWNLOAD_START));
        } else {
            mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_OVER));
        }
    }

    private void moveToNext() {
        if (mUpdateList.size() > 0) {
            mUpdateList.remove(0);
        }
        if (mUpdateList.size() == 0) {
            mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_OVER));
        } else {
            mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_START));
        }
    }

    private void startDownloadFile() {
        final HttpRequestParam requestParam = new HttpRequestParam(mCurUpdateInfo.getUrl());
        mCurThread = HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(HttpResponse response) {
                KSCLog.d("download file success, code: " + response.getCode() + ", msg: " + response.getBodyString());
                mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_DOWNLOAD_FINISH));
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                KSCLog.e("download file fail, error info: " + (error.httpResponse != null ? error.httpResponse.getBodyString() : null), error);
                mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_DOWNLOAD_FAIL));
            }
        }, mServiceHandler);
        mTmpPath = mCurThread.getDownloadPath();
    }

    private void processDownloadFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            String md5 = KSCMD5Utils.getFileMD5(file);
            if (!mCurUpdateInfo.getMD5().equals(md5)) {
                KSCUpdate.mCheckUpdateCallBack.onError("version=" + mCurUpdateInfo.getVersion() + " update fail, md5 error, md5=" + md5);
                moveToNext();
                return;
            }
        }
        if (mCurUpdateInfo.getType().equals("Full")) {
            KSCUpdate.installApk(mContext, new File(filePath));
        } else if (mCurUpdateInfo.getType().equals("Patch")) {
            PatchClient.loadLib();
            String path = Environment.getExternalStorageDirectory() + File.separator + "new.apk";
            int result = PatchClient.applyPatch(mContext.getApplicationContext(), path, filePath);
            if (result == 0) {
                clearCache();
                File newApk = new File(path);
                if (newApk.exists() && newApk.isFile()) {
                    KSCUpdate.installApk(mContext, newApk);
                }
            } else {
                KSCUpdate.mCheckUpdateCallBack.onError("version=" + mCurUpdateInfo.getVersion() + " update fail, patch file fail");
            }
        } else {
            try {
                KSCUpdate.unZipResourceFile(new File(filePath), KSCUpdate.mUpdateResourcePath);
            } catch (IOException e) {
                KSCLog.e("unzip Resource File fail, IO Exception : " + e.getMessage(), e);
                KSCUpdate.mCheckUpdateCallBack.onError("version=" + mCurUpdateInfo.getVersion() + " update fail, unzip resource fail, " + e.getMessage());
            }
        }
        moveToNext();
    }

    private void clearCache() {
        if (mTmpPath == null) {
            return;
        }
        File file = new File(mTmpPath);
        if (!file.exists()) {
            return;
        }
        boolean delete = file.delete();
        if (delete) {
            KSCLog.d("delete tmp file success!");
        } else {
            KSCLog.d("delete tmp file fail!");
        }
    }

    private void retry() {
        clearCache();
        if (mTime < 3) {
            mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_DOWNLOAD_START));
            mTime++;
        } else {
            mTime = 0;
            moveToNext();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = getApplicationContext();
        if (intent.hasExtra("data")) {
            mUpdateList = intent.getParcelableArrayListExtra("data");
            mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_START));
        }
        return super.onStartCommand(intent, flags, startId);
    }

}

package com.ksc.client.update;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.WindowManager;

import com.ksc.client.toolbox.HttpError;
import com.ksc.client.toolbox.HttpErrorListener;
import com.ksc.client.toolbox.HttpListener;
import com.ksc.client.toolbox.HttpRequestManager;
import com.ksc.client.toolbox.HttpRequestParam;
import com.ksc.client.toolbox.HttpRequestThread;
import com.ksc.client.toolbox.HttpResponse;
import com.ksc.client.update.entity.KSCUpdateInfo;
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
    private static ProgressDialog mProgressDialog;
    private String mUpdateResourcePath = null;
    private Handler mServiceHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KSCUpdate.EVENT_UPDATE_START:
                    processSingleRequest();
                    break;
                case KSCUpdate.EVENT_UPDATE_OVER:
                    KSCLog.d("all update over!");
                    if (KSCUpdate.mHandler != null) {
                        KSCUpdate.mHandler.sendMessage(KSCUpdate.mHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_OVER));
                    }
                    stopSelf();
                    break;
                case KSCUpdate.EVENT_UPDATE_DOWNLOAD_START:
                    KSCLog.d("update file download start, url:" + mCurUpdateInfo.getUrl());
                    showUpdateProgress(mContext, mCurUpdateInfo.getIsForce());
                    startDownloadFile();
                    break;
                case KSCUpdate.EVENT_UPDATE_DOWNLOAD_BACKGROUND:
                    KSCLog.d("start download background!");
                    break;
                case KSCUpdate.EVENT_UPDATE_DOWNLOADING:
                    KSCLog.d("update file downloading!");
                    break;
                case KSCUpdate.EVENT_UPDATE_DOWNLOAD_FAIL:
                    KSCLog.d("download file fail!");
                    retry();
                    break;
                case KSCUpdate.EVENT_UPDATE_DOWNLOAD_STOP:
                    KSCLog.d("stop download update file!");
                    if (mCurThread != null && mCurThread.isAlive()) {
                        mCurThread.interrupt();
                    }
                    clearCache();
                    moveToNext();
                    break;
                case KSCUpdate.EVENT_UPDATE_DOWNLOAD_FINISH:
                    KSCLog.d("update file download success, file:" + mTmpPath);
                    processDownloadFile(mTmpPath);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_START:
                    KSCLog.d("start download file!");
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_CURRENT:
                    int currentSize = (int) msg.obj;
                    updateProgress(currentSize, mTotalSize);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_FAIL:
                    KSCLog.d("download file fail!");
                    updateProcessHide();
                    retry();
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_TOTAL:
                    mTotalSize = (int) msg.obj;
                    KSCLog.d("update file size = " + mTotalSize);
                    updateProgress(0, mTotalSize);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_DONE:
                    KSCLog.d("download update file success!");
                    updateProcessHide();
                    break;
                default:
                    break;
            }
        }
    };

    private void processSingleRequest() {
        if (mUpdateList == null) {
            stopSelf();
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
                KSCLog.w("update file " + filePath + ", md5 error!");
                KSCUpdate.mUpdateCallBack.onError(mCurUpdateInfo.getVersion());
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
                KSCLog.w("patch file " + filePath + ", patch fail, result=" + result);
                KSCUpdate.mUpdateCallBack.onError(mCurUpdateInfo.getVersion());
            }
        } else {
            try {
                KSCUpdate.unZipResourceFile(new File(filePath), mUpdateResourcePath);
            } catch (IOException e) {
                KSCLog.e("unzip Resource File fail, IO Exception : " + e.getMessage(), e);
                KSCUpdate.mUpdateCallBack.onError("version=" + mCurUpdateInfo.getVersion() + " update fail, unzip resource fail, " + e.getMessage());
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

    private void showUpdateProgress(final Context context, final boolean isForceUpdate) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.cancel();
                    mProgressDialog = null;
                }
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                mProgressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "后台下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mProgressDialog.cancel();
                        mServiceHandler.handleMessage(mServiceHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_DOWNLOAD_BACKGROUND));
                    }
                });
                if (!isForceUpdate) {
                    mProgressDialog.setCancelable(true);
                    mProgressDialog.setCanceledOnTouchOutside(true);
                    mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "取消更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mProgressDialog.cancel();
                            mServiceHandler.handleMessage(mServiceHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_DOWNLOAD_STOP));
                        }
                    });
                } else {
                    mProgressDialog.setCancelable(false);
                }
                mProgressDialog.show();
            }
        });
    }

    private void updateProcessHide() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.cancel();
                }
            }
        });
    }

    private void updateProgress(final int current, final int total) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                int present = (int) ((current * 100) / (double) total);
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.setMax(total);
                    mProgressDialog.setProgress(current);
                    if (current == total) {
                        mProgressDialog.cancel();
                    }
                }
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = this;
        if (intent.hasExtra("data") && intent.hasExtra("resourcePath")) {
            mUpdateList = intent.getParcelableArrayListExtra("data");
            mUpdateResourcePath = intent.getStringExtra("resourcePath");
            mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_START));
        }
        return super.onStartCommand(intent, flags, startId);
    }

}

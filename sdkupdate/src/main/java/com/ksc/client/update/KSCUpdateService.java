package com.ksc.client.update;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Alamusi on 2016/7/28.
 */
public class KSCUpdateService extends Service {

    private KSCUpdateInfo mCurUpdateInfo;
    private List<KSCUpdateInfo> mUpdateList;
    private Context mContext;
    private String mTmpPath;
    private HttpRequestThread mCurThread;
    private HandlerThread mHandlerThread;
    private int mTotalSize = 0;
    private int mTime = 0;
    private ProgressDialog mProgressDialog;
    private String mUpdateResourcePath = null;
    private boolean mIsUseSelf = false;
    private Handler mThreadHandler;
    private Handler mServiceHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KSCUpdateStatusCode.EVENT_UPDATE_START:
                    processSingleRequest();
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_OVER:
                    KSCLog.d("all update over!");
                    callback(KSCUpdateStatusCode.EVENT_UPDATE_OVER, 0, 0, null);
                    if (!mIsUseSelf) {
                        hideUpdateProcess();
                    }
                    stopSelf();
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_ERROR:
                    callback(KSCUpdateStatusCode.EVENT_UPDATE_ERROR, msg.arg1, msg.arg2, (String) msg.obj);
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_FINISH:
                    callback(KSCUpdateStatusCode.EVENT_UPDATE_FINISH, 0, 0, (String) msg.obj);
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_START:
                    KSCLog.d("update file download start, url:" + mCurUpdateInfo.getUrl());
                    startDownloadFile();
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_BACKGROUND:
                    KSCLog.d("start download background!");
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOADING:
                    KSCLog.d("update file downloading!");
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_FAIL:
                    KSCLog.d("download file fail!");
                    callback(KSCUpdateStatusCode.EVENT_UPDATE_ERROR, 0, 0, mCurUpdateInfo.getId());
                    retry();
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_STOP:
                    KSCLog.d("stop download update file!");
                    if (mCurThread != null && !mCurThread.isInterrupted() && mCurThread.isAlive()) {
                        mCurThread.interrupt();
                    }
                    callback(KSCUpdateStatusCode.EVENT_UPDATE_CANCEL, 0, 0, mCurUpdateInfo.getId());
                    clearCache();
                    moveToNext();
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_FINISH:
                    KSCLog.d("update file download success, file:" + mTmpPath);
                    processDownloadFile(mTmpPath);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_START:
                    KSCLog.d("start download file!");
                    if (!mIsUseSelf) {
                        showUpdateProgress(mContext, mCurUpdateInfo.getIsForce());
                    }
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_CURRENT:
                    int currentSize = (int) msg.obj;
                    if (!mIsUseSelf) {
                        refreshUpdateProgress(currentSize, mTotalSize);
                    }
                    int present = (int) ((currentSize * 100) / (double) mTotalSize);
                    callback(KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOADING, currentSize, present, mCurUpdateInfo.getId());
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_FAIL:
                    KSCLog.d("download file fail!");
                    if (!mIsUseSelf) {
                        hideUpdateProcess();
                    }
                    callback(KSCUpdateStatusCode.EVENT_UPDATE_ERROR, 0, 0, mCurUpdateInfo.getId());
                    retry();
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_TOTAL:
                    mTotalSize = (int) msg.obj;
                    KSCLog.d("update file size = " + mTotalSize);
                    if (!mIsUseSelf) {
                        refreshUpdateProgress(0, mTotalSize);
                    }
                    callback(KSCUpdateStatusCode.EVENT_UPDATE_START, mTotalSize, 0, mCurUpdateInfo.getId());
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_DONE:
                    KSCLog.d("download update file success!");
                    if (!mIsUseSelf) {
                        hideUpdateProcess();
                    }
                    break;
                case 0:
                    if (mUpdateList.size() == 0) {
                        callback(KSCUpdateStatusCode.EVENT_UPDATE_OVER, 0, 0, "");
                    }
                default:
                    break;
            }
        }
    };

    private void processSingleRequest() {
        if (mUpdateList == null) {
            KSCLog.e("update list is null, update over!");
            mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_OVER));
            return;
        }
        if (mUpdateList.size() != 0) {
            mCurUpdateInfo = mUpdateList.get(0);
            mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_START));
        }
    }

    private void moveToNext() {
        if (mUpdateList.size() > 0) {
            mUpdateList.remove(0);
        }
        if (mUpdateList.size() != 0) {
            mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_START));
        }
    }

    private void startDownloadFile() {
        final HttpRequestParam requestParam = new HttpRequestParam(mCurUpdateInfo.getUrl());
        mCurThread = HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(HttpResponse response) {
                KSCLog.d("download file success, code: " + response.getCode() + ", msg: " + response.getBodyString());
                mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_FINISH));
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                KSCLog.e("download file fail, error info: " + (error.httpResponse != null ? error.httpResponse.getBodyString() : null), error);
                mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_FAIL));
            }
        }, mServiceHandler);
        mTmpPath = mCurThread.getDownloadPath();
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
            mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_START));
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
                        mServiceHandler.handleMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_BACKGROUND));
                    }
                });
                if (!isForceUpdate) {
                    mProgressDialog.setCancelable(true);
                    mProgressDialog.setCanceledOnTouchOutside(true);
                    mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "取消更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mProgressDialog.cancel();
                            mServiceHandler.handleMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_STOP));
                        }
                    });
                } else {
                    mProgressDialog.setCancelable(false);
                }
                mProgressDialog.show();
            }
        });
    }

    private void hideUpdateProcess() {
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

    private void refreshUpdateProgress(final int current, final int total) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
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

    private void processDownloadFile(final String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            String md5 = KSCMD5Utils.getFileMD5(file);
            if (!mCurUpdateInfo.getMD5().equals(md5)) {
                KSCLog.w("update file " + filePath + ", md5 error!");
                mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_ERROR, 0, 0, mCurUpdateInfo.getId()));
                moveToNext();
                return;
            }
        }
        if (mCurUpdateInfo.getType().equals("Full")) {
            mThreadHandler.post(installApk(mContext, file, mCurUpdateInfo.getId()));
        } else if (mCurUpdateInfo.getType().equals("Patch")) {
            PatchClient.loadLib();
            mThreadHandler.post(patchClient(mContext, filePath, mCurUpdateInfo.getId()));
        } else {
            mThreadHandler.post(unZipResourceFile(file, mUpdateResourcePath, mCurUpdateInfo.getId()));
        }
        moveToNext();
    }

    /**
     * 安装下载 & Patch的APK
     *
     * @param context 上下文
     * @param file    目标APK文件
     */
    private Runnable installApk(final Context context, final File file, final String name) {
        return new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                context.startActivity(intent);
                mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_FINISH, name));
                mServiceHandler.sendMessage(mServiceHandler.obtainMessage(0));
            }
        };
    }

    private Runnable patchClient(final Context context, final String filePath, final String name) {
        return new Runnable() {
            @Override
            public void run() {
                String path = Environment.getExternalStorageDirectory() + File.separator + "new.apk";
                int result = PatchClient.applyPatch(mContext.getApplicationContext(), path, filePath);
                if (result == 0) {
                    clearCache();
                    File newApk = new File(path);
                    if (newApk.exists() && newApk.isFile()) {
                        installApk(context, newApk, name);
                    } else {
                        mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_ERROR, 0, 0, mCurUpdateInfo.getId()));
                    }
                } else {
                    KSCLog.w("patch file " + filePath + ", patch fail, result=" + result);
                    mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_ERROR, 0, 0, mCurUpdateInfo.getId()));
                }
                mServiceHandler.sendMessage(mServiceHandler.obtainMessage(0));
            }
        };
    }

    /**
     * 解压更新资源
     *
     * @param srcFile 更新文件
     * @param destDir 解压的目录
     * @throws IOException
     */
    private Runnable unZipResourceFile(final File srcFile, final String destDir, final String name) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    ZipFile zipFile = new ZipFile(srcFile);
                    Enumeration zList = zipFile.entries();
                    ZipEntry zipEntry;
                    final int BUF_SIZE = 1024;
                    byte[] buf = new byte[BUF_SIZE];
                    while (zList.hasMoreElements()) {
                        zipEntry = (ZipEntry) zList.nextElement();
                        KSCLog.d("unzipFile: " + "zipEntry.name = " + zipEntry.getName());
                        if (zipEntry.isDirectory()) {
                            String tDir = destDir + zipEntry.getName();
                            tDir = tDir.trim();
                            tDir = new String(tDir.getBytes("8859_1"), "GB2312");
                            KSCLog.d("unzipFile: " + "destDir : " + tDir);
                            File file = new File(tDir);
                            if (!file.exists()) {
                                file.mkdirs();
                                continue;
                            }
                        }
                        OutputStream os = new BufferedOutputStream(new FileOutputStream(getRealFileName(destDir, zipEntry.getName())));
                        InputStream is = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                        int readLength;
                        while ((readLength = is.read(buf, 0, BUF_SIZE)) != -1) {
                            os.write(buf, 0, readLength);
                        }
                        is.close();
                        os.close();
                    }
                    zipFile.close();
                    if (srcFile.exists()) {
                        srcFile.delete();
                    }
                    KSCLog.d("unzipFile: " + srcFile + " is finish.");
                    mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_FINISH, name));
                    mServiceHandler.sendMessage(mServiceHandler.obtainMessage(0));
                } catch (Exception e) {
                    KSCLog.e("unzip Resource File fail, IO Exception : " + e.getMessage(), e);
                    mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_ERROR, 0, 0, mCurUpdateInfo.getId()));
                }
            }
        };
    }

    /**
     * 获得解压的路径
     *
     * @param baseDir     基础路径
     * @param absFileName 文件的名称
     * @return 解压文件的具体路径加文件名
     * @throws UnsupportedEncodingException
     */
    private File getRealFileName(String baseDir, String absFileName) throws UnsupportedEncodingException {
        String[] dirs = absFileName.split("/");
        String lastDir = baseDir;
        if (dirs.length > 1) {
            for (int i = 0; i < dirs.length - 1; i++) {
                lastDir += (dirs[i] + File.separator);
                File dir = new File(lastDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            }
            File ret = new File(lastDir, dirs[dirs.length - 1]);
            KSCLog.d("unzipFile: " + "ret :" + ret);
            return ret;
        } else {
            return new File(baseDir, absFileName);
        }
    }

    private void callback(int status, int arg1, int arg2, String msg) {
        Handler handler = KSCUpdate.getInstance().mHandler;
        if (handler != null) {
            Message message = handler.obtainMessage();
            message.what = status;
            message.arg1 = arg1;
            message.arg2 = arg2;
            message.obj = (msg == null ? "" : msg);
            handler.sendMessage(message);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = this;
        mUpdateList = intent.getParcelableArrayListExtra("data");
        mUpdateResourcePath = intent.getStringExtra("resourcePath");
        mIsUseSelf = intent.getBooleanExtra("useSelf", false);
        mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_START));
        mHandlerThread = new HandlerThread("processUpdateFile");
        mHandlerThread.start();
        mThreadHandler = new Handler(mHandlerThread.getLooper());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
        mHandlerThread.quit();
        mThreadHandler = null;
        mServiceHandler = null;
    }
}

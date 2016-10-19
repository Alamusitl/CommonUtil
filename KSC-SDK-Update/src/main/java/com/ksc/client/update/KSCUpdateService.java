package com.ksc.client.update;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.WindowManager;

import com.afk.client.toolbox.HttpError;
import com.afk.client.toolbox.HttpErrorListener;
import com.afk.client.toolbox.HttpListener;
import com.afk.client.toolbox.HttpRequestManager;
import com.afk.client.toolbox.HttpRequestParam;
import com.afk.client.toolbox.HttpRequestRunnable;
import com.afk.client.toolbox.HttpResponse;
import com.afk.client.util.AppUtils;
import com.afk.client.util.Logger;
import com.afk.client.util.MD5Utils;
import com.afk.client.util.StorageUtils;
import com.ksc.client.update.entity.KSCUpdateInfo;

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

    private static final java.lang.String TAG = KSCUpdateService.class.getSimpleName();

    static {
        System.loadLibrary("PatchClient");
    }

    private KSCUpdateInfo mCurUpdateInfo;
    private List<KSCUpdateInfo> mUpdateList;
    private Context mContext;
    private HttpRequestRunnable mCurThread;
    private HandlerThread mHandlerThread;
    private int mTime = 0;
    private int mLastPresent = 0;
    private ProgressDialog mProgressDialog;
    private String mUpdateResourcePath = null;
    private String mDownloadPath = null;
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
                    Logger.d("all update over!");
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
                    Logger.d("update file download start, url:" + mCurUpdateInfo.getUrl());
                    startDownloadFile();
                    if (!mIsUseSelf) {
                        refreshUpdateProgress(0, mCurUpdateInfo.getSize());
                    }
                    callback(KSCUpdateStatusCode.EVENT_UPDATE_START, mCurUpdateInfo.getSize(), 0, mCurUpdateInfo.getName());
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_BACKGROUND:
                    Logger.d("start download background!");
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOADING:
                    Logger.d("update file downloading!");
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_FAIL:
                    Logger.d("download file fail!");
                    callback(KSCUpdateStatusCode.EVENT_UPDATE_ERROR, 0, 0, mCurUpdateInfo.getName());
                    retry();
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_STOP:
                    Logger.d("stop download update file!");
                    if (mCurThread != null && !mCurThread.isRunning()) {
                        mCurThread.stopThread();
                    }
                    callback(KSCUpdateStatusCode.EVENT_UPDATE_CANCEL, 0, 0, mCurUpdateInfo.getName());
                    clearCache();
                    moveToNext();
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_FINISH:
                    Logger.d("update file download success, file:" + mDownloadPath);
                    processDownloadFile(mDownloadPath);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_START:
                    Logger.d("start download file!");
                    if (!mIsUseSelf) {
                        showUpdateProgress(mContext, mCurUpdateInfo.getIsForce());
                    }
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_CURRENT:
                    int currentSize = (int) msg.obj;
                    if (!mIsUseSelf) {
                        refreshUpdateProgress(currentSize, mCurUpdateInfo.getSize());
                    }
                    int present = (int) ((currentSize * 100) / (double) mCurUpdateInfo.getSize());
                    if (present - mLastPresent >= 1) {
                        callback(KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOADING, currentSize, present, mCurUpdateInfo.getName());
                    }
                    mLastPresent = present;
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_FAIL:
                    Logger.d("download file fail!");
                    if (!mIsUseSelf) {
                        hideUpdateProcess();
                    }
                    callback(KSCUpdateStatusCode.EVENT_UPDATE_ERROR, 0, 0, mCurUpdateInfo.getName());
                    retry();
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_DONE:
                    Logger.d("download update file success!");
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
            Logger.e(TAG, "update list is null, update over!");
            mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_OVER));
            return;
        }
        if (mUpdateList.size() != 0) {
            mCurUpdateInfo = mUpdateList.get(0);
            mDownloadPath = StorageUtils.getDownloadDir(mCurUpdateInfo.getSize());
            if (mDownloadPath == null) {
                Logger.e(TAG, "Download File Failed, Download Space is null");
                moveToNext();
                return;
            }
            String name = mCurUpdateInfo.getUrl().substring(mCurUpdateInfo.getUrl().lastIndexOf("/"), mCurUpdateInfo.getUrl().length());
            mDownloadPath = mDownloadPath + name;
            File file = new File(mDownloadPath);
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    if (!file.getParentFile().mkdirs()) {
                        Logger.e(TAG, "mkdirs dir " + file.getParentFile().getName() + " failed!");
                        moveToNext();
                        return;
                    }
                }
                try {
                    if (!file.createNewFile()) {
                        Logger.e(TAG, "create file " + file.getName() + " failed!");
                        moveToNext();
                        return;
                    }
                } catch (IOException e) {
                    Logger.e("create file " + file.getName() + " failed!", e);
                    moveToNext();
                    return;
                }
                mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_START));
            } else {
                processDownloadFile(mDownloadPath);
            }
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
        HttpRequestParam requestParam = new HttpRequestParam(mCurUpdateInfo.getUrl());
        requestParam.setDownloadPath(mDownloadPath);
        mCurThread = HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(HttpResponse response) {
                Logger.d(TAG, "download file success, code: " + response.getCode() + ", msg: " + response.getBodyString());
                mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_FINISH));
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                Logger.e("download file fail, error info: " + (error.httpResponse != null ? error.httpResponse.getBodyString() : null), error);
                mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_FAIL));
            }
        }, mServiceHandler);
    }

    private void clearCache() {
        if (mDownloadPath == null) {
            return;
        }
        File file = new File(mDownloadPath);
        if (!file.exists()) {
            return;
        }
        boolean delete = file.delete();
        if (delete) {
            Logger.d(TAG, "delete tmp file success!");
        } else {
            Logger.d(TAG, "delete tmp file fail!");
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
                mProgressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, KSCUpdateKeyCode.KEY_PROCESS_TEXT_BACKGROUND, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mProgressDialog.cancel();
                        mServiceHandler.handleMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOAD_BACKGROUND));
                    }
                });
                if (!isForceUpdate) {
                    mProgressDialog.setCancelable(true);
                    mProgressDialog.setCanceledOnTouchOutside(true);
                    mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, KSCUpdateKeyCode.KEY_PROCESS_TEXT_CANCEL, new DialogInterface.OnClickListener() {
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
            String md5 = MD5Utils.getFileMD5(file);
            if (!mCurUpdateInfo.getMD5().equals(md5)) {
                Logger.w(TAG, "update file " + filePath + ", md5 error!");
                mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_ERROR, 0, 0, mCurUpdateInfo.getName()));
                moveToNext();
                return;
            }
        }
        if (mCurUpdateInfo.getType().equals(KSCUpdateKeyCode.KEY_FILE_TYPE_FULL)) {
            installApk(mContext, file, mCurUpdateInfo.getName());
        } else if (mCurUpdateInfo.getType().equals(KSCUpdateKeyCode.KEY_FILE_TYPE_DIFF)) {
            patchClient(mContext, filePath, mCurUpdateInfo.getName());
        } else {
            mThreadHandler.post(unZipResourceFile(file, mUpdateResourcePath, mCurUpdateInfo.getName()));
        }
        moveToNext();
    }

    /**
     * 安装下载 & Patch的APK
     *
     * @param context 上下文
     * @param file    目标APK文件
     */
    private void installApk(final Context context, final File file, final String name) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
        mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_FINISH, name));
        mServiceHandler.sendMessage(mServiceHandler.obtainMessage(0));
    }

    /**
     * 合并老的APK和差异文件
     *
     * @param context  context
     * @param filePath 差异文件的路径
     * @param name     更新包的名称
     */
    private void patchClient(final Context context, final String filePath, final String name) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                String path = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + AppUtils.getPackageName(context) + ".apk";
                int result = PatchClient.applyPatch(mContext.getApplicationContext(), path, filePath);
                if (result == 0) {
                    clearCache();
                    File newApk = new File(path);
                    if (newApk.exists() && newApk.isFile()) {
                        installApk(context, newApk, name);
                    } else {
                        mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_ERROR, 0, 0, mCurUpdateInfo.getName()));
                    }
                } else {
                    Logger.w(TAG, "patch file " + filePath + ", patch fail, result=" + result);
                    mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_ERROR, 0, 0, mCurUpdateInfo.getName()));
                }
            }
        });
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
                    File outDir = new File(destDir);
                    if (!outDir.exists()) {
                        outDir.mkdirs();
                    }
                    while (zList.hasMoreElements()) {
                        zipEntry = (ZipEntry) zList.nextElement();
                        Logger.d(TAG, "unzipFile: " + "zipEntry.name = " + zipEntry.getName());
                        if (zipEntry.isDirectory()) {
                            String tDir = destDir + zipEntry.getName();
                            tDir = tDir.trim();
                            tDir = new String(tDir.getBytes("8859_1"), "GB2312");
                            Logger.d(TAG, "unzipFile: " + "destDir : " + tDir);
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
                    Logger.d(TAG, "unzipFile: " + srcFile + " is finish.");
                    mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_FINISH, name));
                    mServiceHandler.sendMessage(mServiceHandler.obtainMessage(0));
                } catch (Exception e) {
                    Logger.e("unzip Resource File fail, IO Exception : " + e.getMessage(), e);
                    mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_ERROR, 0, 0, mCurUpdateInfo.getName()));
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
        if (dirs.length > 0) {
            for (int i = 0; i < dirs.length - 1; i++) {
                lastDir += (dirs[i] + File.separator);
                File dir = new File(lastDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            }
            File ret = new File(lastDir, dirs[dirs.length - 1]);
            Logger.d(TAG, "unzipFile: " + "ret :" + ret);
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
    public void onCreate() {
        super.onCreate();
        mHandlerThread = new HandlerThread("processUpdateFile");
        mHandlerThread.start();
        mThreadHandler = new Handler(mHandlerThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = this;
        Bundle bundle = intent.getBundleExtra(KSCUpdateKeyCode.KEY_BUNDLE);
        mUpdateList = bundle.getParcelableArrayList(KSCUpdateKeyCode.KEY_LIST);
        mUpdateResourcePath = intent.getStringExtra(KSCUpdateKeyCode.KEY_RESOURCE_PATH);
        mIsUseSelf = intent.getBooleanExtra(KSCUpdateKeyCode.KEY_USE_SELF, false);
        mServiceHandler.sendMessage(mServiceHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_START));
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

package com.ksc.client.update;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.ksc.client.toolbox.HttpError;
import com.ksc.client.toolbox.HttpErrorListener;
import com.ksc.client.toolbox.HttpListener;
import com.ksc.client.toolbox.HttpRequestManager;
import com.ksc.client.toolbox.HttpRequestParam;
import com.ksc.client.toolbox.HttpResponse;
import com.ksc.client.update.callback.CheckUpdateCallBack;
import com.ksc.client.update.entity.KSCUpdateInfo;
import com.ksc.client.update.view.KSCUpdateView;
import com.ksc.client.util.KSCLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Alamusi on 2016/6/22.
 */
public class KSCUpdate {

    protected static final int EVENT_UPDATE_HAS_UPDATE = 1000;
    protected static final int EVENT_UPDATE_NO_UPDATE = 1001;
    protected static final int EVENT_UPDATE_CHECK_FAIL = 1002;
    protected static final int EVENT_UPDATE_START = 1003;
    protected static final int EVENT_UPDATE_CANCEL = 1004;
    protected static final int EVENT_UPDATE_BACKGROUND = 1005;
    protected static final int EVENT_UPDATE_FINISH = 1006;
    protected static final int EVENT_UPDATE_OVER = 1007;
    protected static final int EVENT_UPDATE_FAIL = 1008;
    protected static final int EVENT_UPDATE_DOWNLOADING = 1009;
    protected static final int EVENT_UPDATE_STOP_DOWNLOAD = 1010;

    protected static String mUpdateResourcePath = null;
    protected static CheckUpdateCallBack mCheckUpdateCallBack = null;
    protected static boolean mIsUseCPSelf = false;
    private static ArrayList<KSCUpdateInfo> mReadUpdateList;
    private static Context mContext;

    protected static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_UPDATE_HAS_UPDATE:
                    ArrayList<KSCUpdateInfo> updateInfoList = parseUpdateResponse((String) msg.obj);
                    mCheckUpdateCallBack.onSuccess(true, updateInfoList);
                    if (!mIsUseCPSelf) {
                        showSDKUpdateDialog(mContext, updateInfoList);
                    }
                    break;
                case EVENT_UPDATE_NO_UPDATE:
                    mCheckUpdateCallBack.onSuccess(false, null);
                    break;
                case EVENT_UPDATE_CHECK_FAIL:
                    mCheckUpdateCallBack.onError((String) msg.obj);
                    break;
                case KSCUpdate.EVENT_UPDATE_START:
                    startUpdate(mContext, mReadUpdateList);
                    break;
                case KSCUpdate.EVENT_UPDATE_CANCEL:
                    break;
                case KSCUpdate.EVENT_UPDATE_BACKGROUND:
                    break;
                case KSCUpdate.EVENT_UPDATE_FINISH:
                    break;
                case KSCUpdate.EVENT_UPDATE_OVER:
                    mContext.stopService(new Intent(mContext, KSCUpdateService.class));
                    break;
                case KSCUpdate.EVENT_UPDATE_FAIL:
                    KSCUpdate.mCheckUpdateCallBack.onError((String) msg.obj);
                    break;
                case KSCUpdate.EVENT_UPDATE_DOWNLOADING:
                    break;
                case KSCUpdate.EVENT_UPDATE_STOP_DOWNLOAD:
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_START:
//                    KSCUpdateView.showUpdateProgress(mContext, mCurUpdateInfo.getIsForce(), mHandler);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_TOTAL:
//                    mTotalSize = Long.parseLong((String) msg.obj);
                    KSCUpdateView.updateProgress(0, 100);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_CURRENT:
                    long downloadSize = Long.parseLong((String) msg.obj);
//                    int present = (int) ((downloadSize * 100) / (double) mTotalSize);
//                    KSCUpdateView.updateProgress(present, 100);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_DONE:
                    KSCUpdateView.updateProcessHide();
                    String filePath = (String) msg.obj;
//                    processDownloadFile(filePath);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_FAIL:
                    KSCUpdateView.updateProcessHide();
//                    clearCache();
                    break;
            }
        }
    };

    public static void checkUpdate(final Context context, String appId, String channel, String version, String updateResourcePath, boolean useSelf, CheckUpdateCallBack checkUpdateCallBack) {
        mContext = context;
        if (TextUtils.isEmpty(updateResourcePath)) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File file = context.getExternalFilesDir(null);
                if (file != null) {
                    updateResourcePath = file.getAbsolutePath();
                } else {
                    updateResourcePath = context.getFilesDir().getAbsolutePath();
                }
            } else {
                updateResourcePath = context.getFilesDir().getAbsolutePath();
            }
        }
        mUpdateResourcePath = updateResourcePath;
        mIsUseCPSelf = useSelf;
        mCheckUpdateCallBack = checkUpdateCallBack;
        String platform = "android";
        String url = "?Action=getClientVerlist&Version=" + version + "&app=" + appId + "&versionid=" + version + "&channel=" + channel + "&platform=" + platform;
        final HttpRequestParam requestParam = new HttpRequestParam(url);
        HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(HttpResponse response) {
                if (response.getCode() == HttpURLConnection.HTTP_OK) {
                    String versionList = response.getBodyString();
                    if (versionList.equals("{}")) {
                        mHandler.sendMessage(mHandler.obtainMessage(EVENT_UPDATE_NO_UPDATE));
                    } else {
                        mHandler.sendMessage(mHandler.obtainMessage(EVENT_UPDATE_HAS_UPDATE, versionList));
                    }
                } else {
                    KSCLog.e("check update error, code : " + response.getCode() + " , message : " + response.getBodyString());
                    mHandler.sendMessage(mHandler.obtainMessage(EVENT_UPDATE_CHECK_FAIL, response.getBodyString()));
                }
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                KSCLog.e("get update info fail," + (error.httpResponse != null ? error.httpResponse.getCode() : 0) + ":" + (error.httpResponse != null ? error.httpResponse.getBodyString() : null), error);
                mHandler.sendMessage(mHandler.obtainMessage(EVENT_UPDATE_CHECK_FAIL, error.getMessage()));
            }
        });
    }

    public static void startUpdate(Context context, ArrayList<KSCUpdateInfo> updateList) {
        Intent intent = new Intent(context, KSCUpdateService.class);
        intent.putParcelableArrayListExtra("data", updateList);
        context.startService(intent);
    }

    /**
     * 解析服务器返回的更新信息
     *
     * @param allInfo 更新信息
     * @return 解析完的数据列表
     */
    public static ArrayList<KSCUpdateInfo> parseUpdateResponse(String allInfo) {
        ArrayList<KSCUpdateInfo> updateInfoList = new ArrayList<>();
        try {
            JSONObject data = new JSONObject(allInfo);
            JSONArray list = data.optJSONArray("verlist");
            for (int i = 0; i < list.length(); i++) {
                JSONObject info = list.getJSONObject(i);
                String id = info.optString("id");
                String version = info.optString("version");
                String url = info.optString("url");
                String type = info.optString("package");
                String update = info.optString("update");
                String suffix = info.optString("compress");
                String msg = info.optString("comment");
                String md5 = info.optString("Md5");
                boolean isForce = true;
                if (update.equals("force")) {
                    isForce = true;
                } else if (update.equals("free")) {
                    isForce = false;
                }
                KSCUpdateInfo updateInfo = new KSCUpdateInfo(id, version, url, type, isForce, suffix, msg, md5);
                updateInfoList.add(updateInfo);
            }
        } catch (JSONException e) {
            KSCLog.e("can not format String to JSON, String : [" + allInfo + "]", e);
        }
        return updateInfoList;
    }

    private static void showSDKUpdateDialog(Context context, ArrayList<KSCUpdateInfo> updateInfoList) {
        KSCUpdateInfo updateInfo = updateInfoList.get(0);
        mReadUpdateList = new ArrayList<>();
        if (updateInfo.getType().equals("full") || updateInfo.getType().equals("patch")) {
            mReadUpdateList.add(updateInfo);
            KSCUpdateView.showUpdatePrompt(context, updateInfo.getIsForce(), updateInfo.getUpdateMsg(), mHandler);
        } else {
            KSCUpdateView.showUpdatePrompt(context, updateInfoList, mHandler);
        }
    }

    /**
     * 安装下载 & Patch的APK
     *
     * @param context 上下文
     * @param file    目标APK文件
     */
    protected static void installApk(Context context, File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 解压更新资源
     *
     * @param srcFile 更新文件
     * @param destDir 解压的目录
     * @throws IOException
     */
    protected static void unZipResourceFile(File srcFile, String destDir) throws IOException {
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
        KSCLog.d("unzipFile: " + srcFile + " is finish.");
    }

    /**
     * 获得解压的路径
     *
     * @param baseDir     基础路径
     * @param absFileName 文件的名称
     * @return
     * @throws UnsupportedEncodingException
     */
    private static File getRealFileName(String baseDir, String absFileName) throws UnsupportedEncodingException {
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

}

package com.ksc.client.core.update;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.ksc.client.core.base.callback.UpdateCallBack;
import com.ksc.client.core.config.KSCSDKInfo;
import com.ksc.client.toolbox.HttpError;
import com.ksc.client.toolbox.HttpErrorListener;
import com.ksc.client.toolbox.HttpListener;
import com.ksc.client.toolbox.HttpRequestManager;
import com.ksc.client.toolbox.HttpRequestParam;
import com.ksc.client.toolbox.HttpResponse;
import com.ksc.client.util.KSCLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Alamusi on 2016/6/22.
 */
public class KSCUpdate {

    public static final int EVENT_UPDATE_START = 1000;
    public static final int EVENT_UPDATE_CANCEL = 1001;
    public static final int EVENT_UPDATE_BACKGROUND = 1002;
    public static final int EVENT_UPDATE_FINISH = 1003;
    public static final int EVENT_UPDATE_OVER = 1004;
    public static final int EVENT_UPDATE_FAIL = 1005;
    public static final int EVENT_UPDATE_DOWNLOADING = 1006;
    public static final int EVENT_UPDATE_STOP_DOWNLOAD = 1007;
    public static final int EVENT_UPDATE_NO_UPDATE = 1008;
    private static final int BUF_SIZE = 1024;
    public static String mUpdateResourcePath = null;
    public static UpdateCallBack mUpdateCallBack = null;
    public static boolean mIsUseCPSelf = false;

    public static void checkUpdate(final Context context, String updateResourcePath, boolean useSelf, UpdateCallBack updateCallBack) {
        mUpdateResourcePath = updateResourcePath;
        mIsUseCPSelf = useSelf;
        mUpdateCallBack = updateCallBack;
        String channel = KSCSDKInfo.getChannelId();
        String appId = KSCSDKInfo.getAppId();
        String number = KSCSDKInfo.getBuildVersion();
        String platform = "android";
        String url = "?Action=getClientVerlist&Version=" + number + "&app=" + appId + "&versionid=" + number + "&channel=" + channel + "&platform=" + platform;
        final HttpRequestParam requestParam = new HttpRequestParam(url);
        HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(HttpResponse response) {
                if (response.getCode() == HttpURLConnection.HTTP_OK) {
                    String versionList = response.getBodyString();
                    if (versionList.equals("")) {
                        KSCUpdateService.mHandler.sendMessage(KSCUpdateService.mHandler.obtainMessage(EVENT_UPDATE_NO_UPDATE));
                    } else {
                        context.startService(new Intent(context, KSCUpdateService.class));
                    }
                } else {
                    KSCLog.e("check update error, code : " + response.getCode() + " , message : " + response.getBodyString());
                    KSCUpdateService.mHandler.sendMessage(KSCUpdateService.mHandler.obtainMessage(EVENT_UPDATE_FAIL, response.getBodyString()));
                }
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                KSCLog.e("get update info fail," + (error.httpResponse != null ? error.httpResponse.getCode() : 0) + ":" + (error.httpResponse != null ? error.httpResponse.getBodyString() : null), error);
                KSCUpdateService.mHandler.sendMessage(KSCUpdateService.mHandler.obtainMessage(EVENT_UPDATE_FAIL, error.getMessage()));
            }
        });
    }

    /**
     * 安装下载，Patch的APK
     *
     * @param context
     * @param file
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

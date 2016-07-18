package com.ksc.client.core.update;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.ksc.client.core.config.KSCSDKInfo;
import com.ksc.client.toolbox.HttpError;
import com.ksc.client.toolbox.HttpErrorListener;
import com.ksc.client.toolbox.HttpListener;
import com.ksc.client.toolbox.HttpRequestManager;
import com.ksc.client.toolbox.HttpRequestParam;
import com.ksc.client.toolbox.HttpResponse;
import com.ksc.client.util.KSCLog;

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
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Alamusi on 2016/6/22.
 */
public class KSCUpdate {

    public static final String UPDATE_TYPE_RESOURCE = "resource";
    public static final String UPDATE_TYPR_PATCH = "patch";
    public static final String UPDATE_TYPE_APK = "apk";
    private static final int BUF_SIZE = 1024;

    public static void checkUpdate() {
        String channel = KSCSDKInfo.getChannelId();
        String appid = KSCSDKInfo.getAppId();
        String number = KSCSDKInfo.getBuildVersion();
        String url = "";
        final HttpRequestParam requestParam = new HttpRequestParam(url);
        HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(HttpResponse response) {
                if (response.getCode() == HttpURLConnection.HTTP_OK) {
                    processUpdateRequest(response.getBodyString());
                } else {
                    KSCLog.e("get update response error, code : " + response.getCode() + " , message : " + response.getBodyString());
                }
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                KSCLog.e("get update info fail," + error.httpResponse.getCode() + ":" + error.httpResponse.getBodyString(), error);
            }
        });
    }

    private static void processUpdateRequest(String bodyString) {
        try {
            JSONObject data = new JSONObject(bodyString);
        } catch (JSONException e) {
            KSCLog.e("can not format String to JSON, String : [" + bodyString + "]", e);
        }
    }

    public static void downloadUpdateFile(String url, String type, String sha1) {

    }

    private static void installApk(Context context, File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    private static void unZipResourceFile(File srcFile, String destDir) throws IOException {
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

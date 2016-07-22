package com.ksc.client.core.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.ksc.client.core.config.KSCSDKInfo;
import com.ksc.client.core.update.entity.KSCUpdateInfo;
import com.ksc.client.toolbox.HttpError;
import com.ksc.client.toolbox.HttpErrorListener;
import com.ksc.client.toolbox.HttpListener;
import com.ksc.client.toolbox.HttpRequestManager;
import com.ksc.client.toolbox.HttpRequestParam;
import com.ksc.client.toolbox.HttpResponse;
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
import java.util.List;
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
    private static ProgressDialog mDialog;
    private static long mTotalSize = 0;
    private static long mDownloadSize = 0;
    private static Activity mActivity;
    private static List<KSCUpdateInfo> mUpdateList = new ArrayList<>();
    private static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HttpRequestManager.DOWNLOAD_FILE_START:
                    mDialog.show();
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_TOTAL:
                    mTotalSize = Long.parseLong((String) msg.obj);
                    mDialog.setMax(100);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_CURRENT:
                    mDownloadSize = Long.parseLong((String) msg.obj);
                    int present = (int) ((mDownloadSize * 100) / (double) mTotalSize);
                    mDialog.setProgress(present);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_DONE:
                    mDialog.cancel();
                    String filePath = (String) msg.obj;
                    for (KSCUpdateInfo info : mUpdateList) {
                        if (info.getType().equals("Full")) {
                            installApk(mActivity, new File(filePath));
                        } else if (info.getType().equals("Patch")) {
                            PatchClient.loadLib();
                            PatchClient.applyPatch(mActivity.getApplicationContext(), Environment.getExternalStorageDirectory() + File.separator + "new.apk", filePath);
                        } else {
                            try {
                                unZipResourceFile(new File(filePath), mActivity.getFilesDir().getAbsolutePath());
                            } catch (IOException e) {
                                KSCLog.e("unzip Resource File fail, IO Exception : " + e.getMessage(), e);
                            }
                        }
                    }

                    break;
                case HttpRequestManager.DOWNLOAD_FILE_FAIL:
                    mDialog.cancel();
                    break;
            }
        }
    };

    public static void checkUpdate(Activity activity) {
        mActivity = activity;
        String channel = KSCSDKInfo.getChannelId();
        String appid = KSCSDKInfo.getAppId();
        String number = KSCSDKInfo.getBuildVersion();
        String platform = "Android";
        String url = "";
        final HttpRequestParam requestParam = new HttpRequestParam(url);
        HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(HttpResponse response) {
                if (response.getCode() == HttpURLConnection.HTTP_OK) {
                    processUpdateResponse(response.getBodyString());
                } else if (response.getCode() == 406) {
                    KSCLog.e("check update error, msg : " + response.getBodyString());
                } else {
                    KSCLog.e("get update response error, code : " + response.getCode() + " , message : " + response.getBodyString());
                }
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                KSCLog.e("get update info fail," + (error.httpResponse != null ? error.httpResponse.getCode() : 0) + ":" + (error.httpResponse != null ? error.httpResponse.getBodyString() : null), error);
            }
        });
    }

    private static void processUpdateResponse(String bodyString) {
        if (bodyString.equals("")) {
            KSCLog.d("client version is last version");
            return;
        }
        try {
            JSONObject data = new JSONObject(bodyString);
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
                KSCUpdateInfo updateInfo = new KSCUpdateInfo(id, version, url, type, update, suffix, msg, md5);
                mUpdateList.add(updateInfo);
            }
            for (KSCUpdateInfo info : mUpdateList) {
                showUpdateMsg(mActivity, info.getUpdateMsg());
            }
        } catch (JSONException e) {
            KSCLog.e("can not format String to JSON, String : [" + bodyString + "]", e);
        }
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

    public static void setProcessDialog(ProgressDialog dialog) {
        mDialog = dialog;
    }

    private static void showUpdateMsg(final Activity activity, String msg) {
        final AlertDialog alertDialog = new AlertDialog.Builder(activity).setTitle("更新提示").setMessage(msg).create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startDownloadFile(activity);
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.cancel();
            }
        });
        alertDialog.show();
    }

    private static void startDownloadFile(Activity activity) {
        showProcessDialog(activity);
        final HttpRequestParam requestParam = new HttpRequestParam("");
        HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(HttpResponse response) {
                KSCLog.d("download file success, code: " + response.getCode() + ", msg: " + response.getBodyString());
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                KSCLog.e("download file fail, error info: " + (error.httpResponse != null ? error.httpResponse.getBodyString() : null), error);
            }
        }, mHandler);
    }

    private static void showProcessDialog(Activity activity) {
        mDialog = new ProgressDialog(activity);
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }

}

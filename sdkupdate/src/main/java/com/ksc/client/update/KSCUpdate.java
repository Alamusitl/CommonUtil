package com.ksc.client.update;

import android.app.Activity;
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
import com.ksc.client.update.callback.UpdateCallBack;
import com.ksc.client.update.entity.KSCUpdateInfo;
import com.ksc.client.update.view.KSCUpdateDialogActivity;
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

    public static final int EVENT_UPDATE_START = 1003;// 开始更新
    public static final int EVENT_UPDATE_CANCEL = 1004;// 取消更新
    public static final int EVENT_UPDATE_DOWNLOAD_START = 1005;// 开始下载
    public static final int EVENT_UPDATE_DOWNLOAD_BACKGROUND = 1006;// 后台下载
    public static final int EVENT_UPDATE_DOWNLOADING = 1007;// 正在下载
    public static final int EVENT_UPDATE_DOWNLOAD_FAIL = 1008;// 下载失败
    public static final int EVENT_UPDATE_DOWNLOAD_FINISH = 1009;// 下载完成
    public static final int EVENT_UPDATE_DOWNLOAD_STOP = 1010;// 停止下载
    public static final int EVENT_UPDATE_OVER = 1011;// 更新完成
    private static final int EVENT_UPDATE_HAS_UPDATE = 1000;// 检查有更新
    private static final int EVENT_UPDATE_NO_UPDATE = 1001;// 检查无更新
    private static final int EVENT_UPDATE_CHECK_FAIL = 1002;// 检查失败
    private static final int REQUEST_CODE = 10000;
    protected static UpdateCallBack mUpdateCallBack = null;
    private static String mUpdateResourcePath = null;
    private static boolean mIsUseCPSelf = false;
    private static CheckUpdateCallBack mCheckUpdateCallBack = null;
    private static ArrayList<KSCUpdateInfo> mReadUpdateList;
    private static Activity mActivity;

    protected static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_UPDATE_HAS_UPDATE:
                    ArrayList<KSCUpdateInfo> updateInfoList = parseUpdateResponse((String) msg.obj);
                    mCheckUpdateCallBack.onSuccess(true, updateInfoList);
                    if (!mIsUseCPSelf) {
                        showSDKUpdateDialog(mActivity, updateInfoList);
                    }
                    break;
                case EVENT_UPDATE_NO_UPDATE:
                    mCheckUpdateCallBack.onSuccess(false, null);
                    break;
                case EVENT_UPDATE_CHECK_FAIL:
                    mCheckUpdateCallBack.onError((String) msg.obj);
                    break;
                case KSCUpdate.EVENT_UPDATE_START:
                    startUpdate(mActivity, mReadUpdateList, mUpdateCallBack);
                    break;
                case KSCUpdate.EVENT_UPDATE_CANCEL:
                    break;
                case KSCUpdate.EVENT_UPDATE_OVER:
                    if (mUpdateCallBack != null) {
                        mUpdateCallBack.onFinishUpdate();
                    }
                    break;
            }
        }
    };

    public static void checkUpdate(final Activity activity, String appId, String channel, String version, String updateResourcePath, boolean useSelf, CheckUpdateCallBack checkUpdateCallBack) {
        mActivity = activity;
        if (TextUtils.isEmpty(updateResourcePath)) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File file = activity.getExternalFilesDir(null);
                if (file != null) {
                    updateResourcePath = file.getAbsolutePath();
                } else {
                    updateResourcePath = activity.getFilesDir().getAbsolutePath();
                }
            } else {
                updateResourcePath = activity.getFilesDir().getAbsolutePath();
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
                try {
                    InputStream is = activity.getAssets().open("versionList.json");
                    byte[] buffer = new byte[1024];
                    StringBuilder builder = new StringBuilder();
                    while (is.read(buffer, 0, 1024) != -1) {
                        String tmp = new String(buffer);
                        builder.append(tmp);
                    }
                    KSCLog.d(builder.toString());
                    String versionList = builder.toString();
                    mHandler.sendMessage(mHandler.obtainMessage(EVENT_UPDATE_HAS_UPDATE, versionList));
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                KSCLog.e("get update info fail," + (error.httpResponse != null ? error.httpResponse.getCode() : 0) + ":" + (error.httpResponse != null ? error.httpResponse.getBodyString() : null), error);
//                mHandler.sendMessage(mHandler.obtainMessage(EVENT_UPDATE_CHECK_FAIL, error.getMessage()));
            }
        });
    }

    public static void startUpdate(Activity activity, ArrayList<KSCUpdateInfo> updateList, UpdateCallBack updateCallBack) {
        if (updateCallBack == null) {
            updateCallBack = new UpdateCallBack() {
                @Override
                public void onStartUpdate() {

                }

                @Override
                public void onProcess(int present) {

                }

                @Override
                public void onError(String version) {

                }

                @Override
                public void onFinishUpdate() {

                }
            };
        }
        mUpdateCallBack = updateCallBack;
        Intent intent = new Intent(activity, KSCUpdateService.class);
        intent.putParcelableArrayListExtra("data", updateList);
        intent.putExtra("resourcePath", mUpdateResourcePath);
        activity.startService(intent);
        mUpdateCallBack.onStartUpdate();
    }

    /**
     * 解析服务器返回的更新信息，序列化
     *
     * @param allInfo 更新信息
     * @return 解析完的数据列表
     */
    private static ArrayList<KSCUpdateInfo> parseUpdateResponse(String allInfo) {
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

    /**
     * 显示更新提示Dialog
     *
     * @param activity       当前的Activity
     * @param updateInfoList 更新列表
     */
    private static void showSDKUpdateDialog(Activity activity, ArrayList<KSCUpdateInfo> updateInfoList) {
        KSCUpdateInfo updateInfo = updateInfoList.get(0);
        Intent intent = new Intent(activity, KSCUpdateDialogActivity.class);
        intent.putExtra("updatePrompt", true);
        if (updateInfo.getType().equals("full") || updateInfo.getType().equals("patch")) {
            ArrayList<KSCUpdateInfo> tmpList = new ArrayList<>();
            tmpList.add(updateInfo);
            intent.putParcelableArrayListExtra("updateList", tmpList);
        } else {
            intent.putParcelableArrayListExtra("updateList", updateInfoList);
        }
        activity.startActivityForResult(intent, REQUEST_CODE);
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

    public static void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        KSCLog.d("KSCUpdate onActivityResult begin called!");
        if (requestCode == REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_CANCELED:
                    KSCLog.d("user cancel update!");
                    mHandler.sendMessage(mHandler.obtainMessage(EVENT_UPDATE_CANCEL));
                    break;
                case Activity.RESULT_OK:
                    if (data.hasExtra("updateList")) {
                        mReadUpdateList = data.getParcelableArrayListExtra("updateList");
                        mHandler.sendMessage(mHandler.obtainMessage(EVENT_UPDATE_START));
                    }
                    break;
            }
        }
        KSCLog.d("KSCUpdate onActivityResult end called!");
    }

}

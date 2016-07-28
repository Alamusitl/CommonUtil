package com.ksc.client.core.update;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

import com.ksc.client.core.update.entity.KSCUpdateInfo;
import com.ksc.client.core.update.view.KSCUpdateView;
import com.ksc.client.toolbox.HttpError;
import com.ksc.client.toolbox.HttpErrorListener;
import com.ksc.client.toolbox.HttpListener;
import com.ksc.client.toolbox.HttpRequestManager;
import com.ksc.client.toolbox.HttpRequestParam;
import com.ksc.client.toolbox.HttpResponse;
import com.ksc.client.util.KSCLog;
import com.ksc.client.util.KSCMD5Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alamusi on 2016/7/28.
 */
public class KSCUpdateService extends Service {

    private static KSCUpdateInfo mCurUpdateInfo;
    private static List<KSCUpdateInfo> mUpdateList = new ArrayList<>();
    private static long mTotalSize = 0;
    private static long mDownloadSize = 0;
    private static Context mContext;

    protected static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KSCUpdate.EVENT_UPDATE_START:
                    processSingleRequest();
                    break;
                case KSCUpdate.EVENT_UPDATE_CANCEL:
                    moveToNext();
                    break;
                case KSCUpdate.EVENT_UPDATE_BACKGROUND:
                    break;
                case KSCUpdate.EVENT_UPDATE_FINISH:
                    moveToNext();
                    break;
                case KSCUpdate.EVENT_UPDATE_OVER:
                    KSCUpdate.mUpdateCallBack.onOver();
                    break;
                case KSCUpdate.EVENT_UPDATE_FAIL:
                    KSCUpdate.mUpdateCallBack.onError((String) msg.obj);
                    break;
                case KSCUpdate.EVENT_UPDATE_DOWNLOADING:
                    startDownloadFile();
                    break;
                case KSCUpdate.EVENT_UPDATE_STOP_DOWNLOAD:
                    break;
                case KSCUpdate.EVENT_UPDATE_NO_UPDATE:
                    KSCUpdate.mUpdateCallBack.onSuccess(false, null, false, null);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_START:
                    KSCUpdateView.showUpdateProgress(mContext, mCurUpdateInfo.getIsForce(), mHandler);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_TOTAL:
                    mTotalSize = Long.parseLong((String) msg.obj);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_CURRENT:
                    mDownloadSize = Long.parseLong((String) msg.obj);
                    int present = (int) ((mDownloadSize * 100) / (double) mTotalSize);
                    KSCUpdateView.updateProgress(present, 100);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_DONE:
                    String filePath = (String) msg.obj;
                    processDownloadFile(filePath);
                    break;
                case HttpRequestManager.DOWNLOAD_FILE_FAIL:
                    break;
            }
        }
    };

    protected static void processUpdateResponse(String bodyString) {
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
                boolean isForce = true;
                if (update.equals("force")) {
                    isForce = true;
                } else if (update.equals("free")) {
                    isForce = false;
                }
                KSCUpdateInfo updateInfo = new KSCUpdateInfo(id, version, url, type, isForce, suffix, msg, md5);
                mUpdateList.add(updateInfo);
            }
            mHandler.sendMessage(mHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_START));
        } catch (JSONException e) {
            KSCLog.e("can not format String to JSON, String : [" + bodyString + "]", e);
        }
    }

    private static void processSingleRequest() {
        if (mUpdateList.size() > 0) {
            mCurUpdateInfo = mUpdateList.get(0);
        } else {
            mHandler.sendMessage(mHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_OVER));
            return;
        }
        int result = KSCUpdate.mUpdateCallBack.onSuccess(true, mCurUpdateInfo.getVersion(), mCurUpdateInfo.getIsForce(), mCurUpdateInfo.getUpdateMsg());
        if (!KSCUpdate.mIsUseCPSelf) {
            KSCUpdateView.showUpdatePrompt(mContext, mCurUpdateInfo.getIsForce(), mCurUpdateInfo.getUpdateMsg(), mHandler);
            return;
        }
        if (result == KSCUpdate.EVENT_UPDATE_START) {
            mHandler.sendMessage(mHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_START));
        } else if (result == KSCUpdate.EVENT_UPDATE_CANCEL) {
            mHandler.sendMessage(mHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_CANCEL));
        }
    }

    private static void moveToNext() {
        if (mUpdateList.size() > 0) {
            mUpdateList.remove(0);
        }
        if (mUpdateList.size() == 0) {
            mHandler.sendMessage(mHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_OVER));
        } else {
            mHandler.sendMessage(mHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_START));
        }
    }

    private static void startDownloadFile() {
        final HttpRequestParam requestParam = new HttpRequestParam(mCurUpdateInfo.getUrl());
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

    private static void processDownloadFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            String md5 = KSCMD5Utils.getFileMD5(file);
            if (!mCurUpdateInfo.getMD5().equals(md5)) {
                KSCUpdate.mUpdateCallBack.onError("version=" + mCurUpdateInfo.getVersion() + " update fail, md5 error, md5=" + md5);
                mHandler.sendMessage(mHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_START));
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
                File newApk = new File(path);
                if (newApk.exists() && newApk.isFile()) {
                    KSCUpdate.installApk(mContext, newApk);
                }
            } else {
                KSCUpdate.mUpdateCallBack.onError("version=" + mCurUpdateInfo.getVersion() + " update fail, patch file fail");
            }
        } else {
            try {
                KSCUpdate.unZipResourceFile(new File(filePath), KSCUpdate.mUpdateResourcePath);
            } catch (IOException e) {
                KSCLog.e("unzip Resource File fail, IO Exception : " + e.getMessage(), e);
                KSCUpdate.mUpdateCallBack.onError("version=" + mCurUpdateInfo.getVersion() + " update fail, unzip resource fail, " + e.getMessage());
            }
        }
        mHandler.sendMessage(mHandler.obtainMessage(KSCUpdate.EVENT_UPDATE_START));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = getApplicationContext();
        String data = intent.getStringExtra("data");
        processUpdateResponse(data);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

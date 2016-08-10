package com.ksc.client.update;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.ksc.client.util.KSCHelpUtils;
import com.ksc.client.util.KSCLog;
import com.ksc.client.util.KSCPackageUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alamusi on 2016/6/22.
 */
public class KSCUpdate {

    private static final String GET_VERSION_LIST_URL = "http://192.168.158.168:8000/update/getverlist/";

    private Activity mActivity;
    private CheckUpdateCallBack mCheckUpdateCallBack = null;
    private UpdateCallBack mUpdateCallBack = null;
    protected Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KSCUpdateStatusCode.EVENT_UPDATE_HAS_UPDATE:
                    ArrayList<KSCUpdateInfo> updateInfoList = parseUpdateResponse((String) msg.obj);
                    mCheckUpdateCallBack.onSuccess(true, updateInfoList);
                    if (msg.arg1 == 1) {
                        showSDKUpdateDialog(mActivity, updateInfoList);
                    }
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_NO_UPDATE:
                    mCheckUpdateCallBack.onSuccess(false, null);
                    release();
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_CHECK_FAIL:
                    mCheckUpdateCallBack.onError((String) msg.obj);
                    release();
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_START:
                    if (mUpdateCallBack != null) {
                        mUpdateCallBack.onUpdateStart((String) msg.obj, msg.arg1, KSCHelpUtils.changeToStr(msg.arg1));
                    }
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_CANCEL:
                    if (mUpdateCallBack != null) {
                        mUpdateCallBack.onUpdateCancel((String) msg.obj);
                    }
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_ERROR:
                    if (mUpdateCallBack != null) {
                        mUpdateCallBack.onUpdateError((String) msg.obj);
                    }
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_DOWNLOADING:
                    if (mUpdateCallBack != null) {
                        mUpdateCallBack.onUpdating((String) msg.obj, msg.arg1, KSCHelpUtils.changeToStr(msg.arg1), msg.arg2);
                    }
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_FINISH:
                    if (mUpdateCallBack != null) {
                        mUpdateCallBack.onUpdateFinish((String) msg.obj);
                    }
                    break;
                case KSCUpdateStatusCode.EVENT_UPDATE_OVER:
                    if (mUpdateCallBack != null) {
                        mUpdateCallBack.onUpdateOver();
                    }
                    release();
                    break;
            }
        }
    };

    public static KSCUpdate getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 检查更新
     *
     * @param activity            上下文
     * @param appId               AppId
     * @param channel             渠道ID
     * @param resourceVersion     资源版本号
     * @param useSelf             是否使用自己的更新视图
     * @param checkUpdateCallBack 检查更新回调
     */
    public void checkUpdate(final Activity activity, String appId, String channel, String resourceVersion, final boolean useSelf, CheckUpdateCallBack checkUpdateCallBack) {
        if (activity == null) {
            KSCLog.e("KSCUpdate check param activity is null, please check!");
            return;
        }
        if (TextUtils.isEmpty(appId)) {
            KSCLog.e("KSCUpdate check param appId is empty, please check!");
            return;
        }
        if (TextUtils.isEmpty(channel)) {
            KSCLog.e("KSCUpdate check param channel is empty, please check!");
            return;
        }
        if (TextUtils.isEmpty(resourceVersion)) {
            KSCLog.e("KSCUpdate check param resourceVersion is empty, please check!");
            return;
        }
        if (checkUpdateCallBack == null) {
            KSCLog.e("KSCUpdate check param checkUpdateCallBack is null, please check!");
            return;
        }
        mActivity = activity;
        mCheckUpdateCallBack = checkUpdateCallBack;
        String url = GET_VERSION_LIST_URL + "?" + "app_id=" + appId + "&full_id=" + KSCPackageUtils.getVersionCode(activity) + "&resource_id=" + resourceVersion + "&channel=" + channel + "&platform=android";
        url = "http://192.168.158.168:8000/update/getverlist/?app_id=9bc6200b-f708-44bc-949f-c06d48d67f65&full_id=0000100000&resource_id=0000100000&channel=d2e4325a-4ee4-4af0-b2a0-c2dae1262b1e&platform=android";
        final HttpRequestParam requestParam = new HttpRequestParam(url);
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Token 920553d7631d619284b7d126544453015fc6eb34");
        requestParam.setHeaders(headers);
        HttpRequestManager.execute(requestParam, new HttpListener() {
            @Override
            public void onResponse(HttpResponse response) {
                Message message = mHandler.obtainMessage();
                if (response.getCode() == HttpURLConnection.HTTP_OK) {
                    String versionList = response.getBodyString();
                    try {
                        JSONObject list = new JSONObject(versionList);
                        if (list.length() == 0) {
                            message.what = KSCUpdateStatusCode.EVENT_UPDATE_NO_UPDATE;
                        } else if (list.has("detail")) {
                            message.what = KSCUpdateStatusCode.EVENT_UPDATE_CHECK_FAIL;
                            message.obj = list.optString("detail");
                        } else {
                            message.what = KSCUpdateStatusCode.EVENT_UPDATE_HAS_UPDATE;
                            if (!useSelf) {
                                message.arg1 = 1;
                            } else {
                                message.arg1 = 0;
                            }
                            message.obj = versionList;
                        }
                    } catch (JSONException e) {
                        KSCLog.e("update response convert error, JSONException", e);
                        message.what = KSCUpdateStatusCode.EVENT_UPDATE_CHECK_FAIL;
                        message.obj = e.getMessage();
                    }
                } else {
                    KSCLog.e("check update error, code : " + response.getCode() + " , message : " + response.getBodyString());
                    message.what = KSCUpdateStatusCode.EVENT_UPDATE_CHECK_FAIL;
                    message.obj = response.getBodyString();
                }
                mHandler.sendMessage(message);
            }
        }, new HttpErrorListener() {
            @Override
            public void onErrorResponse(HttpError error) {
                KSCLog.e("get update info fail," + (error.httpResponse != null ? error.httpResponse.getCode() : 0) + ":" + (error.httpResponse != null ? error.httpResponse.getBodyString() : null), error);
                mHandler.sendMessage(mHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_CHECK_FAIL, error.getMessage()));
            }
        });
    }

    /**
     * 开始更新
     *
     * @param activity           上下文
     * @param useSelf            是否使用自己的更新视图
     * @param updateResourcePath 资源更新路径
     * @param updateList         需要更新的信息List
     * @param updateCallBack     更新回调
     */
    public void startUpdate(Activity activity, boolean useSelf, String updateResourcePath, ArrayList<KSCUpdateInfo> updateList, UpdateCallBack updateCallBack) {
        if (updateCallBack == null) {
            updateCallBack = new UpdateCallBack() {

                @Override
                public void onUpdateStart(String name, int totalSize, String Size) {

                }

                @Override
                public void onUpdating(String name, int currentSize, String Size, float present) {

                }

                @Override
                public void onUpdateError(String name) {

                }

                @Override
                public void onUpdateCancel(String name) {

                }

                @Override
                public void onUpdateFinish(String name) {

                }

                @Override
                public void onUpdateOver() {

                }
            };
        }
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
        mUpdateCallBack = updateCallBack;
        Intent intent = new Intent(activity, KSCUpdateService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(KSCUpdateKeyCode.KEY_LIST, updateList);
        intent.putExtra(KSCUpdateKeyCode.KEY_BUNDLE, bundle);
        intent.putExtra(KSCUpdateKeyCode.KEY_RESOURCE_PATH, updateResourcePath);
        intent.putExtra(KSCUpdateKeyCode.KEY_USE_SELF, useSelf);
        activity.startService(intent);
    }

    /**
     * 解析服务器返回的更新信息，序列化
     *
     * @param allInfo 更新信息
     * @return 解析完的数据列表
     */
    private ArrayList<KSCUpdateInfo> parseUpdateResponse(String allInfo) {
        ArrayList<KSCUpdateInfo> updateInfoList = new ArrayList<>();
        try {
            JSONObject data = new JSONObject(allInfo);
            JSONArray list = data.optJSONArray(KSCUpdateKeyCode.KEY_VERSION_LIST);
            for (int i = 0; i < list.length(); i++) {
                JSONObject info = list.getJSONObject(i);
                String name = info.optString(KSCUpdateKeyCode.KEY_VERSION_LIST_NAME);
                String version = info.optString(KSCUpdateKeyCode.KEY_VERSION_LIST_VERSION);
                String url = info.optString(KSCUpdateKeyCode.KEY_VERSION_LIST_URL);
                String type = info.optString(KSCUpdateKeyCode.KEY_VERSION_LIST_PACKAGE);
                String update = info.optString(KSCUpdateKeyCode.KEY_VERSION_LIST_TYPE);
                String msg = info.optString(KSCUpdateKeyCode.KEY_VERSION_LIST_COMMENT);
                int size = info.getInt(KSCUpdateKeyCode.KEY_VERSION_LIST_SIZE);
                String md5 = info.optString(KSCUpdateKeyCode.KEY_VERSION_LIST_MD5);
                boolean isForce = true;
                if (update.equals(KSCUpdateKeyCode.KEY_TYPE_FORCE)) {
                    isForce = true;
                } else if (update.equals(KSCUpdateKeyCode.KEY_TYPE_FREE)) {
                    isForce = false;
                }
                KSCUpdateInfo updateInfo = new KSCUpdateInfo(name, version, url, type, isForce, msg, size, md5);
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
    private void showSDKUpdateDialog(Activity activity, ArrayList<KSCUpdateInfo> updateInfoList) {
        Intent intent = new Intent(activity, KSCUpdateDialogActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(KSCUpdateKeyCode.KEY_LIST, updateInfoList);
        intent.putExtra(KSCUpdateKeyCode.KEY_BUNDLE, bundle);
        activity.startActivityForResult(intent, 10000);
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        KSCLog.d("KSCUpdate onActivityResult begin called!");
        if (requestCode == 10000) {
            switch (resultCode) {
                case Activity.RESULT_CANCELED:
                    KSCLog.d("user cancel update!");
                    mHandler.sendMessage(mHandler.obtainMessage(KSCUpdateStatusCode.EVENT_UPDATE_CANCEL, ""));
                    break;
                case Activity.RESULT_OK:
                    Bundle bundle = data.getBundleExtra(KSCUpdateKeyCode.KEY_BUNDLE);
                    bundle.setClassLoader(KSCUpdateInfo.class.getClassLoader());
                    ArrayList<KSCUpdateInfo> mReadUpdateList = bundle.getParcelableArrayList(KSCUpdateKeyCode.KEY_LIST);
                    File file = activity.getExternalFilesDir(null);
                    if (file == null || !file.exists()) {
                        file = activity.getFilesDir();
                    }
                    startUpdate(activity, false, file.getAbsolutePath(), mReadUpdateList, mUpdateCallBack);
                    break;
            }
        }
        KSCLog.d("KSCUpdate onActivityResult end called!");
    }

    private void release() {
        mActivity = null;
        mCheckUpdateCallBack = null;
    }

    private static class SingletonHolder {
        public static final KSCUpdate INSTANCE = new KSCUpdate();
    }

}

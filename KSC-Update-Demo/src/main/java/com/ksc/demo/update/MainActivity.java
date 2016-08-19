package com.ksc.demo.update;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ksc.client.update.KSCUpdate;
import com.ksc.client.update.callback.CheckUpdateCallBack;
import com.ksc.client.update.callback.UpdateCallBack;
import com.ksc.client.update.entity.KSCUpdateInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView mTvShowMsg;
    private CheckUpdateCallBack mCheckUpdateCallBack = null;
    private UpdateCallBack mUpdateCallBack = null;
    private ArrayList<KSCUpdateInfo> mList = null;
    private String resourceVersion = "001";
    private String resourcePath = null;

    private String appId = "9bc6200b-f708-44bc-949f-c06d48d67f65";
    private String appKey = "10000";
    private String channel = "d2e4325a-4ee4-4af0-b2a0-c2dae1262b1e";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
        File file = getExternalFilesDir(null);
        if (file == null || !file.exists()) {
            file = getFilesDir();
        }
        resourcePath = file.getAbsolutePath();
        loadData();
        Log.e("TAG", getApplicationInfo().sourceDir);
        decrypt();
    }

    private void initListener() {
        mCheckUpdateCallBack = new CheckUpdateCallBack() {
            @Override
            public void onError(String error) {
                Log.e(TAG, "check update error, error:" + error);
            }

            @Override
            public void onSuccess(boolean hasUpdate, ArrayList<KSCUpdateInfo> updateList) {
                Log.d(TAG, hasUpdate + ": size=" + (updateList == null ? 0 : updateList.size()));
                mList = updateList;
            }
        };
        mUpdateCallBack = new UpdateCallBack() {
            @Override
            public void onUpdateStart(String name, int totalSize, String Size) {
                Log.d(TAG, name + ":" + totalSize + ":" + Size);
            }

            @Override
            public void onUpdating(String name, int currentSize, String Size, float present) {
                Log.d(TAG, name + ":" + currentSize + ":" + Size + ":" + present);
            }

            @Override
            public void onUpdateError(String name) {
                Log.w(TAG, "update error name:" + name);
            }

            @Override
            public void onUpdateCancel(String name) {
                Log.w(TAG, "update cancel name:" + name);
            }

            @Override
            public void onUpdateFinish(String name) {
                Log.w(TAG, "update finish name:" + name);
            }

            @Override
            public void onUpdateOver() {
                Log.d(TAG, "update over");
            }
        };
    }

    private void initView() {
        mTvShowMsg = (TextView) findViewById(R.id.tv_showMsg);
        mTvShowMsg.setMovementMethod(new ScrollingMovementMethod());
        findViewById(R.id.btnCheckUpdate).setOnClickListener(this);
        findViewById(R.id.btnStartUpdate).setOnClickListener(this);
    }

    private void loadData() {
        File file = new File(resourcePath);
        File srcFile = null;
        if (file.exists() && file.isDirectory()) {
            File[] fileList = file.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.getName().equals("Info.json")) {
                    srcFile = aFileList;
                }
            }
        }
        if (srcFile == null) {
            readFile(null);
        } else {
            readFile(srcFile);
        }
    }

    private void readFile(File file) {
        try {
            InputStream is;
            if (file == null) {
                AssetManager manager = getAssets();
                String[] list = manager.list("");
                if (list != null && list.length == 0) {
                    return;
                }
                is = manager.open("Info.json");
            } else {
                is = new FileInputStream(file);
            }

            byte[] buffer = new byte[1024];
            StringBuilder builder = new StringBuilder();
            while (is.read(buffer, 0, 1024) > 0) {
                builder.append(new String(buffer));
            }
            String msg = builder.toString();
            builder.delete(0, msg.length());
            if (TextUtils.isEmpty(msg)) {
                Log.i(TAG, "loadData: msg is empty");
                return;
            }
            JSONArray jsonArray = new JSONArray(msg);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                String id = jsonObject.optString("id");
                String info = jsonObject.optString("msg");
                resourceVersion = info;
                builder.append(mTvShowMsg.getText());
                builder.append("id:");
                builder.append(id);
                builder.append("\n");
                builder.append("msg:");
                builder.append(info);
                builder.append("\n");
                mTvShowMsg.setText(builder.toString());
                builder.delete(0, builder.length());
            }
            is.close();
        } catch (IOException e) {
            Log.i(TAG, "loadData: IOException", e);
        } catch (JSONException e) {
            Log.e(TAG, "loadData: JSONException", e);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCheckUpdate:
                KSCUpdate.getInstance().checkUpdate(this, appId, channel, resourceVersion, false, mCheckUpdateCallBack);
                break;
            case R.id.btnStartUpdate:
                KSCUpdate.getInstance().startUpdate(this, false, resourcePath, mList, mUpdateCallBack);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        KSCUpdate.getInstance().onActivityResult(this, requestCode, resultCode, data);
    }

    private void decrypt() {

    }

}

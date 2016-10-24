package com.ksc.demo.util.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.afk.client.util.AppUtils;
import com.ksc.demo.util.R;

public class AppUtilActivity extends Activity implements View.OnClickListener {

    private TextView mAppName, mPackage, mVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_util);
        mAppName = (TextView) findViewById(R.id.id_tv_app_name);
        mPackage = (TextView) findViewById(R.id.id_tv_package);
        mVersion = (TextView) findViewById(R.id.id_tv_version);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_btn_app_name:
                mAppName.setText(AppUtils.getAppName(this));
                break;
            case R.id.id_btn_package:
                mPackage.setText(AppUtils.getPackageName(this));
                break;
            case R.id.id_btn_version:
                mVersion.setText(AppUtils.getVersionName(this) + ":" + AppUtils.getVersionCode(this));
                break;
        }
    }
}

package com.ksc.client.core.base.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.ksc.client.core.KSCSDK;

/**
 * SDK原生Activity，建议游戏继承此Activity
 * Created by Alamusi on 2016/6/21.
 */
public class KSCActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KSCSDK.getInstance().onCreate(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        KSCSDK.getInstance().onStart(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        KSCSDK.getInstance().onRestart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        KSCSDK.getInstance().onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        KSCSDK.getInstance().onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        KSCSDK.getInstance().onStop(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KSCSDK.getInstance().onDestroy(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        KSCSDK.getInstance().onNewIntent(this, intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        KSCSDK.getInstance().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        KSCSDK.getInstance().onBackPressed(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        KSCSDK.getInstance().onConfigurationChanged(this, newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        KSCSDK.getInstance().onSaveInstanceState(this, outState);
    }
}

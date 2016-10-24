package com.ksc.demo.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ksc.demo.util.activity.AppUtilActivity;

public class MainActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_btn_app_util:
                Intent intent = new Intent(this, AppUtilActivity.class);
                startActivity(intent);
                break;
        }
    }
}

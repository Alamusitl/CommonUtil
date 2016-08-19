package com.baidu.mobads.demo.main.feeds;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baidu.mobads.demo.main.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnListView = (Button) this.findViewById(R.id.btnList);
        btnListView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开启信息流广告形式
                startActivity(new Intent(MainActivity.this, ListViewActivity.class));
            }

        });
        Button btnOrigin = (Button) this.findViewById(R.id.btnOrigin);
        btnOrigin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开启原生字段形式
                startActivity(new Intent(MainActivity.this, NativeOriginActivity.class));
            }

        });

        Button btnVideoFeed = (Button) this.findViewById(R.id.btnVideoFeed);
        btnVideoFeed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开启非WiFi下二次确认下载activity
                Intent intent = new Intent(MainActivity.this, VideoFeedActivity.class);
                startActivity(intent);
            }

        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}

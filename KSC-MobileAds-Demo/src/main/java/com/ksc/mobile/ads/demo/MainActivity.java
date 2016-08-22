package com.ksc.mobile.ads.demo;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.ksc.client.ads.KSCADAgent;
import com.ksc.client.ads.KSCMobileAdKeyCode;
import com.ksc.client.ads.view.KSCMobileAdActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        KSCADAgent.getInstance().init(this);

        if (Build.VERSION.SDK_INT > 23) {
            System.out.println(Build.VERSION.BASE_OS);
            System.out.println(Build.VERSION.PREVIEW_SDK_INT);
            System.out.println(Build.VERSION.SECURITY_PATCH);
        }
        System.out.println(Build.VERSION.CODENAME);
        System.out.println(Build.VERSION.INCREMENTAL);
        System.out.println(Build.VERSION.RELEASE);
        System.out.println(Build.VERSION.SDK_INT);
        System.out.println(Build.BOARD);
        System.out.println(Build.BOOTLOADER);
        System.out.println(Build.BRAND);
        System.out.println(Build.DEVICE);
        System.out.println(Build.DISPLAY);
        System.out.println(Build.FINGERPRINT);
        System.out.println(Build.HARDWARE);
        System.out.println(Build.HOST);
        System.out.println(Build.ID);
        System.out.println(Build.MANUFACTURER);
        System.out.println(Build.MODEL);
        System.out.println(Build.PRODUCT);
        System.out.println(Build.SERIAL);


        findViewById(R.id.btnShowVideoAd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "test.mp4";
                Intent intent = new Intent(MainActivity.this, KSCMobileAdActivity.class);
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_TYPE, KSCMobileAdKeyCode.VIDEO_IN_CACHE);
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_PATH, path);
                startActivity(intent);
            }
        });
        findViewById(R.id.btnStreamVideoAd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, KSCMobileAdActivity.class);
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_TYPE, KSCMobileAdKeyCode.VIDEO_IN_STREAM);
                intent.putExtra(KSCMobileAdKeyCode.VIDEO_PATH, "http://v1.mukewang.com/a45016f4-08d6-4277-abe6-bcfd5244c201/L.mp4");
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        KSCADAgent.getInstance().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        KSCADAgent.getInstance().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KSCADAgent.getInstance().onDestroy();
    }
}

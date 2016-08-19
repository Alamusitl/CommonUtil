package com.baidu.mobads.demo.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.baidu.mobads.DubaoAd;
import com.baidu.mobads.DubaoAd.Position;

public class DubaoActivity extends Activity {

    private DubaoAd mDubao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String adPlaceId = "2682695"; // 重要：请填上您的广告位ID，代码位错误会导致无法请求到广告
        int position = Position.POSITION_LEFT; // 广告显示在屏幕左侧还是右侧
        double marginPercent = 0.2; // 广告距离屏幕顶部的距离占屏幕高度的百分比，取值范围0~1
        mDubao = new DubaoAd(this, adPlaceId, new Position(position, marginPercent));

        LinearLayout linearLayout = new LinearLayout(this);
        Button button = new Button(this);
        button.setText("关闭度宝广告");

        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mDubao != null) {
                    mDubao.destroy();
                    mDubao = null;
                }
            }
        });
        linearLayout.addView(button);
        setContentView(linearLayout);
    }
}

package com.baidu.mobads.demo.main;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.baidu.mobads.AdSize;
import com.baidu.mobads.InterstitialAd;
import com.baidu.mobads.InterstitialAdListener;

public class InterstitialAdForVideoAppActivity extends Activity {
    private InterstitialAd theAd;
    private RelativeLayout.LayoutParams reLayoutParams;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interstitialad_for_video_app);

        final RelativeLayout parentLayout = (RelativeLayout) this.findViewById(R.id.parent_interstitial);
        final RelativeLayout relativeLayout = new RelativeLayout(this);
        reLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        reLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        parentLayout.addView(relativeLayout, reLayoutParams);
        LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.interstitial_linear);
        button = new Button(this);
        linearLayout.addView(button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (theAd.isAdReady()) {
                    theAd.showAdInParentForVideoApp(InterstitialAdForVideoAppActivity.this, relativeLayout);
                } else {
                    loadAd();
                }
            }
        });
        final RadioButton qianRadioButton = new RadioButton(this);
        qianRadioButton.setText("前贴片");
        qianRadioButton.setTextColor(Color.BLACK);
        final RadioButton zanRadioButton = new RadioButton(this);
        zanRadioButton.setText("暂停页");
        zanRadioButton.setTextColor(Color.BLACK);
        RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(LinearLayout.HORIZONTAL);
        radioGroup.addView(qianRadioButton);
        radioGroup.addView(zanRadioButton);
        linearLayout.addView(radioGroup);
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (zanRadioButton.getId() == checkedId) {
                    // 暂停广告
                    createZanTingYeAd();
                } else if (qianRadioButton.getId() == checkedId) {
                    // 前贴片广告
                    createQianTiePianAd();
                }
            }
        });
        createZanTingYeAd();
    }

    /**
     * 创建前贴片广告
     */
    private void createQianTiePianAd() {
        String adPlaceId = "2058626"; // 重要：请填上您的广告位ID
        theAd = new InterstitialAd(this, AdSize.InterstitialForVideoBeforePlay, adPlaceId);
        theAd.setListener(new InterstitialAdListener() {

            @Override
            public void onAdClick(InterstitialAd arg0) {
                Log.i("QianTiePian_AD", "onAdClick");
            }

            @Override
            public void onAdDismissed() {
                Log.i("QianTiePian_AD", "onAdDismissed");
                button.setText("加载前贴片广告");
            }

            @Override
            public void onAdFailed(String arg0) {
                Log.i("QianTiePian_AD", "onAdFailed");
            }

            @Override
            public void onAdPresent() {
                Log.i("QianTiePian_AD", "onAdPresent");
            }

            @Override
            public void onAdReady() {
                Log.i("QianTiePian_AD", "onAdReady");
                button.setText("显示前贴片广告");
            }

        });
        button.setText("加载前贴片广告");
    }

    /**
     * 创建暂停页广告
     */
    private void createZanTingYeAd() {
        String adPlaceId = "2058626"; // 重要：请填上您的广告位ID
        theAd = new InterstitialAd(this, AdSize.InterstitialForVideoPausePlay, adPlaceId);
        theAd.setListener(new InterstitialAdListener() {

            @Override
            public void onAdClick(InterstitialAd arg0) {
                Log.i("ZanTingYe_AD", "onAdClick");
            }

            @Override
            public void onAdDismissed() {
                Log.i("ZanTingYe_AD", "onAdDismissed");
                button.setText("加载暂停页广告");
            }

            @Override
            public void onAdFailed(String arg0) {
                Log.i("ZanTingYe_AD", "onAdFailed");
            }

            @Override
            public void onAdPresent() {
                Log.i("ZanTingYe_AD", "onAdPresent");
            }

            @Override
            public void onAdReady() {
                Log.i("ZanTingYe_AD", "onAdReady");
                button.setText("显示暂停页广告");
            }

        });
        button.setText("加载暂停页广告");
    }

    /**
     * 加载广告
     */
    private void loadAd() {
        int width = getValueById(R.id.interstitial_width);
        int height = getValueById(R.id.interstitial_height);
        if (width <= 0 || height <= 0) {
            width = 600;
            height = 500;
        }
        reLayoutParams.width = width;
        reLayoutParams.height = height;
        // 广告请求的尺寸以及展现时传入的父控件尺寸都要合理，不能过小。
        theAd.loadAdForVideoApp(width, height);
    }

    private int getValueById(int viewId) {
        EditText editText = (EditText) this.findViewById(viewId);
        String value = editText.getText().toString();
        if (value.length() > 0) {
            try {
                return Integer.valueOf(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

}

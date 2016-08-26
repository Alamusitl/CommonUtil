package com.ksc.client.ads.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ksc.client.ads.KSCViewUtils;
import com.ksc.client.ads.config.KSCMobileAdKeyCode;

/**
 * Created by Alamusi on 2016/8/25.
 */
public class KSCLandingPageView extends RelativeLayout {

    private ImageView mCloseView;
    private WebView mLandingView;

    public KSCLandingPageView(Context context) {
        this(context, null);
    }

    public KSCLandingPageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KSCLandingPageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView(Context context) {
        setBackgroundColor(Color.WHITE);
        // 关闭View
        mCloseView = new ImageView(context);
        mCloseView.setId(KSCViewUtils.generateViewId());
        mCloseView.setBackgroundColor(Color.TRANSPARENT);
        mCloseView.setImageBitmap(KSCViewUtils.getBitmapFromAssets(context, KSCMobileAdKeyCode.IMG_VIDEO_VIEW_CLOSE));
        LayoutParams lp = new LayoutParams(100, 100);
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.setMargins(0, 20, 20, 20);
        addView(mCloseView, lp);
        // 显示H5的View
        mLandingView = new WebView(context);
        mLandingView.setWebViewClient(new WebViewClient());
        mLandingView.setWebChromeClient(new WebChromeClient());
        WebSettings settings = mLandingView.getSettings();
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setAppCacheEnabled(true);
        settings.setJavaScriptEnabled(true);

        lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.BELOW, mCloseView.getId());
        addView(mLandingView, lp);
    }

    public void setCloseViewClickListener(OnClickListener onClickListener) {
        mCloseView.setOnClickListener(onClickListener);
    }

    public void setLandingViewDownloadListener(DownloadListener downloadListener) {
        mLandingView.setDownloadListener(downloadListener);
    }

    public void setLandingViewUrl(String url) {
        mLandingView.loadUrl(url);
    }
}

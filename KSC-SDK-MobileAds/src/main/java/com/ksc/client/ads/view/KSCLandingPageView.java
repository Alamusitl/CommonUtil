package com.ksc.client.ads.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ksc.client.ads.config.KSCMobileAdKeyCode;
import com.ksc.client.util.KSCViewUtils;

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
        LayoutParams lp;
        // 显示H5的View
        mLandingView = new WebView(context);
        setWebViewClient();
        WebSettings settings = mLandingView.getSettings();
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setAppCacheEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setDefaultTextEncodingName("UTF-8");
        lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mLandingView, lp);

        // 关闭View
        mCloseView = new ImageView(context);
        mCloseView.setId(KSCViewUtils.generateViewId());
        mCloseView.setBackgroundColor(Color.TRANSPARENT);
        mCloseView.setImageBitmap(KSCViewUtils.getBitmapFromAssets(context, KSCMobileAdKeyCode.IMG_VIDEO_VIEW_CLOSE));
        lp = new LayoutParams(100, 100);
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.setMargins(0, 20, 20, 20);
        addView(mCloseView, lp);
    }

    public void setSize(int size) {
        ViewGroup.LayoutParams lp = mCloseView.getLayoutParams();
        lp.width = size;
        lp.height = size;
        mCloseView.setLayoutParams(lp);
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

    public void setLandingViewData(String data) {
        mLandingView.loadDataWithBaseURL(null, data, "text/html", "UTF-8", null);
    }

    private void setWebViewClient() {
        mLandingView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
            }
        });
        mLandingView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
            }

            @Override
            public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
                super.onReceivedTouchIconUrl(view, url, precomposed);
            }
        });
    }
}

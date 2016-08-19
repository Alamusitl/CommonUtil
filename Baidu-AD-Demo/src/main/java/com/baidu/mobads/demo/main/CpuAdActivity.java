package com.baidu.mobads.demo.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.baidu.mobads.CpuInfoManager;
import com.baidu.mobads.CpuInfoManager.UrlListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 内容联盟
 */
public class CpuAdActivity extends Activity {
    // 测试id
    private static String DEFAULT_APPSID = "d77e414";
    private WebView mWebView;
    private RelativeLayout mRootRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_union);
        initSpinner();
        Button button = (Button) this.findViewById(R.id.button);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showSelectedCpuWebPage();
            }
        });
    }

    /**
     * 调用SDK接口，获取内容联盟页面URL
     */
    private void showSelectedCpuWebPage() {
        // 内容联盟url获取后只能展示一次，多次展示需要每次通过以下接口重新获取
        // 媒体伙伴必须在MSSP业务端选择接入内容联盟的应用与频道类型，以便在接入内容页中生成广告，从而获得广告收益。
        // 不进行相关操作，将无法获得内容联盟页面的广告收益。
        CpuInfoManager.getCpuInfoUrl(this, getAppsid(), getChannel(), new UrlListener() {

            @Override
            public void onUrl(String url) {
                handleWebViewLayout(url);
            }
        });
    }

    /**
     * 初始化下拉框
     */
    private void initSpinner() {
        Spinner channelSpinner = (Spinner) this.findViewById(R.id.channel);
        channelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showSelectedCpuWebPage();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
        List<SpinnerItem> list = new ArrayList<SpinnerItem>();
        list.add(new SpinnerItem("娱乐频道", CpuInfoManager.CHANNEL_ENTERTAINMENT));
        list.add(new SpinnerItem("体育频道", CpuInfoManager.CHANNEL_SPORT));
        list.add(new SpinnerItem("图片频道", CpuInfoManager.CHANNEL_PICTURE));
        list.add(new SpinnerItem("手机频道", CpuInfoManager.CHANNEL_MOBILE));
        list.add(new SpinnerItem("财经频道", CpuInfoManager.CHANNEL_FINANCE));
        list.add(new SpinnerItem("汽车频道", CpuInfoManager.CHANNEL_AUTOMOTIVE));
        list.add(new SpinnerItem("房产频道", CpuInfoManager.CHANNEL_HOUSE));
        list.add(new SpinnerItem("热点频道", CpuInfoManager.CHANNEL_HOTSPOT));
        ArrayAdapter<SpinnerItem> dataAdapter = new ArrayAdapter<SpinnerItem>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        channelSpinner.setAdapter(dataAdapter);

    }

    /**
     * 根据内容联盟url，渲染页面
     *
     * @param url
     */
    private void handleWebViewLayout(String url) {
        initWebView();
        final ViewGroup totalView = (ViewGroup) this.getWindow().getDecorView();
        mRootRelativeLayout = new RelativeLayout(this);
        totalView.addView(mRootRelativeLayout, new ViewGroup.LayoutParams(-1, -1));
        RelativeLayout.LayoutParams webViewLayoutParams = new RelativeLayout.LayoutParams(-1, -1);
        webViewLayoutParams.topMargin = getStatusBarHeight();
        mRootRelativeLayout.addView(mWebView, webViewLayoutParams);
        mWebView.loadUrl(url);
        Button closeButton = new Button(this);
        closeButton.setBackgroundResource(R.drawable.close_icon);
        int side = (int) (30 * getScreenDensity(this));
        RelativeLayout.LayoutParams buttonLayoutParams = new RelativeLayout.LayoutParams(side, side);
        buttonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        buttonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        buttonLayoutParams.topMargin = getStatusBarHeight();
        closeButton.setLayoutParams(buttonLayoutParams);
        mRootRelativeLayout.addView(closeButton);
        closeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                close();
            }
        });
    }

    /**
     * 关闭内容联盟页面
     */
    private void close() {
        final ViewGroup totalView = (ViewGroup) this.getWindow().getDecorView();
        totalView.removeView(mRootRelativeLayout);
        mRootRelativeLayout.removeAllViews();
    }

    /**
     * 初始化展示内容联盟页面的webview
     */
    private void initWebView() {
        mWebView = new WebView(this);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // 如果是图片频道，则必须设置该接口为true，否则页面无法展现
        webSettings.setDomStorageEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

    }

    /**
     * 获取屏幕密度
     */
    public float getScreenDensity(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        return dm.density;
    }

    /**
     * 获取状态栏高度
     */
    private int getStatusBarHeight() {
        Rect frame = new Rect();
        this.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }

    /**
     * 获取appsid
     *
     * @return
     */
    private String getAppsid() {
        String value = getValueFromEditText(R.id.appsid);
        if (TextUtils.isEmpty(value)) {
            return DEFAULT_APPSID;
        } else {
            return value;
        }
    }

    /**
     * 获取频道
     *
     * @return
     */
    private int getChannel() {
        Spinner channelSpinner = (Spinner) this.findViewById(R.id.channel);
        SpinnerItem selectedItem = (SpinnerItem) channelSpinner.getSelectedItem();
        return selectedItem.getChannel();
    }

    private String getValueFromEditText(int id) {
        EditText appsidEditText = (EditText) this.findViewById(id);
        String value = appsidEditText.getText().toString();
        return value;
    }

    /*
     * 监听返回键，浏览器回退
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mRootRelativeLayout == null || mRootRelativeLayout.getParent() == null) {
            return super.onKeyDown(keyCode, event);
        }
        try {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (mRootRelativeLayout != null && mRootRelativeLayout.getParent() != null
                        && mWebView != null && mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    class SpinnerItem extends Object {
        /**
         * 频道名称
         */
        String text;
        /**
         * 频道id
         */
        int channel;

        public SpinnerItem(String text, int channel) {
            this.text = text;
            this.channel = channel;
        }

        @Override
        public String toString() {
            return text;
        }

        int getChannel() {
            return channel;
        }

    }
}

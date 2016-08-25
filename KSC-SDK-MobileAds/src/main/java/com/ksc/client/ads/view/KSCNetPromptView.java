package com.ksc.client.ads.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ksc.client.ads.KSCViewUtils;

/**
 * Created by Alamusi on 2016/8/25.
 */
public class KSCNetPromptView extends RelativeLayout {

    private TextView mPromptMsgView;
    private Button mConfirmView;
    private int mBackgroundColor = Color.parseColor("#b2FFFFFF");
    private int mPromptTextColor = Color.parseColor("#990000");
    private int mConfirmBackground = Color.parseColor("#b51010");
    private int mConfirmTextColor = Color.parseColor("#fefefe");

    public KSCNetPromptView(Context context) {
        this(context, null);
    }

    public KSCNetPromptView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KSCNetPromptView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        float[] backgroundRadii = {30, 30, 30, 30, 30, 30, 30, 30};
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadii(backgroundRadii);
        background.setColor(mBackgroundColor);
        background.setStroke(0, mBackgroundColor);
        setViewBackground(this, background);

        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mPromptMsgView = new TextView(context);
        mPromptMsgView.setId(KSCViewUtils.generateViewId());
        mPromptMsgView.setText("当前处于非WIFI环境下，您确定要下载吗？");
        mPromptMsgView.setTextColor(mPromptTextColor);
        mPromptMsgView.setPadding(50, 50, 50, 50);
        mPromptMsgView.setGravity(Gravity.CENTER);
        mPromptMsgView.setBackgroundColor(Color.TRANSPARENT);
        addView(mPromptMsgView, lp);

        mConfirmView = new Button(context);
        mConfirmView.setId(KSCViewUtils.generateViewId());
        GradientDrawable confirmBackground = new GradientDrawable();
        confirmBackground.setCornerRadius(90);
        confirmBackground.setColor(mConfirmBackground);
        confirmBackground.setStroke(0, mConfirmBackground);
        setViewBackground(mConfirmView, confirmBackground);
        mConfirmView.setText("确定");
        mConfirmView.setTextColor(mConfirmTextColor);
        mConfirmView.setPadding(150, 20, 150, 20);
        lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.addRule(BELOW, mPromptMsgView.getId());
        lp.addRule(CENTER_HORIZONTAL);
        lp.bottomMargin = 50;
        addView(mConfirmView, lp);
    }

    /**
     * 设置View的背景图片
     *
     * @param view     需要设置背景的View
     * @param drawable 背景Drawable
     */
    private void setViewBackground(View view, GradientDrawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            // noinspection deprecation
            view.setBackgroundDrawable(drawable);
        }
    }


    public void setPromptMsg(String text) {
        mPromptMsgView.setText(text);
    }

    public void setConfirmViewClickListenr(OnClickListener onClickListener) {
        mConfirmView.setOnClickListener(onClickListener);
    }
}

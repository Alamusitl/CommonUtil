package com.ksc.client.ads.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ksc.client.ads.config.KSCColor;
import com.ksc.client.util.KSCViewUtils;

/**
 * Created by Alamusi on 2016/8/25.
 */
public class KSCNetPromptView extends LinearLayout {

    private TextView mPromptMsgView;
    private Button mConfirmView;

    public KSCNetPromptView(Context context) {
        this(context, null);
    }

    public KSCNetPromptView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        setOrientation(VERTICAL);
        float[] backgroundRadii = {30, 30, 30, 30, 30, 30, 30, 30};
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadii(backgroundRadii);
        background.setColor(KSCColor.TRANSPARENT_WHITE_8);
        background.setStroke(0, KSCColor.TRANSPARENT_WHITE_8);
        setViewBackground(this, background);

        int textSize = 20; // sp

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        lp.weight = 1;
        mPromptMsgView = new TextView(context);
        mPromptMsgView.setId(KSCViewUtils.generateViewId());
        mPromptMsgView.setTextColor(KSCColor.TEXT_RED);
        mPromptMsgView.setTextSize(textSize);
        mPromptMsgView.setGravity(Gravity.CENTER);
        addView(mPromptMsgView, lp);

        RelativeLayout parentLayout = new RelativeLayout(context);
        parentLayout.setId(KSCViewUtils.generateViewId());
        parentLayout.setBackgroundColor(Color.TRANSPARENT);
        addView(parentLayout, lp);

        mConfirmView = new Button(context);
        mConfirmView.setId(KSCViewUtils.generateViewId());
        GradientDrawable confirmBackground = new GradientDrawable();
        confirmBackground.setCornerRadius(90);
        confirmBackground.setColor(KSCColor.BACKGROUND_RED);
        confirmBackground.setStroke(0, KSCColor.BACKGROUND_RED);
        setViewBackground(mConfirmView, confirmBackground);
        mConfirmView.setTextColor(KSCColor.TEXT_WHITE);
        mConfirmView.setTextSize(textSize);
        mConfirmView.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams confirmViewLp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        confirmViewLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        parentLayout.addView(mConfirmView, confirmViewLp);
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

    /**
     * 设置显示的字段
     *
     * @param promptText  提示字段
     * @param confirmText 按钮字段
     */
    public void setText(String promptText, String confirmText) {
        mPromptMsgView.setText(promptText);
        mConfirmView.setText(confirmText);
    }

    /**
     * 设置大小
     *
     * @param width  宽度
     * @param height 高度
     */
    public void setSize(int width, int height) {
        ViewGroup.LayoutParams lp = mConfirmView.getLayoutParams();
        lp.width = width * 7 / 10;
        lp.height = height / 4;
        mConfirmView.setLayoutParams(lp);
    }

    /**
     * 设置点击监听
     *
     * @param onClickListener 点击监听器
     */
    public void setConfirmViewClickListener(OnClickListener onClickListener) {
        mConfirmView.setOnClickListener(onClickListener);
    }
}

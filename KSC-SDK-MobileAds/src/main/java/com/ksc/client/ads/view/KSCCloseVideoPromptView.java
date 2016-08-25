package com.ksc.client.ads.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ksc.client.ads.KSCViewUtils;

/**
 * Created by Alamusi on 2016/8/24.
 */
public class KSCCloseVideoPromptView extends LinearLayout {

    private TextView mPromptMsg;
    private Button mCloseVideo;
    private Button mContinue;
    private int color_white = Color.parseColor("#efefef");
    private int wathet_blue = Color.parseColor("#0190ae");
    private int dark_blue = Color.parseColor("#026175");
    private int translucent_white = Color.parseColor("#D9FFFFFF");

    private int mTextSize = 18;// sp

    public KSCCloseVideoPromptView(Context context) {
        super(context);
        initView(context);
    }

    public KSCCloseVideoPromptView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    /**
     * 初始化View
     *
     * @param context 上下文
     */
    private void initView(Context context) {
        setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams parentLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);

        //  提示信息
        mPromptMsg = new TextView(context);
        mPromptMsg.setId(KSCViewUtils.generateViewId());
        mPromptMsg.setTextColor(dark_blue);
        mPromptMsg.setLineSpacing(5, 1);
        mPromptMsg.setTextSize(mTextSize);
        mPromptMsg.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        mPromptMsg.setMinLines(2);
        mPromptMsg.setPadding(50, 60, 50, 60);

        float[] textViewRadii = {30, 30, 30, 30, 0, 0, 0, 0};
        GradientDrawable promptViewBackground = new GradientDrawable();
        promptViewBackground.setColor(translucent_white);
        promptViewBackground.setCornerRadii(textViewRadii);
        promptViewBackground.setStroke(0, translucent_white);
        setViewBackground(mPromptMsg, promptViewBackground);
        parentLayoutParam.weight = 2;
        addView(mPromptMsg, parentLayoutParam);

        RelativeLayout controlView = new RelativeLayout(context);
        controlView.setId(KSCViewUtils.generateViewId());
        parentLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        parentLayoutParam.weight = 1;
        addView(controlView, parentLayoutParam);

        RelativeLayout.LayoutParams lp;

        // 分割图片
        ImageView imgLine = new ImageView(context);
        imgLine.setId(KSCViewUtils.generateViewId());
        imgLine.setBackgroundColor(color_white);
        imgLine.setImageDrawable(new ColorDrawable(wathet_blue));
        imgLine.setPadding(0, 30, 0, 30);
        lp = new RelativeLayout.LayoutParams(5, RelativeLayout.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        controlView.addView(imgLine, lp);

        // 关闭按钮
        mCloseVideo = new Button(context);
        mCloseVideo.setTextColor(wathet_blue);
        mCloseVideo.setBackgroundColor(color_white);
        mCloseVideo.setId(KSCViewUtils.generateViewId());
        mCloseVideo.setGravity(Gravity.CENTER);
        mCloseVideo.setTextSize(mTextSize);
        float[] closeViewRadii = {0, 0, 0, 0, 0, 0, 30, 30};
        GradientDrawable closeViewBackground = new GradientDrawable();
        closeViewBackground.setColor(color_white);
        closeViewBackground.setCornerRadii(closeViewRadii);
        closeViewBackground.setStroke(0, color_white);
        setViewBackground(mCloseVideo, closeViewBackground);
        lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.LEFT_OF, imgLine.getId());
        controlView.addView(mCloseVideo, lp);

        // 继续观看
        mContinue = new Button(context);
        mContinue.setTextColor(wathet_blue);
        mContinue.setBackgroundColor(color_white);
        mContinue.setId(KSCViewUtils.generateViewId());
        mContinue.setGravity(Gravity.CENTER);
        mContinue.setTextSize(mTextSize);
        float[] continueViewRadii = {0, 0, 0, 0, 30, 30, 0, 0};
        GradientDrawable continueViewBackground = new GradientDrawable();
        continueViewBackground.setColor(color_white);
        continueViewBackground.setCornerRadii(continueViewRadii);
        continueViewBackground.setStroke(0, color_white);
        setViewBackground(mContinue, continueViewBackground);
        lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.RIGHT_OF, imgLine.getId());
        controlView.addView(mContinue, lp);

        setText();
    }

    private void setText() {
        mPromptMsg.setText("注意: 视频播放不完全，将得不到奖励！您确定要提前关闭吗？");
        mCloseVideo.setText("关闭");
        mContinue.setText("继续观看");
    }

    /**
     * 设置关闭按钮的点击事件监听
     *
     * @param onClickListener 点击事件监听
     */
    public void setCloseButtonClickListener(OnClickListener onClickListener) {
        mCloseVideo.setOnClickListener(onClickListener);
    }

    /**
     * 继续观看按钮的点击事件监听
     *
     * @param onClickListener 点击事件监听
     */
    public void setContinueButtonClickListener(OnClickListener onClickListener) {
        mContinue.setOnClickListener(onClickListener);
    }

    /**
     * 设置提示内容信息
     *
     * @param msg 显示信息
     */
    public void setText(String msg) {
        mPromptMsg.setText(msg);
    }

    /**
     * 设置关闭按钮的内容
     *
     * @param text 内容
     */
    public void setCloseButtonText(String text) {
        mCloseVideo.setText(text);
    }

    /**
     * 设置继续按钮的内容
     *
     * @param text 内容
     */
    public void setContinueButtonText(String text) {
        mContinue.setText(text);
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

}

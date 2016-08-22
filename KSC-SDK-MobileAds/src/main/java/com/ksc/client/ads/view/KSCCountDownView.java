package com.ksc.client.ads.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Alamusi on 2016/8/19.
 */
public class KSCCountDownView extends TextView {

    /**
     * View 显示区域
     */
    private Rect mBounds = new Rect();

    /**
     * 进度条的矩形区域
     */
    private RectF mArcRect = new RectF();

    /**
     * 画笔
     */
    private Paint mPaint = new Paint();

    /**
     * 外部轮廓的颜色，默认黑色
     */
    private int mOutLineColor = Color.BLACK;

    /**
     * 外部轮廓的宽度，默认为2
     */
    private int mOutLineWidth = 2;

    /**
     * 进度条的颜色，默认蓝色
     */
    private int mProgressLineColor = Color.BLUE;

    /**
     * 进度条的宽度，默认为8
     */
    private int mProgressLineWidth = 8;

    /**
     * 进度条类型，默认为顺数倒计时
     */
    private ProgressType mProgressType = ProgressType.COUNT;

    /**
     * 当期的进度值
     */
    private float mProgress;

    /**
     * 倒计时总时间，默认15秒
     */
    private int mTotalCountDownTime = 15 * 1000;

    /**
     * 当前倒计时时间
     */
    private int mCurrentCountDownTime;

    /**
     * 显示的内容
     */
    private String mContent;

    /**
     * 内容的字体大小
     */
    private float mContentSize = getTextSize();

    /**
     * 内容的颜色
     */
    private int mContentColor = getCurrentTextColor();

    /**
     * 内部圆的颜色，默认透明
     */
    private ColorStateList mInnerCircleColor = ColorStateList.valueOf(Color.TRANSPARENT);

    /**
     * 中心圆的颜色，默认透明
     */
    private int mCircleColor = Color.TRANSPARENT;

    public KSCCountDownView(Context context) {
        this(context, null);
    }

    public KSCCountDownView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KSCCountDownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 获得View的边界
        getDrawingRect(mBounds);

        int size = mBounds.height() > mBounds.width() ? mBounds.width() : mBounds.height();
        float outRadius = size / 2;

        // 画内部圆
        int circleColor = mInnerCircleColor.getColorForState(getDrawableState(), Color.TRANSPARENT);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(circleColor);
        canvas.drawCircle(mBounds.centerX(), mBounds.centerY(), outRadius - mOutLineWidth, mPaint);

        // 画边框圆
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mOutLineWidth);
        mPaint.setColor(mOutLineColor);
        canvas.drawCircle(mBounds.centerX(), mBounds.centerY(), outRadius - mOutLineWidth / 2, mPaint);

        //画内容
        Paint paint = getPaint();
        paint.setAntiAlias(true);
        paint.setColor(mContentColor);
        paint.setTextSize(mContentSize);
        paint.setTextAlign(Paint.Align.CENTER);
        float textY = mBounds.centerY() - (paint.descent() + paint.ascent()) / 2;
        canvas.drawText(mContent, mBounds.centerX(), textY, paint);

        // 画进度条
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mProgressLineWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(mProgressLineColor);
        int deleteWidth = (mProgressLineWidth + mOutLineWidth) / 2;
        mArcRect.set(mBounds.left + deleteWidth, mBounds.top + deleteWidth, mBounds.right - deleteWidth, mBounds.bottom - deleteWidth);
        canvas.drawArc(mArcRect, -90, 360 * mProgress, false, mPaint);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        validateCircleColor();
    }

    /******************************************
     * Class member setter & getter method
     ******************************************/

    /**
     * 获得外部轮廓的颜色
     *
     * @return 颜色值
     */
    public int getOutLineColor() {
        return mOutLineColor;
    }

    /**
     * 设置外部轮廓的颜色
     *
     * @param color 颜色值
     */
    public void setOutLineColor(@ColorInt int color) {
        mOutLineColor = color;
        invalidate();
    }

    /**
     * 获得外部轮廓的宽度
     *
     * @return 宽度值
     */
    public int getOutLineWidth() {
        return mOutLineWidth;
    }

    /**
     * 设置外部轮廓的宽度
     *
     * @param width 宽度值
     */
    public void setOutLineWidth(int width) {
        mOutLineWidth = width;
        invalidate();
    }

    /**
     * 获得进度条的颜色值
     *
     * @return 颜色值
     */
    public int getProgressLineColor() {
        return mProgressLineColor;
    }

    /**
     * 设置进度条的颜色
     *
     * @param color 颜色值
     */
    public void setProgressLineColor(@ColorInt int color) {
        mProgressLineColor = color;
        invalidate();
    }

    /**
     * 获得进度条的宽度值
     *
     * @return 宽度值
     */
    public int getProgressLineWidth() {
        return mProgressLineWidth;
    }

    /**
     * 设置进度条的宽度
     *
     * @param width 宽度值
     */
    public void setProgressLineWidth(int width) {
        mProgressLineWidth = width;
        invalidate();
    }

    /**
     * 获得进度条的类型(顺时针Or逆时针)
     *
     * @return 进度条的类型
     */
    public ProgressType getProgressType() {
        return mProgressType;
    }

    /**
     * 设置进度条的类型
     *
     * @param type 类型(顺时针Or逆时针)
     */
    public void setProgressType(ProgressType type) {
        mProgressType = type;
        invalidate();
    }

    /**
     * 设置进度条的进度值
     *
     * @param currentCountDownTime 当期的时间
     * @param totalCountDownTime   总的时间
     */
    public void setProgress(int currentCountDownTime, int totalCountDownTime) {
        switch (mProgressType) {
            case COUNT:
                mProgress = 1 - (currentCountDownTime / (float) totalCountDownTime);
                break;
            case COUNT_BACK:
                mProgress = currentCountDownTime / (float) totalCountDownTime;
                break;
        }
    }

    /**
     * 获得倒计时总时间
     *
     * @return 倒计时总时间
     */
    public int getTotalCountDownTime() {
        return mTotalCountDownTime;
    }

    /**
     * 设置倒计时总时间，单位为毫秒
     *
     * @param totalCountDownTime 倒计时总时间
     */
    public void setTotalCountDownTime(int totalCountDownTime) {
        mTotalCountDownTime = totalCountDownTime;
        setProgress(mCurrentCountDownTime, mTotalCountDownTime);
        setContent(String.valueOf(mTotalCountDownTime / 1000));
        invalidate();
    }

    /**
     * 获得当前倒计时的时间
     *
     * @return 当前倒计时时间
     */
    public int getCurrentCountDownTime() {
        return mCurrentCountDownTime;
    }

    /**
     * 设置当前倒计时时间
     *
     * @param currentCountDownTime 倒计时时间，单位为毫秒
     */
    public void setCurrentCountDownTime(int currentCountDownTime) {
        mCurrentCountDownTime = currentCountDownTime;
        setProgress(mCurrentCountDownTime, mTotalCountDownTime);
        setContent(String.valueOf(mCurrentCountDownTime / 1000));
        invalidate();
    }

    /**
     * 设置显示的内容
     *
     * @param content 内容
     */
    public void setContent(String content) {
        mContent = content;
    }

    /**
     * 设置内容的字体大小
     *
     * @param size 大小
     */
    public void setContentSize(float size) {
        mContentSize = size;
    }

    /**
     * 设置内容的颜色
     *
     * @param color 颜色值
     */
    public void setContentColor(int color) {
        mContentColor = color;
    }

    /**
     * 设置圆形的填充颜色
     *
     * @param innerCircleColor 颜色值
     */
    public void setInnerCircleColor(@ColorInt int innerCircleColor) {
        mInnerCircleColor = ColorStateList.valueOf(innerCircleColor);
        invalidate();
    }

    /******************************************
     * Operation method
     ******************************************/

    /**
     * 验证是否重新绘制圆的颜色
     */
    private void validateCircleColor() {
        int circleColor = mInnerCircleColor.getColorForState(getDrawableState(), Color.TRANSPARENT);
        if (mCircleColor != circleColor) {
            mCircleColor = circleColor;
            invalidate();
        }
    }

    /**
     * 进度条类型。
     */
    public enum ProgressType {
        /**
         * 顺数进度条，从0-100；
         */
        COUNT,

        /**
         * 倒数进度条，从100-0；
         */
        COUNT_BACK
    }

}
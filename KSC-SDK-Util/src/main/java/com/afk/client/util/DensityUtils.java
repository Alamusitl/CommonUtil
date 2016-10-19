package com.afk.client.util;

import android.content.Context;
import android.util.TypedValue;

/**
 * 常用单位转换的辅助类
 * Created by Alamusi on 2016/9/29.
 */
public class DensityUtils {

    /**
     * dp转px
     *
     * @param context 上下文
     * @param dpVal   dp值
     * @return 转化后的px值
     */
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, context.getResources().getDisplayMetrics());
    }

    /**
     * sp转px
     *
     * @param context 上下文
     * @param spVal   sp值
     * @return 转化后的px值
     */
    public static int sp2px(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal, context.getResources().getDisplayMetrics());
    }

    /**
     * px转dp
     *
     * @param context 上下文
     * @param pxVal   px值
     * @return 转化后的dp值
     */
    public static float px2dp(Context context, float pxVal) {
        return pxVal / context.getResources().getDisplayMetrics().density;
    }

    /**
     * px转sp
     *
     * @param context 上下文
     * @param pxVal   px值
     * @return 转化后的sp值
     */
    public static float px2sp(Context context, float pxVal) {
        return pxVal / context.getResources().getDisplayMetrics().density;
    }
}

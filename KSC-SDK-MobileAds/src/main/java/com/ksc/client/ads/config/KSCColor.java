package com.ksc.client.ads.config;

import android.graphics.Color;

/**
 * Created by Alamusi on 2016/8/26.
 */
public class KSCColor {

    public static final int TRANSPARENT_WHITE_8 = Color.parseColor("#D9FFFFFF");// 15%透明白色
    public static final int BACKGROUND_WHITE = Color.parseColor("#EFEFEF");// 按钮背景白色
    public static final int BACKGROUND_RED = Color.parseColor("#B51010");// 按钮背景颜色，红色
    public static final int WATHET_BLUE = Color.parseColor("#0190AE");// 按钮字体颜色，浅蓝色
    public static final int DARK_BLUE = Color.parseColor("#026175");// 提示字体颜色，深蓝色
    public static final int TEXT_RED = Color.parseColor("#990000");// 提示字体颜色，红色
    public static final int TEXT_WHITE = Color.parseColor("#FEFEFE");// 按钮字体颜色，白色

    public static int getColor(String colorString) {
        return Color.parseColor(colorString);
    }
}

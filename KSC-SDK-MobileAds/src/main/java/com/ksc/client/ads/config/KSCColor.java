package com.ksc.client.ads.config;

import android.graphics.Color;

/**
 * Created by Alamusi on 2016/8/26.
 */
public class KSCColor {

    public static final int TRANSPARENT_BLACK_7 = Color.parseColor("#B2000000");// 30%透明的黑色
    public static final int BACKGROUND_WHITE = Color.parseColor("#EFEFEF");// 按钮背景白色
    public static final int WATHET_BLUE = Color.parseColor("#0190AE");// 按钮字体颜色
    public static final int DARK_BLUE = Color.parseColor("#026175");// 提示字体颜色
    public static final int TRANSPARENT_WHITE_8 = Color.parseColor("#D9FFFFFF");// 15%透明白色

    public static int getColor(String colorString) {
        return Color.parseColor(colorString);
    }
}

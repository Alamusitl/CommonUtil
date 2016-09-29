package com.ksc.client.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

/**
 * 屏幕相关的辅助类
 * Created by Alamusi on 2016/9/29.
 */
public class KSCScreenUtils {

    /**
     * 获得屏幕宽度
     *
     * @param context 上下文
     * @return 屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 获得屏幕高度
     *
     * @param context 上下文
     * @return 屏幕高度
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 获得状态栏告诉
     *
     * @param context 上下文
     * @return 状态栏告诉
     */
    public static int getStatusHeight(Context context) {
        int height = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = context.getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }

    /**
     * 获取当前屏幕截图，包含状态栏
     *
     * @param activity 上下文
     * @return 截图的Bitmap
     */
    public static Bitmap snapShotWithStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bitmap = Bitmap.createBitmap(bmp, 0, 0, width, height);
        view.destroyDrawingCache();
        return bitmap;
    }

    /**
     * 获取当前屏幕截图，不包含状态栏
     *
     * @param activity 上下文
     * @return 截图的Bitmap
     */
    public static Bitmap snapShotWithoutStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int statusBarHeight = getStatusHeight(activity);
        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bitmap = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return bitmap;
    }
}

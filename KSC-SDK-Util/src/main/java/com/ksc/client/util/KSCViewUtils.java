package com.ksc.client.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Alamusi on 2016/8/19.
 */
public class KSCViewUtils {

    private static final AtomicInteger mNextGenerateId = new AtomicInteger(1);
    private static final String TAG = KSCViewUtils.class.getSimpleName();

    /**
     * Generate a value suitable for use in setId(int).
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId();
        } else {
            for (; ; ) {
                final int result = mNextGenerateId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (mNextGenerateId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        }
    }

    public static BitmapDrawable getBitmapDrawableFromAssets(Context context, String fileName) {
        BitmapDrawable drawable = null;
        AssetManager manager = context.getResources().getAssets();
        try {
            InputStream is = manager.open(fileName);
            drawable = new BitmapDrawable(context.getResources(), is);
        } catch (IOException e) {
            Log.e(TAG, "getBitmapDrawableFromAssets: ", e);
        }
        return drawable;
    }

    public static Bitmap getBitmapFromAssets(Context context, String fileName) {
        Bitmap bitmap = null;
        AssetManager manager = context.getResources().getAssets();
        try {
            InputStream is = manager.open(fileName);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            Log.e(TAG, "getBitmapFromAssets: ", e);
        }
        return bitmap;
    }
}

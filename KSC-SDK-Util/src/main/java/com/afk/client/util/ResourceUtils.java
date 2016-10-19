package com.afk.client.util;

import android.content.Context;

/**
 * Created by Alamusi on 2016/6/29.
 */
public class ResourceUtils {

    public static int getStringId(Context context, String name) {
        return context.getResources().getIdentifier(name, "string", context.getPackageName());
    }

    public static String getString(Context context, String name) {
        String data = null;
        try {
            data = context.getResources().getString(getStringId(context, name));
        } catch (Exception e) {
            // ignore
        }
        return data;
    }

    public static int getId(Context context, String name) {
        int id = 0;
        try {
            id = context.getResources().getIdentifier(name, "id", context.getPackageName());
        } catch (Exception e) {
            // ignore
        }
        return id;
    }

    public static int getLayoutId(Context context, String name) {
        int id = 0;
        try {
            id = context.getResources().getIdentifier(name, "layout", context.getPackageName());
        } catch (Exception e) {
            // ignore
        }
        return id;
    }

    public static int getDrawableId(Context context, String name) {
        int id = 0;
        try {
            id = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        } catch (Exception e) {
            // ignore
        }
        return id;
    }
}

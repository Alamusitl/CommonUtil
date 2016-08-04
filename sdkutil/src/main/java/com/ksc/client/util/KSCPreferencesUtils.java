package com.ksc.client.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Alamusi on 2016/6/22.
 */
public class KSCPreferencesUtils {

    private static final String K_APP_UUID = "SDK_UUID";

    public static String getUUID(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(K_APP_UUID, Context.MODE_PRIVATE);
        return sharedPreferences.getString("UUID", null);
    }

    public static void setUUID(Context context, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(K_APP_UUID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("UUID", value);
        editor.apply();
    }

    public static String getValue(Context context, String name, String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }

    public static String getValue(Context context, String name, String key) {
        return getValue(context, name, key, null);
    }

    public static void setValue(Context context, String name, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
}

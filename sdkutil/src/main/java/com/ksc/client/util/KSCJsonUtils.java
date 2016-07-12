package com.ksc.client.util;

import org.json.JSONObject;

/**
 * Created by Alamusi on 2016/6/27.
 */
public class KSCJsonUtils {
    public static String getString(JSONObject data, String key) {
        if (data == null || key == null) {
            return null;
        }
        if (data.has(key) && !data.isNull(key)) {
            return data.optString(key);
        }
        return null;
    }

    public static int getInt(JSONObject data, String key) {
        if (data == null || key == null) {
            return -1;
        }
        if (data.has(key) && !data.isNull(key)) {
            return data.optInt(key);
        }
        return -1;
    }
}

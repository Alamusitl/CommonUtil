package com.ksc.client.util;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;

/**
 * Created by Alamusi on 2016/6/23.
 */
public class KSCHelpUtils {

    /**
     * 比较两个数字的大小，当第一个参数大于第二个参数时返回1，等于是0，反之-1
     *
     * @param src
     * @param dest
     * @return
     */
    public static int compare(String src, String dest) {
        int result = -1;
        if (!isNumber(src) || !isNumber(dest)) {
            return result;
        }
        int tmp = src.compareToIgnoreCase(dest);
        return src.compareToIgnoreCase(dest);
    }

    private static boolean isNumber(String str) {
        if (str == null || "".equals(str)) {
            return false;
        }
        return str.matches("^[0-9]+(.[0-9]*)?$");
    }

    public static String changeToStr(long size) {
        DecimalFormat df = new DecimalFormat("#.00");
        String sizeStr;
        if (size >= 1024) {
            float sizeFloat = size / (float) 1024;
            sizeStr = df.format(sizeFloat) + "KB";
            if (sizeFloat >= 1024) {
                sizeFloat = sizeFloat / 1024;
                sizeStr = df.format(sizeFloat) + "MB";
            }
        } else {
            sizeStr = size + "b";
        }
        return sizeStr;
    }

    /**
     * 加密 AES—>Base64—>URLEncode
     *
     * @param content 原始数据
     * @param key     AES密码
     * @return 加密后的数据or null
     */
    public static String encodeParam(String content, String key) {
        if (content == null) {
            KSCLog.e("content is null!");
            return null;
        }
        if (key == null || key.length() != 16) {
            KSCLog.e("key is null or length is not 16!");
            return null;
        }
        byte[] encryptParam = KSCAESUtils.encrypt(content, key);
        if (encryptParam == null) {
            KSCLog.e("AES encrypt param error!");
            return null;
        }
        String base64Param = Base64.encodeToString(encryptParam, Base64.DEFAULT);
        if (base64Param == null) {
            KSCLog.e("Base64 encode param error!");
            return null;
        }
        try {
            return URLEncoder.encode(base64Param, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            KSCLog.e("url encode param exception", e);
            return null;
        }
    }

    /**
     * 解密 URLDecode—>Base64—>AES
     *
     * @param content 加密后的数据
     * @param key     AES密码
     * @return 原始数据or null
     */
    public static String decodeParam(String content, String key) {
        if (content == null) {
            KSCLog.e("content is null!");
            return null;
        }
        if (key == null || key.length() != 16) {
            KSCLog.e("key is null or length is not 16!");
            return null;
        }
        String decodeParam;
        try {
            decodeParam = URLDecoder.decode(content, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            KSCLog.e("url decode param exception", e);
            return null;
        }
        byte[] base64Param = Base64.decode(decodeParam, Base64.DEFAULT);
        if (base64Param == null) {
            KSCLog.e("Base64 decode param error!");
            return null;
        }
        byte[] decryptParam = KSCAESUtils.decrypt(base64Param, key);
        if (decryptParam == null) {
            KSCLog.e("AES decrypt param error!");
            return null;
        }
        return new String(decryptParam);
    }
}

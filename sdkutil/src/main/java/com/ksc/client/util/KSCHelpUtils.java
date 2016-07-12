package com.ksc.client.util;

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
}

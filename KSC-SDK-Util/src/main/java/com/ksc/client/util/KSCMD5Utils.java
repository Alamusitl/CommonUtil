package com.ksc.client.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

/**
 * Created by Alamusi on 2016/7/28.
 */
public class KSCMD5Utils {

    private static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 获得文件的MD5值, 32位
     *
     * @param file 目标文件
     * @return 返回32位小写的MD5值
     */
    public static String getFileMD5(File file) {
        if (!file.exists()) {
            KSCLog.e("file " + file.getName() + " not exist!");
            return null;
        }
        if (!file.isFile()) {
            KSCLog.e("file " + file.getName() + " is not a file!");
            return null;
        }
        try {
            MessageDigest digest;
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int readSize;
            digest = MessageDigest.getInstance("MD5");
            while ((readSize = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, readSize);
            }
            byte[] b = digest.digest();
            return byteToHexString(b);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String md5(String msg) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(msg.getBytes("UTF-8"));
            byte[] encryptStr = messageDigest.digest();
            if (encryptStr == null) {
                return "";
            }
            StringBuilder builder = new StringBuilder();
            for (byte anEncryptStr : encryptStr) {
                if (Integer.toHexString(0xff & anEncryptStr).length() == 1) {
                    builder.append("0").append(Integer.toHexString(0xff & anEncryptStr));
                } else {
                    builder.append(Integer.toHexString(0xff & anEncryptStr));
                }
            }
            return builder.toString();
        } catch (Exception e) {
            KSCLog.e("md5 sign " + msg + " exception", e);
            return "";
        }
    }

    /**
     * 把byte[]数组转换成十六进制字符串表示形式
     *
     * @param tmp 要转换的byte[]
     * @return 十六进制字符串表示形式
     */

    private static String byteToHexString(byte[] tmp) {
        String s;
        // 用字节表示就是 16 个字节
        char str[] = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符，
        // 所以表示成 16 进制需要 32 个字符
        int k = 0; // 表示转换结果中对应的字符位置
        for (int i = 0; i < 16; i++) { // 从第一个字节开始，对 MD5 的每一个字节
            // 转换成 16 进制字符的转换
            byte byte0 = tmp[i]; // 取第 i 个字节
            str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换,
            // >>> 为逻辑右移，将符号位一起右移
            str[k++] = hexDigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
        }
        s = new String(str); // 换后的结果转换为字符串
        return s.toLowerCase();
    }
}

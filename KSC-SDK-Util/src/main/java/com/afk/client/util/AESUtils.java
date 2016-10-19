package com.afk.client.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Alamusi on 2016/8/15.
 */
public class AESUtils {

    /**
     * AES加密
     *
     * @param content    需要加密的字符串
     * @param privateKey 密钥，必须为16位
     * @return 加密后的byte[]
     */
    public static byte[] encrypt(String content, String privateKey) {
        if (content == null) {
            Logger.e("content is null!");
            return null;
        }
        if (privateKey == null || privateKey.length() != 16) {
            Logger.e("key is null or length is not 16!");
            return null;
        }
        try {
            byte[] enCodeFormat = privateKey.getBytes();
            SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec("0102030405060708".getBytes()));
            return cipher.doFinal(byteContent);
        } catch (Exception e) {
            Logger.e("AES Encrypt Exception, " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * AES解密
     *
     * @param content    需要解密的byte[]
     * @param privateKey 密钥，必须为16位
     * @return 解密后的byte[]
     */
    public static byte[] decrypt(byte[] content, String privateKey) {
        if (content == null) {
            Logger.e("content is null!");
            return null;
        }
        if (privateKey == null || privateKey.length() != 16) {
            Logger.e("key is null or length is not 16!");
            return null;
        }
        try {
            byte[] enCodeFormat = privateKey.getBytes();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec("0102030405060708".getBytes()));
            return cipher.doFinal(content);
        } catch (Exception e) {
            Logger.e("AES Decrypt Exception, " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * AES加密
     *
     * @param content    需要加密的字符串
     * @param privateKey 密钥
     * @return 加密后的字符串
     */
    public static String encryptStr(String content, String privateKey) {
        byte[] result = encrypt(content, privateKey);
        if (result == null) {
            return "";
        }
        return parseByte2HexStr(result);
    }

    /**
     * AES解密
     *
     * @param content    需要解密的字符串
     * @param privateKey 密钥
     * @return 解密后的字符串
     */
    public static String decryptStr(String content, String privateKey) {
        byte[] byteContent = parseHexStr2Byte(content);
        byte[] result = decrypt(byteContent, privateKey);
        if (result == null) {
            return "";
        } else {
            return new String(result);
        }
    }

    /**
     * byte[] 转换为字符串
     *
     * @param buf 需要转换的byte[]
     * @return 转换后的字符串
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuilder sb = new StringBuilder();
        for (byte aBuf : buf) {
            String hex = Integer.toHexString(aBuf & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 字符串转化为byte[]
     *
     * @param hexStr 需要转换的字符串
     * @return 转换后的byte[]
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

}

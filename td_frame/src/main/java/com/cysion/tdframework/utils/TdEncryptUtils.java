package com.cysion.tdframework.utils;

import android.os.Build;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by cysion.liu on 2016/6/20.
 * 加密工具类，目前提供：
 * 1--AES加密解密；
 * 2--MD5加密
 */
public class TdEncryptUtils {

    private static final String MD5 = "MD5";

    /**
     * MD5加密
     * @param string  要加密的字符串
     * @param encoding 字符编码
     * @return  密文
     * @throws Exception
     */
    public static String MD5encrypt(String string, String encoding) {
        if (string == null || encoding == null) {
            throw new IllegalArgumentException("no content");
        }

        byte[] hash = null;
        try {
            hash = MessageDigest.getInstance(MD5).digest(string.getBytes());
        } catch (NoSuchAlgorithmException aE) {
            aE.printStackTrace();
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }


    /**
     * AES加密
     *
     * @param seed      加密种子
     * @param cleartext 需要加密的文本
     * @return 加密后的字符串
     * @throws Exception
     */
    public static String AESencrypt(String seed, String cleartext) throws Exception {
        byte[] rawKey = getRawKey(seed.getBytes());
        byte[] result = encrypt(rawKey, cleartext.getBytes());
        return toHex(result);
    }


    /**
     * AES解密
     *
     * @param seed      加密种子
     * @param encrypted 需要解密的文本
     * @return 原文
     * @throws Exception
     */
    public static String AESdecrypt(String seed, String encrypted) throws Exception {
        byte[] rawKey = getRawKey(seed.getBytes());
        byte[] enc = toByte(encrypted);
        byte[] result = decrypt(rawKey, enc);
        return new String(result);
    }


    private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        //一个是API 17以下的获得方式，一个是17及以上版本
        SecureRandom sr = null;
        if (Build.VERSION.SDK_INT < 17) {
            sr = SecureRandom.getInstance("SHA1PRNG");
        } else {
            sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        }
        sr.setSeed(seed);
        kgen.init(128, sr); // 192 and 256 bits may not be available
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        return raw;
    }


    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }
}


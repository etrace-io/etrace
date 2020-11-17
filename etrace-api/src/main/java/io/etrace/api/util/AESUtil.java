package io.etrace.api.util;

import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

public class AESUtil {

    public static byte[] hex2byte(String strhex) {
        if (strhex == null) {
            return null;
        }
        int l = strhex.length();
        if (l % 2 == 1) {
            return null;
        }
        byte[] b = new byte[l / 2];
        for (int i = 0; i != l / 2; i++) {
            b[i] = (byte)Integer.parseInt(strhex.substring(i * 2, i * 2 + 2),
                16);
        }
        return b;
    }

    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }

    public static String aesEncrypt(String text, String password) throws Exception {
        if (StringUtils.isEmpty(text)) {
            throw new Exception("text must not be null");
        }
        if (StringUtils.isEmpty(password)) {
            throw new Exception("password must not be null");
        }
        byte[] encryptWithAES = AesUtils.encryptWithAES(text.getBytes(StandardCharsets.UTF_8), password);
        return byte2hex(encryptWithAES);

    }

    public static String aesDecrypt(String text, String password) throws Exception {
        if (StringUtils.isEmpty(text)) {
            throw new Exception("text must not be null");
        }
        if (StringUtils.isEmpty(password)) {
            throw new Exception("password must not be null");
        }
        byte[] bytes = AesUtils.decryptWithAES(hex2byte(text), password);
        return new String(bytes, StandardCharsets.UTF_8);

    }

}

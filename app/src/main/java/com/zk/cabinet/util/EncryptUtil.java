package com.zk.cabinet.util;

import android.text.TextUtils;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class EncryptUtil {

    public static byte[] desEncrypt (byte[] data, byte[] desKey, byte[] desVI) {
        byte[] result = null;

        try {
            // 密钥
            DESKeySpec desKeySpec = new DESKeySpec(desKey);
            // 向量
            IvParameterSpec ivSpec = new IvParameterSpec(desVI);
            AlgorithmParameterSpec paramSpec = ivSpec;

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secureKey = keyFactory.generateSecret(desKeySpec);
            // DES加密模式CBC
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");

            cipher.init(Cipher.ENCRYPT_MODE, secureKey, paramSpec);
            result = Base64.encode(cipher.doFinal(data), Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static byte[] desDecrypt (byte[] data, byte[] desKey, byte[] desVI) {
        byte[] result = null;

        try {
            // 密钥
            DESKeySpec desKeySpec = new DESKeySpec(desKey);
            // 向量
            IvParameterSpec ivSpec = new IvParameterSpec(desVI);
            AlgorithmParameterSpec paramSpec = ivSpec;

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secureKey = keyFactory.generateSecret(desKeySpec);
            // DES加密模式CBC
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, secureKey, paramSpec);

            result = cipher.doFinal(Base64.decode(data, Base64.DEFAULT));
//            result = Base64.encode(cipher.doFinal(data), Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}

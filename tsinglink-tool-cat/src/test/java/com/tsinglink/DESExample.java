package com.tsinglink;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;

public class DESExample {
    public static void main(String[] args) throws Exception {
        // 要加密的明文数据
        String plainText = "Enver20191210";
        System.out.println("明文: " + plainText);

        // 生成 DES 密钥
        SecretKey secretKey = generateDESKey();

        // 手动指定初始化向量（IV）
        byte[] ivBytes = "A888D6C1".getBytes(StandardCharsets.UTF_8);

        // 加密
        String encryptedText = encrypt(plainText, secretKey, ivBytes);
        System.out.println("加密后的密文: " + encryptedText);

        // 解密
        String decryptedText = decrypt(encryptedText, secretKey, ivBytes);
        System.out.println("解密后的明文: " + decryptedText);
    }

    // 生成 DES 密钥
    public static SecretKey generateDESKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
        keyGenerator.init(56); // DES 密钥长度为 56 位

        // 创建一个DESKeySpec对象
        // 创建一个DESKeySpec对象
        DESKeySpec desKeySpec = new DESKeySpec("E526A308".getBytes());

        // 创建密匙工厂
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("DES");

        // 将密码利用密匙工厂转换成密匙
        SecretKey securekey = secretKeyFactory.generateSecret(desKeySpec);
        return securekey;
    }

    // 加密方法
    public static String encrypt(String plainText, SecretKey secretKey, byte[] ivBytes) throws Exception {
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        byte[] plainBytes = plainText.getBytes("UTF-8");
        byte[] encryptedBytes = cipher.doFinal(plainBytes);

        // 将加密后的字节数组转换为 Base64 字符串，便于存储和传输
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // 解密方法
    public static String decrypt(String encryptedText, SecretKey secretKey, byte[] ivBytes) throws Exception {
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        // 将 Base64 字符串解码为字节数组
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // 将解密后的字节数组转换为字符串
        return new String(decryptedBytes, "UTF-8");
    }
}

package io.github.hqqich.cat.security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * RipeMD-160是基于Merkle-Damgard结构的加密哈希函数，它是比特币标准之一。
 * RipeMD-160是RTPEND算法的增强版本，RipeMD-160算法可以产生出160位的的哈希摘要。
 *
 */

public class RipeMD160Utils {
	/**
	 * @param src 明文
	 * @return
	 */
	public static String ripeMD160(String src) {
		try {
			// 注册BouncyCastle提供的通知类对象BouncyCastleProvider
			Security.addProvider(new BouncyCastleProvider());
			// 获取RipeMD160算法的"消息摘要对象"(加密对象)
			MessageDigest messageDigest = MessageDigest.getInstance("RipeMD160");
			// 更新原始数据
			messageDigest.update(src.getBytes());
			// 调用messageDigest.digest()获取信息摘要(加密)
			return new BigInteger(1, messageDigest.digest()).toString(16).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			System.err.println("计算RipeMD160消息摘要异常！");
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		String src = "春宵一刻值千金，花有清香月有阴；歌管楼台声细细，秋千院落夜沉沉！";
		System.out.println("原始信息为："+src);
		System.out.println("RipeMD-160消息摘要："+RipeMD160Utils.ripeMD160(src));

	}

}

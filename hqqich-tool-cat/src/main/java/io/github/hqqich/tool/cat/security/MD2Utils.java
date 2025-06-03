package io.github.hqqich.tool.cat.security;

import io.github.hqqich.cat.utils.StringByteHexUtils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * MD2消息摘要算法
 *
 * @author 鬼面书生
 *
 */
public class MD2Utils {
	/**
	 * @param src 明文
	 * @return
	 */
	public static String Md2(String src) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD2");
			return StringByteHexUtils.byteArray2HexString(digest.digest(src.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			System.err.println("计算MD2消息摘要异常！");
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		String src = "春宵一刻值千金，花有清香月有阴；歌管楼台声细细，秋千院落夜沉沉！";
		System.out.println("原始信息为："+src);
		System.out.println("MD2消息摘要："+MD2Utils.Md2(src));
	}

}

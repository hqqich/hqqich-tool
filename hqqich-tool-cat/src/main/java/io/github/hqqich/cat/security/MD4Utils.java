package io.github.hqqich.cat.security;

import io.github.hqqich.cat.utils.StringByteHexUtils;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD4Digest;

/**
 *
 * MD4摘要算法
 *
 * @author 鬼面书生
 *
 */
public class MD4Utils {
	/**
	 * @param src 明文
	 * @return
	 */
	public static String Md4(String src) {
		// 获取消息摘要MD4对象
		Digest digest = new MD4Digest();
		digest.update(src.getBytes(), 0, src.getBytes().length);
		byte[] md4Byte = new byte[digest.getDigestSize()];
		digest.doFinal(md4Byte, 0);
		return StringByteHexUtils.byteArray2HexString(md4Byte);
	}

	public static void main(String[] args) {
		String src = "春宵一刻值千金，花有清香月有阴；歌管楼台声细细，秋千院落夜沉沉！";
		System.out.println("原始信息为："+src);
		System.out.println("MD4消息摘要 : " + MD4Utils.Md4(src));

	}

}

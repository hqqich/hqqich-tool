package io.github.hqqich.cat.security;

import io.github.hqqich.cat.utils.StringByteHexUtils;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;


/**
 *
 * DES加密说明<br>
 * DES是一种对称加密算法，对称加密即：加密和解密使用相同密钥的算法。<br>
 * 注意：DES加密和解密过程中，密钥长度必须是8的倍数;<br>
 */
public class DESUtils {

	/**
	 * 加密过程
	 *
	 * @param src
	 *            原始信息
	 * @param password
	 *            密码
	 * @return
	 */
	public static byte[] encrypt(String src, String password) {

		try {
			// DES算法要求有一个可信任的随机数源
			SecureRandom secureRandom = new SecureRandom();

			// 创建一个DESKeySpec对象
			DESKeySpec desKeySpec = new DESKeySpec(password.getBytes());

			// 创建密匙工厂
			SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("DES");

			// 将密码利用密匙工厂转换成密匙
			SecretKey securekey = secretKeyFactory.generateSecret(desKeySpec);

			// 创建Cipher对象，用于完成实际加密操作
			Cipher cipher = Cipher.getInstance("DES");

			// 用密匙初始化Cipher对象
			cipher.init(Cipher.ENCRYPT_MODE, securekey, secureRandom);

			// 执行加密操作并返回密文
			return cipher.doFinal(src.getBytes());

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return null;

	}

	/**
	 *
	 * 解密
	 *
	 * @param src
	 *            密文字节数组 byte[]
	 *
	 * @param password
	 *            密码 String
	 *
	 * @return byte[]
	 *
	 * @throws Exception
	 *
	 */

	public static byte[] decrypt(byte[] src, String password) throws Exception {

		// DES算法要求有一个可信任的随机数源
		SecureRandom random = new SecureRandom();

		// 创建一个DESKeySpec对象
		DESKeySpec desKey = new DESKeySpec(password.getBytes());

		// 创建一个密匙工厂
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

		// 将DESKeySpec对象转换成SecretKey对象
		SecretKey securekey = keyFactory.generateSecret(desKey);

		// Cipher对象，用于完成实际解密操作
		Cipher cipher = Cipher.getInstance("DES");

		// 用密匙初始化Cipher对象
		cipher.init(Cipher.DECRYPT_MODE, securekey, random);

		// 开始解密
		return cipher.doFinal(src);

	}

	public static void main(String[] args) throws Exception {
		// 待加密内容
		String srcStr = "Enver20191210";
		// 密码，长度必须是8的倍数
		String password = "E526A308";
		byte[] encriptMsg = DESUtils.encrypt(srcStr, password);
		String enc = new String(encriptMsg);
		System.out.println("明文信息：" + srcStr);
		System.out.println("DES加密后(byte数组)：" + encriptMsg);
		System.out.println("DES加密后密文：" + enc);
		String bytesToHexStr = StringByteHexUtils.bytesToHex(encriptMsg);
		System.out.println("DES加密后字节数组转16进制：" + bytesToHexStr);

		byte[] b = StringByteHexUtils.hexToByteArray(bytesToHexStr);
		System.out.println("16进制转换成字节数组：" + b);
		System.out.println("16进制转换成字节数组后的密文：" + new String(b));

		// 将加密后的byte数据再进行base64加密，生成字符串。
		String encoded = Base64.getEncoder().encodeToString(encriptMsg);
		System.out.println("BASE64对DES加密后的字节数组再次加密后的密文：" + encoded);

		// 将base64加密后生成的字符串解密，还原成byte数组(密文)，供DES解密用
		byte[] decoded = Base64.getDecoder().decode(encoded);
		System.out.println("BASE64解密后的字节数组：" + decoded);
		System.out.println("BASE64解密后数组转换成的密码：" + new String(decoded));

		byte[] decryResult = DESUtils.decrypt(b, password);
		System.out.println("解密后：" + new String(decryResult));
		System.out.println("原始加密后密文与base64解密密文比较：" + enc.equals(new String(decoded)));
		byte[] _decryResult = DESUtils.decrypt(decoded, password);
		System.out.println("DES利用密码串解密后：" + new String(_decryResult));
		/*//利用密码串password进行加密
		byte[] encriptMsg = DESUtils.encrypt(srcStr, password);
		System.out.println("DES加密前的明文内容：" + srcStr);
		System.out.println("DES加密所用到的密码串：" + password);
		System.out.println("DES加密后密文：" + new String(encriptMsg));

		//利用密码password进行解密
		byte[] decryResult = DESUtils.decrypt(encriptMsg, password);
		System.out.println("DES利用密码串解密后：" + new String(decryResult));*/

	}

}

package com.tsinglink.cat.utils;

/**
 * 字符串、字节、进制转换
 * 
 * @author 鬼面书生
 *
 */
public class StringByteHexUtils {
	// 十六进制位
	public static char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	/**
	 * 字节数组转16进制
	 * 
	 * @param bytes 需要转换的byte数组
	 * @return 转换后的Hex字符串
	 */
	public static String bytesToHex(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String hexStr = Integer.toHexString(bytes[i] & 0xFF);
			if (hexStr.length() < 2) {
				sb.append(0);
			}
			sb.append(hexStr);
		}
		return sb.toString();
	}

	/**
	 * 十六进制字符串转byte数组
	 * 
	 * @param hexStr 待转换的Hex字符串
	 * @return 转换后的byte数组结果
	 */
	public static byte[] hexToByteArray(String hexStr) {
		int hexLength = hexStr.length();
		byte[] result;
		if (hexLength % 2 == 1) {
			// 奇数
			hexLength++;
			result = new byte[(hexLength / 2)];
			hexStr = "0" + hexStr;
		} else {
			// 偶数
			result = new byte[(hexLength / 2)];
		}
		int j = 0;
		for (int i = 0; i < hexLength; i += 2) {
			result[j] = hexToByte(hexStr.substring(i, i + 2));
			j++;
		}
		return result;
	}

	/**
	 * 十六进制字符串转字节
	 * 
	 * @param hexStr 待转换的Hex字符串
	 * @return 转换后的byte
	 */
	public static byte hexToByte(String hexStr) {
		return (byte) Integer.parseInt(hexStr, 16);
	}

	/**
	 * 字符串转换unicode
	 */
	public static String string2Unicode(String string) {
		StringBuffer unicode = new StringBuffer();
		for (int i = 0; i < string.length(); i++) {
			// 取出每一个字符
			char c = string.charAt(i);
			// 转换为unicode
			unicode.append("\\u" + Integer.toHexString(c));
		}
		return unicode.toString();
	}

	/**
	 * unicode字符串转16进制字符串
	 * 
	 * @param s
	 * @return
	 */
	public static String unicodeStrTo16(String s) {
		String str = "";
		for (int i = 0; i < s.length(); i++) {
			int ch = (int) s.charAt(i);
			str += Integer.toHexString(ch);
		}
		return str;
	}

	public static byte[] toByteArray(int i) {
		byte[] byteArray = new byte[4];
		byteArray[0] = (byte) (i >>> 24);
		byteArray[1] = (byte) ((i & 0xFFFFFF) >>> 16);
		byteArray[2] = (byte) ((i & 0xFFFF) >>> 8);
		byteArray[3] = (byte) (i & 0xFF);
		return byteArray;
	}

	/**
	  * 字节转16进制
	 * 
	 * @param b
	 * @return
	 */
	private static String byteToHexString(byte b) {
		int n = b;
		if (n < 0)
			n = 256 + n;
		int d1 = n / 16;
		int d2 = n % 16;
		return "" + hexDigits[d1] + hexDigits[d2];
	}

	/**
	  * 字节数组转16进制
	 * 
	 * @param b
	 * @return
	 */
	public static String byteArrayToHexString(byte[] b) {
		StringBuffer resultSb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			resultSb.append(byteToHexString(b[i]));
		}
		return resultSb.toString();
	}
	
	/**
	 * 字节数组转16进制
	 * 
	 * @param b
	 */
	public static String byteArray2HexString(byte[] b) {
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			str.append(hex.toUpperCase());
		}
		return str.toString();
	}

	public static byte[] long2bytes(long l) {
		byte[] bytes = new byte[8];
		for (int i = 0; i < 8; i++) {
			bytes[i] = (byte) (l >>> ((7 - i) * 8));
		}
		return bytes;
	}
}

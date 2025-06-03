package io.github.hqqich.tool.cat.security;

import io.github.hqqich.cat.utils.StringByteHexUtils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SHAUtils {

	public static void sha1(String message) {
		try {
			MessageDigest sha1=MessageDigest.getInstance("SHA");
			byte[] shaEncode=sha1.digest(message.getBytes());
			String finalShaEncode= StringByteHexUtils.bytesToHex(shaEncode);
			System.out.println(message+"  的SHA1摘要："+finalShaEncode);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void sha256(String message) {
		try {
			MessageDigest sha256=MessageDigest.getInstance("SHA-256");
			byte[] sha256Encode=sha256.digest(message.getBytes());
			String finalShaEncode=StringByteHexUtils.bytesToHex(sha256Encode);
			System.out.println(message+"  的SHA256摘要："+finalShaEncode);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void sha384(String message) {
		try {
			MessageDigest sha384=MessageDigest.getInstance("SHA-384");
			byte[] sha384Encode=sha384.digest(message.getBytes());
			String finalShaEncode=StringByteHexUtils.bytesToHex(sha384Encode);
			System.out.println(message+"  的SHA384摘要："+finalShaEncode);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void sha512(String message) {
		try {
			MessageDigest sha512=MessageDigest.getInstance("SHA-512");
			byte[] sha512Encode=sha512.digest(message.getBytes());
			String finalShaEncode=StringByteHexUtils.bytesToHex(sha512Encode);
			System.out.println(message+"  的SHA512摘要："+finalShaEncode);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		SHAUtils.sha1("春宵一刻值千金，花有清香月有阴；歌管楼台声细细，秋千院落夜沉沉！");
		SHAUtils.sha256("春宵一刻值千金，花有清香月有阴；歌管楼台声细细，秋千院落夜沉沉！");
		SHAUtils.sha384("春宵一刻值千金，花有清香月有阴；歌管楼台声细细，秋千院落夜沉沉！");
		SHAUtils.sha512("春宵一刻值千金，花有清香月有阴；歌管楼台声细细，秋千院落夜沉沉！");
	}

}

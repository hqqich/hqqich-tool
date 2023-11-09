package com.tsinglink;


import com.tsinglink.cat.security.SM4Utils;
import java.nio.charset.StandardCharsets;
import org.bouncycastle.util.encoders.Hex;

public class Sm4Test {

	public static void main(String[] args) throws Exception {
		String key = "7202c1719c59c3f6520c551f6ea2ef9f";
		String privateKey = "0x23d5b2a2eb0a9c8b86d62cbc3955cfd1fb26ec576ecc379f402d0f5d2b27a7bb";
		//System.out.println(SM4.ECB.encrypt());

		byte[] decode = Hex.decode(privateKey.replace("0x", ""));

		System.out.println(new String(decode));
		System.out.println(new String(decode, StandardCharsets.UTF_8));

		String s = new String(decode, StandardCharsets.UTF_8);
		byte[] bytes = s.getBytes();
		System.out.println(new String(Hex.encode(bytes)));

		System.out.println(SM4Utils.encryptByEcb(decode, key));

	}
}

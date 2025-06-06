package io.github.hqqich.tool.cat.utils;

import java.math.BigInteger;
import org.bouncycastle.math.ec.ECPoint;

/**
 * SM2密钥对Bean
 * @author 鬼面书生
 *
 */
public class SM2KeyPair {

	private final ECPoint publicKey;
	private final BigInteger privateKey;

	public SM2KeyPair(ECPoint publicKey, BigInteger privateKey) {
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	public ECPoint getPublicKey() {
		return publicKey;
	}

	public BigInteger getPrivateKey() {
		return privateKey;
	}

}

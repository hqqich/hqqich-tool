package io.github.hqqich.cat.security;

import io.github.hqqich.cat.utils.SM2KeyPair;
import io.github.hqqich.cat.utils.StringByteHexUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;


/**
 * SM2椭圆曲线参数加密与解密实现 包括 -签名,验签 -密钥交换 -公钥加密,私钥解密 SM2非对称加密的结果由C1,C2,C3三部分组成。
 * 其中C1是生成随机数的计算出的椭圆曲线点，C2是密文数据，C3是SM3的摘要值。 旧国密标准的结果是按C1C2C3顺序，新标准的是按C1C3C2顺序存放!
 *
 * @author 鬼面书生整理
 *
 */
@SuppressWarnings("deprecation")
public class SM2Utils {
	/**
	 * SM2椭圆曲线公钥密码算法推荐曲线参数:素数域256位椭圆曲线
	 *
	 * 椭圆曲线方程：y^2 = x^3 + a*x + b 曲线参数：a,b,n,p,Gx,Gy
	 *
	 * 要求满足不等式 4*a^3 + 27*b^2 ≠ 0
	 *
	 */
	private static final BigInteger a = new BigInteger(
			"FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC", 16);
	private static final BigInteger b = new BigInteger(
			"28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93", 16);
	private static final BigInteger n = new BigInteger(
			"FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123", 16);
	private static final BigInteger p = new BigInteger(
			"FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF", 16);
	private static final BigInteger gx = new BigInteger(
			"32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16);
	private static final BigInteger gy = new BigInteger(
			"BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16);

	// 初始常量，长度64位,可根据实际需要生成
	private static final BigInteger IV = new BigInteger(
			"2C98EEFD718C73C9CF4925CEF2CE6A878C7AFBEF126F97B2D2938C2498397A8B", 16);
	private static final int BYTEARRAY_OUTPUT_STREAM_SIZE = 32;
	private static final int DIGEST_LENGTH = 32;
	// 字符集
	private static final String CHARSET_ENCODING_UTF8 = "UTF8";
	// 0x80十进制为-128
	private static final byte[] START_POSITION = { (byte) 0x80 };
	// 0x00十进制为0
	private static final byte[] ZERO_POSITION = { (byte) 0x00 };
	// 常量Tj={79cc4519，其中0≤j≤15；7a879d8a，其中16≤j≤63}
	private static final Integer TJ_15 = Integer.valueOf("79cc4519", 16);
	private static final Integer TJ_63 = Integer.valueOf("7a879d8a", 16);

	private static ECDomainParameters eccDomainParameters;

	// 安全随机数
	private static SecureRandom random = new SecureRandom();
	private static ECCurve.Fp eCCurve;
	private static ECPoint ecPoint;
	static {
		eCCurve = new ECCurve.Fp(p, a, b);
		ecPoint = eCCurve.createPoint(gx, gy);
		eccDomainParameters = new ECDomainParameters(eCCurve, ecPoint, n);
	}

	/**
	 *
	 * 两个TJ常量，由TJ_15和TJ_63数值转换而来
	 *
	 * @param j
	 * @return
	 *
	 */
	private static int getTjValue(int j) {
		if (j >= 0 && j <= 15) {
			return TJ_15.intValue();
		} else if (j >= 16 && j <= 63) {
			return TJ_63.intValue();
		} else {
			throw new RuntimeException("数据无效");
		}
	}

	/**
	 *
	 * GG计算公式： GGj(X,Y,Z)={X^Y^Z 0≤j≤15；（X & Y）|（~X&Z）16≤j≤63},其中X，Y，Z为字节（32bit）
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @param j
	 * @return
	 *
	 */
	private static Integer GG(Integer x, Integer y, Integer z, int j) {
		if (j >= 0 && j <= 15) {
			return Integer.valueOf(x.intValue() ^ y.intValue() ^ z.intValue());
		} else if (j >= 16 && j <= 63) {
			return Integer.valueOf((x.intValue() & y.intValue()) | (~x.intValue() & z.intValue()));
		} else {
			throw new RuntimeException("数据无效");
		}
	}

	/**
	 * FF计算公式： FFj(X,Y,Z)={X^Y^Z 0≤j≤15；(X & Y)|(X&Z)|(Y&Z)
	 * 16≤j≤63}，其中X，Y，Z为字节（32bit）
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @param j
	 * @return
	 *
	 */
	private static Integer FF(Integer x, Integer y, Integer z, int j) {
		if (j >= 0 && j <= 15) {
			return Integer.valueOf(x.intValue() ^ y.intValue() ^ z.intValue());
		} else if (j >= 16 && j <= 63) {
			return Integer.valueOf(
					(x.intValue() & y.intValue()) | (x.intValue() & z.intValue()) | (y.intValue() & z.intValue()));
		} else {
			throw new RuntimeException("数据无效");
		}
	}

	private static byte[] toByteArray(int i) {
		byte[] byteArray = new byte[4];
		byteArray[0] = (byte) (i >>> 24);
		byteArray[1] = (byte) ((i & 0xFFFFFF) >>> 16);
		byteArray[2] = (byte) ((i & 0xFFFF) >>> 8);
		byteArray[3] = (byte) (i & 0xFF);
		return byteArray;
	}

	private static byte[] toByteArray(int a, int b, int c, int d, int e, int f, int g, int h) throws IOException {
		ByteArrayOutputStream baoStream = new ByteArrayOutputStream(BYTEARRAY_OUTPUT_STREAM_SIZE);
		baoStream.write(toByteArray(a));
		baoStream.write(toByteArray(b));
		baoStream.write(toByteArray(c));
		baoStream.write(toByteArray(d));
		baoStream.write(toByteArray(e));
		baoStream.write(toByteArray(f));
		baoStream.write(toByteArray(g));
		baoStream.write(toByteArray(h));
		return baoStream.toByteArray();
	}

	/**
	 * 以16进制打印字节数组
	 *
	 * @param b
	 */
	public static void printHexString(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			System.out.print(hex.toUpperCase());
		}
		System.out.println();
	}

	/**
	 * 随机数生成器 max属于[1, max-1]
	 *
	 * @param max
	 * @return
	 */
	private static BigInteger random(BigInteger max) {
		BigInteger r = new BigInteger(256, random);
		while (r.compareTo(max) >= 0) {
			r = new BigInteger(128, random);
		}
		return r;
	}

	/**
	 * 判断字节数组是否全0
	 *
	 * @param buffer
	 * @return
	 */
	private static boolean checkAllZero(byte[] buffer) {
		for (int i = 0; i < buffer.length; i++) {
			if (buffer[i] != 0)
				return false;
		}
		return true;
	}

	/**
	 * 公钥加密
	 *
	 * SM2非对称加密的结果由C1,C2,C3三部分组成。 其中C1是根据随机数计算出来的椭圆曲线点，C2是密文数据，C3是SM3的摘要值。
	 * 旧国密标准的结果是按C1C2C3顺序，新标准的是按C1C3C2顺序存放!
	 *
	 * @param srcStr    明文
	 * @param publicKey 公钥
	 * @param newStandard 是否采用新标准生成密文 true是，false否
	 * @return
	 */
	public static byte[] encrypt(String srcStr, ECPoint publicKey,boolean newStandard) {

		byte[] inputBuffer = srcStr.getBytes();

		// 椭圆曲线点
		byte[] C1;
		ECPoint kpb;
		// 私钥
		byte[] privateKey;
		do {
			// 加密第1步：产生随机数k，k取值范围[1, n-1]
			BigInteger k = random(n);

			// 加密第2步：根据随机数k,计算椭圆曲线点C1,计算公式：C1 = [k]G = (x1, y1)
			ECPoint eCPoint = ecPoint.multiply(k);
			C1 = eCPoint.getEncoded(false);

			// 加密第3步：计算椭圆曲线点ecPoint,计算公式：ecPoint = [h]Pb
			BigInteger h = eccDomainParameters.getH();
			if (h != null) {
				ECPoint ecPoint = publicKey.multiply(h);
				if (ecPoint.isInfinity())
					throw new IllegalStateException();
			}

			// 加密第4步： 利用公钥计算kpb,计算公式：[k]PB = (x2, y2)
			kpb = publicKey.multiply(k).normalize();

			// 加密第5步：生成私钥privateKey,计算公式privateKey = KDF(x2||y2, klen)
			byte[] kpbBytes = kpb.getEncoded(false);
			privateKey = KDF(kpbBytes, inputBuffer.length);

		} while (checkAllZero(privateKey));

		// 加密第6步：生成密文C2，计算公式：C2=M^t
		byte[] C2 = new byte[inputBuffer.length];
		for (int i = 0; i < inputBuffer.length; i++) {
			C2[i] = (byte) (inputBuffer[i] ^ privateKey[i]);
		}

		// 加密第7步：生成消息摘要C3，计算公式：C3 = Hash(x2 || M || y2)
		byte[] C3 = byteHash(kpb.getXCoord().toBigInteger().toByteArray(), inputBuffer,
				kpb.getYCoord().toBigInteger().toByteArray());

		// 加密第8步：生成最后的密文encryptResult，计算公式：encryptResult=C1 || C2 || C3
		// 加密结果由：根据随机数计算出的椭圆曲线点C1，密文数据C2，SM3的摘要值C3三个部分组成
		byte[] encryptResult = new byte[C1.length + C2.length + C3.length];
		if(newStandard) {
			// 新SM2标准存放顺序C1C3C2
			System.arraycopy(C1, 0, encryptResult, 0, C1.length);
			System.arraycopy(C3, 0, encryptResult, C1.length + C2.length, C3.length);
			System.arraycopy(C2, 0, encryptResult, C1.length, C2.length);
		}else {
			//旧SM2标准存放顺序C1C2C3
			 System.arraycopy(C1, 0, encryptResult, 0, C1.length);
			 System.arraycopy(C2, 0, encryptResult, C1.length, C2.length);
			 System.arraycopy(C3, 0, encryptResult, C1.length + C2.length, C3.length);
		}


		return encryptResult;
	}

	/**
	 * 私钥解密
	 *
	 * @param encryptData 密文数据字节数组
	 * @param privateKey  解密私钥
	 * @return
	 */
	public static String decrypt(byte[] encryptData, BigInteger privateKey) {

		byte[] C1 = new byte[65];
		System.arraycopy(encryptData, 0, C1, 0, C1.length);

		ECPoint ecPoint = eCCurve.decodePoint(C1).normalize();

		// 计算椭圆曲线点eCPoint，计算公式eCPoint= [h]ecPoint
		BigInteger h = eccDomainParameters.getH();
		if (h != null) {
			ECPoint _ecPoint = ecPoint.multiply(h);
			// 是否为无穷点
			if (_ecPoint.isInfinity())
				throw new IllegalStateException();
		}
		// 计算[dB]C1，公式：[dB]C1 = (x2, y2)
		ECPoint dBC1 = ecPoint.multiply(privateKey).normalize();

		// 生成密钥t，公式：t = KDF(x2 || y2, klen)
		byte[] dBC1Bytes = dBC1.getEncoded(false);
		int klen = encryptData.length - 65 - DIGEST_LENGTH;
		byte[] t = KDF(dBC1Bytes, klen);
		// 校验密钥是否为0
		if (checkAllZero(t)) {
			System.err.println("密钥全部为0！");
			throw new IllegalStateException();
		}

		// 计算明文Plaintext,公式：Plaintext=C2^t
		byte[] Plaintext = new byte[klen];
		for (int i = 0; i < Plaintext.length; i++) {
			Plaintext[i] = (byte) (encryptData[C1.length + i] ^ t[i]);
		}

		// 生成消息摘要messageDigest,计算公式：messageDigest= Hash(x2 || M' || y2) 再判断消息摘要与C3是否一致
		byte[] C3 = new byte[DIGEST_LENGTH];
		System.arraycopy(encryptData, encryptData.length - DIGEST_LENGTH, C3, 0, DIGEST_LENGTH);
		byte[] messageDigest = byteHash(dBC1.getXCoord().toBigInteger().toByteArray(), Plaintext,
				dBC1.getYCoord().toBigInteger().toByteArray());
		if (Arrays.equals(messageDigest, C3)) {
			try {
				return new String(Plaintext, CHARSET_ENCODING_UTF8);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return null;
		} else {
			System.err.println("数字签名验证失败，无法解密！");
			return null;
		}

	}

	/**
	 * 判断是否在范围内
	 *
	 * @param param
	 * @param min
	 * @param max
	 * @return
	 */
	private static boolean between(BigInteger param, BigInteger min, BigInteger max) {
		return param.compareTo(min) >= 0 && param.compareTo(max) < 0;
	}

	/**
	 * 判断生成的公钥是否合法(如果无穷大则不合法)
	 *
	 * @param publicKey
	 * @return
	 */
	private static boolean checkPublicKey(ECPoint publicKey) {
		if (!publicKey.isInfinity()) {
			BigInteger x = publicKey.getXCoord().toBigInteger();
			BigInteger y = publicKey.getYCoord().toBigInteger();
			if (between(x, new BigInteger("0"), p) && between(y, new BigInteger("0"), p)) {
				BigInteger xResult = x.pow(3).add(a.multiply(x)).add(b).mod(p);
				BigInteger yResult = y.pow(2).mod(p);
				if (yResult.equals(xResult) && publicKey.multiply(n).isInfinity()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 生成密钥对
	 *
	 * @return
	 */
	public static SM2KeyPair generateKeyPair() {

		BigInteger d = random(n.subtract(new BigInteger("1")));

		SM2KeyPair keyPair = new SM2KeyPair(ecPoint.multiply(d).normalize(), d);

		if (checkPublicKey(keyPair.getPublicKey())) {
			return keyPair;
		} else {
			return null;
		}
	}

	/**
	 * 导出公钥到本地
	 *
	 * @param publicKey
	 * @param path
	 */
	public static void exportPublicKey(ECPoint publicKey, String path) {
		File file = new File(path);
		try {
			if (!file.exists())
				file.createNewFile();
			byte buffer[] = publicKey.getEncoded(false);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(buffer);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从本地导入公钥
	 *
	 * @param keyPath
	 * @return
	 */
	public static ECPoint importPublicKey(String keyPath) {
		File file = new File(keyPath);
		try {
			if (!file.exists())
				return null;
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			byte buffer[] = new byte[16];
			int size;
			while ((size = fis.read(buffer)) != -1) {
				baos.write(buffer, 0, size);
			}
			fis.close();
			return eCCurve.decodePoint(baos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 导出私钥到本地
	 *
	 * @param privateKey
	 * @param keyPath
	 */
	public static void exportPrivateKey(BigInteger privateKey, String keyPath) {
		File file = new File(keyPath);
		try {
			if (!file.exists())
				file.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(privateKey);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从本地导入私钥
	 *
	 * @param path
	 * @return
	 */
	public static BigInteger importPrivateKey(String path) {
		File file = new File(path);
		try {
			if (!file.exists())
				return null;
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fis);
			BigInteger res = (BigInteger) (ois.readObject());
			ois.close();
			fis.close();
			return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 字节数组拼接
	 *
	 * @param params
	 * @return
	 */
	private static byte[] join(byte[]... params) {
		ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
		byte[] res = null;
		try {
			for (int i = 0; i < params.length; i++) {
				baoStream.write(params[i]);
			}
			res = baoStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * 生成消息摘要（与SM3算法相同）
	 *
	 * 方法：对长度为l(l<2^64)bit的消息m，进行填充和迭代压缩，生成杂凑值，最终的杂凑值为256bit。
	 *
	 * @param params
	 * @return
	 */
	private static byte[] byteHash(byte[]... params) {
		byte[] res = null;
		try {
			res = hash(join(params));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * 填充+迭代压缩
	 *
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public static byte[] hash(byte[] source) throws IOException {
		// 数据填充
		byte[] m1 = padding(source);
		int n = m1.length / (512 / 8);
		byte[] b;
		byte[] vi = IV.toByteArray();
		byte[] vi1 = null;
		// 迭代压缩
		for (int i = 0; i < n; i++) {
			b = Arrays.copyOfRange(m1, i * 64, (i + 1) * 64);
			vi1 = CF(vi, b);
			vi = vi1;
		}
		return vi1;
	}

	/**
	 * CF为压缩方法
	 *
	 * @param vi
	 * @param bi
	 * @return
	 * @throws IOException
	 */
	private static byte[] CF(byte[] vi, byte[] bi) throws IOException {
		int a, b, c, d, e, f, g, h;
		a = toInteger(vi, 0);
		b = toInteger(vi, 1);
		c = toInteger(vi, 2);
		d = toInteger(vi, 3);
		e = toInteger(vi, 4);
		f = toInteger(vi, 5);
		g = toInteger(vi, 6);
		h = toInteger(vi, 7);

		int[] w = new int[68];
		int[] w1 = new int[64];
		for (int i = 0; i < 16; i++) {
			w[i] = toInteger(bi, i);
		}
		for (int j = 16; j < 68; j++) {
			w[j] = P1(w[j - 16] ^ w[j - 9] ^ Integer.rotateLeft(w[j - 3], 15)) ^ Integer.rotateLeft(w[j - 13], 7)
					^ w[j - 6];
		}
		for (int j = 0; j < 64; j++) {
			w1[j] = w[j] ^ w[j + 4];
		}
		int ss1, ss2, tt1, tt2;
		for (int j = 0; j < 64; j++) {
			ss1 = Integer.rotateLeft(Integer.rotateLeft(a, 12) + e + Integer.rotateLeft(getTjValue(j), j), 7);
			ss2 = ss1 ^ Integer.rotateLeft(a, 12);
			tt1 = FF(a, b, c, j) + d + ss2 + w1[j];
			tt2 = GG(e, f, g, j) + h + ss1 + w[j];
			d = c;
			c = Integer.rotateLeft(b, 9);
			b = a;
			a = tt1;
			h = g;
			g = Integer.rotateLeft(f, 19);
			f = e;
			e = P0(tt2);
		}
		byte[] v = toByteArray(a, b, c, d, e, f, g, h);
		for (int i = 0; i < v.length; i++) {
			v[i] = (byte) (v[i] ^ vi[i]);
		}
		return v;
	}

	private static Integer P0(Integer x) {
		return Integer
				.valueOf(x.intValue() ^ Integer.rotateLeft(x.intValue(), 9) ^ Integer.rotateLeft(x.intValue(), 17));
	}

	private static Integer P1(Integer x) {
		return Integer
				.valueOf(x.intValue() ^ Integer.rotateLeft(x.intValue(), 15) ^ Integer.rotateLeft(x.intValue(), 23));
	}

	private static int toInteger(byte[] source, int index) {
		StringBuilder valueStr = new StringBuilder("");
		for (int i = 0; i < 4; i++) {
			valueStr.append(StringByteHexUtils.hexDigits[(byte) ((source[index * 4 + i] & 0xF0) >> 4)]);
			valueStr.append(StringByteHexUtils.hexDigits[(byte) (source[index * 4 + i] & 0x0F)]);
		}
		return Long.valueOf(valueStr.toString(), 16).intValue();

	}

	/**
	 * 数据填充 设消息m的长度为l bit，首先将bit"1"添加到消息末尾，再加k个“0”，k是满足l+1+k =448 mod
	 * 512的最小非负整数。然后再添加一个64bit串，该串是l的二进制表示，填充后的消息m'长度为512的整数倍。
	 *
	 * @param source
	 * @return
	 * @throws IOException
	 */
	private static byte[] padding(byte[] source) throws IOException {
		if (source.length >= 0x2000000000000000l) {
			throw new RuntimeException("src data invalid.");
		}
		long l = source.length * 8;
		long k = 448 - (l + 1) % 512;
		if (k < 0) {
			k = k + 512;
		}
		ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
		baoStream.write(source);
		baoStream.write(START_POSITION);
		long i = k - 7;
		while (i > 0) {
			baoStream.write(ZERO_POSITION);
			i -= 8;
		}
		baoStream.write(StringByteHexUtils.long2bytes(l));
		return baoStream.toByteArray();
	}

	/**
	 * 获取用户标识字节数组
	 *
	 * @param IDA
	 * @param publicKey
	 * @return
	 */
	private static byte[] ZA(String IDA, ECPoint publicKey) {
		byte[] idaBytes = IDA.getBytes();
		int entlenA = idaBytes.length * 8;
		byte[] ENTLA = new byte[] { (byte) (entlenA & 0xFF00), (byte) (entlenA & 0x00FF) };
		return byteHash(ENTLA, idaBytes, a.toByteArray(), b.toByteArray(), gx.toByteArray(), gy.toByteArray(),
				publicKey.getXCoord().toBigInteger().toByteArray(), publicKey.getYCoord().toBigInteger().toByteArray());
	}

	/**
	 * 签名
	 *
	 * @param M       签名信息
	 * @param IDA     签名方唯一标识
	 * @param keyPair 签名方密钥对
	 * @return 签名
	 */
	public static Signature signature(String M, String IDA, SM2KeyPair keyPair) {
		byte[] ZA = ZA(IDA, keyPair.getPublicKey());
		byte[] _M = join(ZA, M.getBytes());
		BigInteger e = new BigInteger(1, byteHash(_M));
		BigInteger k;
		BigInteger r;
		do {
			k = random(n);
			ECPoint p1 = ecPoint.multiply(k).normalize();
			BigInteger x1 = p1.getXCoord().toBigInteger();
			r = e.add(x1);
			r = r.mod(n);
		} while (r.equals(BigInteger.ZERO) || r.add(k).equals(n));

		BigInteger s = ((keyPair.getPrivateKey().add(BigInteger.ONE).modInverse(n))
				.multiply((k.subtract(r.multiply(keyPair.getPrivateKey()))).mod(n))).mod(n);

		return new Signature(r, s);
	}

	/**
	 * 签名验证
	 *
	 * @param M          签名信息
	 * @param signature  签名
	 * @param IDA        签名方唯一标识
	 * @param aPublicKey 签名方公钥
	 * @return true or false
	 */
	public static boolean verifySignature(String M, Signature signature, String IDA, ECPoint aPublicKey) {
		if (!between(signature.r, BigInteger.ONE, n))
			return false;
		if (!between(signature.s, BigInteger.ONE, n))
			return false;

		byte[] M_ = join(ZA(IDA, aPublicKey), M.getBytes());
		BigInteger e = new BigInteger(1, byteHash(M_));
		BigInteger t = signature.r.add(signature.s).mod(n);

		if (t.equals(BigInteger.ZERO))
			return false;

		ECPoint p1 = ecPoint.multiply(signature.s).normalize();
		ECPoint p2 = aPublicKey.multiply(t).normalize();
		BigInteger x1 = p1.add(p2).normalize().getXCoord().toBigInteger();
		BigInteger R = e.add(x1).mod(n);
		if (R.equals(signature.r))
			return true;
		return false;
	}

	/**
	 * 密钥生成
	 *
	 * @param Z
	 * @param klen 生成klen字节数长度的密钥
	 * @return
	 */
	private static byte[] KDF(byte[] Z, int klen) {
		int ct = 1;
		int end = (int) Math.ceil(klen * 1.0 / 32);
		ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
		try {
			for (int i = 1; i < end; i++) {
				baoStream.write(byteHash(Z, toByteArray(ct)));
				ct++;
			}
			byte[] last = byteHash(Z, toByteArray(ct));
			if (klen % 32 == 0) {
				baoStream.write(last);
			} else
				baoStream.write(last, 0, klen % 32);
			return baoStream.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 公钥字符串转换为公钥对象
	 * @param publicKey 16进制公钥字符串
	 * @return
	 */
	public static ECPoint publicKeyStr2ECPoint(String publicKey) {
		byte[] _publicKeyByte=StringByteHexUtils.hexToByteArray(publicKey);
		return eCCurve.decodePoint(_publicKeyByte);
	}

	/**
	 * 公钥对象转公钥字符串(16进制)
	 * @param publicKey
	 * @param endCompression 是否末端压缩:true是，false否
	 * @return
	 */
	public static String eCPoint2PublicKeyStr(ECPoint publicKey,boolean endCompression) {
		return StringByteHexUtils.byteArray2HexString(publicKey.getEncoded(endCompression));
	}

	public static class Signature {
		BigInteger r;
		BigInteger s;

		public Signature(BigInteger r, BigInteger s) {
			this.r = r;
			this.s = s;
		}

		@Override
		public String toString() {
			return r.toString(16) + "," + s.toString(16);
		}
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		// 生成密钥对
		SM2KeyPair keyPair = SM2Utils.generateKeyPair();
		ECPoint publicKey = keyPair.getPublicKey();
		BigInteger privateKey = keyPair.getPrivateKey();
		// 导出公钥
		SM2Utils.exportPublicKey(publicKey, "D:/publickey.pem");
		// 导出私钥
		SM2Utils.exportPrivateKey(privateKey, "D:/privatekey.pem");
		// 原始信息
		String srcStr = "春宵一刻值千金，花有清香月有阴；歌管楼台声细细，秋千院落夜沉沉！";
		// 导入公钥
		ECPoint importPublicKey = SM2Utils.importPublicKey("D:/publickey.pem");
		// 导入私钥
		BigInteger importPrivateKey = SM2Utils.importPrivateKey("D:/privatekey.pem");
		String publicKstr=SM2Utils.eCPoint2PublicKeyStr(importPublicKey, false);
		System.out.println("公钥："+importPublicKey);
		System.out.println("公钥转公钥字符串："+publicKstr);
		System.out.println("公钥字符串转公钥："+SM2Utils.publicKeyStr2ECPoint(publicKstr));
		System.out.println("私钥："+privateKey);
		// 利用导入的公钥进行加密
		byte[] encryptData = SM2Utils.encrypt(srcStr, importPublicKey,false);
		System.out.println("原始信息（明文）:" + srcStr);
		//密文字节数组转字符串（密文字符串）
		String encryptStr=StringByteHexUtils.bytesToHex(encryptData);
		System.out.println("利用导入的公钥进行加密后的密文:");
		System.out.println(encryptStr);
		//密文字符串转密文字节数组
		byte[] _encryptData=StringByteHexUtils.hexToByteArray(encryptStr);
		// 利用导入的私钥进行解密
		System.out.println("解密后的明文:" + SM2Utils.decrypt(_encryptData, importPrivateKey));

		System.out.println("#####################签 名 与 验 证###################");
		//IDA为签名方唯一标识
		String IDA = "鬼面书生";
		String signatureMessage = "需要进行签名的信息";
		//进行签名
		Signature signature = SM2Utils.signature(signatureMessage, IDA, new SM2KeyPair(publicKey, privateKey));
		System.out.println("用户标识:" + IDA);
		System.out.println("签名信息:" + signatureMessage);
		System.out.println("数字签名:" + signature);
		System.out.println("验证签名:" + SM2Utils.verifySignature(signatureMessage, signature, IDA, publicKey));

	}

}

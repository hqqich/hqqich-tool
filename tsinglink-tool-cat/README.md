# cat

## 一、介绍  

> cat:全称 `Cryptographic Algorithm Tool`

主要提供以下加密算法调用：  
1.AES  
2.DES  
3.DSA
4.MD5  
5.MersenneTwister(梅森旋转算法)  
6.RSA  
7.国密SM2
8.国密SM3
9.国密SM4
10.DH
11.MD2
12.MD4
13.RipeMD-160
其他算法敬请期待。

## 二、软件架构及说明  

1.本工具包基于纯java编写而成；  
2.部分算法密钥对基于bcprov-jdk16的调用生成。  
3.部分加密与解密算法来自网上公开的源码，并在此基础上进行整理和优化而来；  
4.本加密与解密包均在JDK1.8环境下编译并测试通过，建议所使用JAVA环境不低于JDK1.8；  
5.其他高版本的JDK（如JDK9、JDK10、JDK11及以上版本）请自行测试；


## 四、使用说明

### 1.AES

加密：   
AESUtils.ecodes("明文","密码","密钥长度");  
解密：  
AESUtils.decodes("密文","密码","密钥长度");  
加密示例：  
String cipherText = AESUtils.ecodes("一片春愁待酒浇，江上舟摇，楼上帘招。秋娘渡与泰娘桥，风又飘飘，雨又萧萧。", "1qaz@SX#EDC4rfv", 256);  
System.out.println("加密后密文: " + cipherText);  
解密示例：  
String clearText = AESUtils.decodes(cipherText, "1qaz@SX#EDC4rfv", 256);  
System.out.println("密文解密后的明文：" + clearText);  

### 2.DES

加密：  
byte[] encriptMsg = DESUtils.encrypt("明文", "密码");  
System.out.println("加密后密文: " + new String(encriptMsg));  
加密示例：  
//待加密内容  
String srcStr = "春宵一刻值千金，花有清香月有阴；歌管楼台声细细，秋千院落夜沉沉！";  
// 密码，长度必须是8的倍数  
String password = "ABCDEFGH12345678";  
byte[] encriptMsg = DESUtils.encrypt(srcStr, password);  
String enc = new String(encriptMsg);  
System.out.println("DES加密后(byte[])：" + encriptMsg);  
System.out.println("DES加密后密文：" + enc);  
注意：DES加密后的byte数组转换成字符串后是乱码字符（即上述enc是乱码），且无法反向还原成byte数组；  
解密：  
byte[] decryResult = DESUtils.decrypt("加密后的byte数组", "密码");  
System.out.println("解密后的明文：" + new String(decryResult));  
解密示例：   
byte[] decryResult = DESUtils.decrypt(encriptMsg, password);  
System.out.println("DES利用密码串解密后：" + new String(decryResult));  
---------------------------------------------------------------------------------------------------  
PS:为了方便管理密文，可将加密后生成的字节数组采用Base64进行二次加密后，密文不再是乱码，方便管理，示例如下：  
// 将加密后的byte数组再进行base64加密，生成字符串密文。  
String encoded = Base64.getEncoder().encodeToString(encriptMsg);  
System.out.println("BASE64对DES加密后的byte数组再次加密后的密文：" + encoded);

	在调用DES解密以前，先采集BASE64进行第一次解密，示例如下：  
	//将上述采用base64加密后生成的字符串进行Base64第一次解密，还原成byte数组(密文)，供DES解密用  
	byte[] decoded = Base64.getDecoder().decode(encoded);  
	System.out.println("BASE64解密后的字节数组：" + decoded);  
	System.out.println("BASE64解密后数组转换成的密文：" + new String(decoded));  
	BASE64解密后数组转换成的密文字符串以后与DES加密后的byte数组转换成字符串后的结果是一样的，还是乱码字符；  
	将BASE64解密后的字节数组调用DES解密函数进行二次解密：  
	byte[] _decryResult = DESUtils.decrypt(decoded, password);  
	System.out.println("Base64解密后的数组decoded利用密码串调用DES解密后：" + new String(_decryResult));  

### 3.DSA

//生成密钥对
KeyPair keyPair = DSAUtils.initKey();
//提取出公钥和私钥
byte[] keyPairPrivate = keyPair.getPrivate().getEncoded();
byte[] keyPairPublic = keyPair.getPublic().getEncoded();
//algorithm.getName()包含：SHA1withDSA, SHA224withDSA, SHA256withDSA;
byte[] signed = DSAUtils.sign(str.getBytes(), keyPairPrivate, algorithm.getName());
//签名
System.out.println("签名：" + Base64.getEncoder().encodeToString(signed));
//签名验证
boolean verify = DSAUtils.verify(str.getBytes(), keyPairPublic, signed, algorithm.getName());
System.out.println("验签：" + verify);

### 4.MD5

MD5算法为摘要算法，加密过程不可逆，本工具中MD5加密后生成的密文有两种：一种是长度为32位；一种是长度为16位；  
默认密文长度为32位，  
MD5Utils.Md5("原始信息", 密文长度);  
示例如下：  
String srcStr = "春宵一刻值千金，花有清香月有阴；歌管楼台声细细，秋千院落夜沉沉！";  
System.out.println("原始信息：" + srcStr);  
System.out.println("MD5加密后(密文长度16位)：" + MD5Utils.Md5(srcStr, 16));  
System.out.println("MD5加密后(密文长度32位)：" + MD5Utils.Md5(srcStr, 32));

### 5.MersenneTwister(梅森旋转算法)

参看代码中main方法   
梅森旋转算法（Mersenne twister）是一个伪随机数发生算法。由松本真和西村拓士在1997年开发，
基于有限二进制字段上的矩阵线性递归。可以快速产生高质量的伪随机数，修正了古典随机数发生算法的很多缺陷。
整个算法主要分为三个阶段：
第一阶段：获得基础的梅森旋转链；
第二阶段：对于旋转链进行旋转算法；
第三阶段：对于旋转算法所得的结果进行处理；
算法实现的过程中，参数的选取取决于梅森素数，故此得名。

### 6.RSA

RSA为非对称加密码算法，其密钥对（公钥与私钥）由RSA算法自动生成；  
加密与解密过程：  
a.调用密钥对生成函数，生成密钥对；  
KeyPair keyPair = RSAUtils.generateKeyPair(4196);  
b.利用密钥对中的公钥进行加密；  
//从密钥对中获取公钥  
Key publicKey=keyPair.getPublic();  
String encriptMsg = RSAUtils.encrypt("待加密的明文", 公钥publicKey);  
encriptMsg即为最终加密后的结果；  
c.解密时利用私钥进行解密；  
//从密钥对中获取私钥  
Key priateKey=keyPair.getPrivate();  
String decryptMsg=RSAUtils.decrypt("待解密的密文", 私钥privateKey)

完整示例：
String srcStr = "春宵一刻值千金，花有清香月有阴；歌管楼台声细细，秋千院落夜沉沉！";  
//获取非对称密钥  
KeyPair keyPair = RSAUtils.generateKeyPair(4196);   
//对公钥与私钥进行base64编码，方便密钥对的管理，源代码中的方法为RSAUtils.keyEncrypt(公钥或私钥)  
System.out.println("公钥：" +new String(Base64.encodeBase64(keyPair.getPublic().getEncoded())));  
System.out.println("私钥：" +new String(Base64.encodeBase64(keyPair.getPrivate().getEncoded())));  
//加密：  
String encriptMsg = RSAUtils.encrypt(srcStr, keyPair.getPublic());  
System.out.println("原始信息：" + srcStr + " 加密后的密文为："+encriptMsg);  
//解密  
String decryptMsg= RSAUtils.decrypt(encriptMsg, keyPair.getPrivate());  
System.out.println("密文解密明文为：" +decryptMsg);  

### 7.国密SM2

国密码SM2算法采用椭圆曲线公钥密码算法，属于非对称加密算法；
加密的结果由C1,C2,C3三部分组成，其中C1是生成随机数的计算出的椭圆曲线点，C2是密文数据，C3是SM3的摘要值。
旧国密标准的结果是按C1C2C3顺序，新标准的是按C1C3C2顺序存放!
完整使用示例：
String srcStr="春宵一刻值千金，花有清香月有阴；歌管楼台声细细，秋千院落夜沉沉！";  
System.out.println("原始信息："+srcStr);  
//生成密钥对
SM2KeyPair keyPair=SM2Utils.generateKeyPair();
ECPoint publicKey =keyPair.getPublicKey();
System.out.println("公钥："+publicKey.toString());
BigInteger privateKey=keyPair.getPrivateKey();
System.out.println("密钥："+privateKey.toString());
byte[] data = SM2Utils.encrypt(srcStr, publicKey);
System.out.print("密文：");
SM2Utils.printHexString(data);
System.out.println("解密后明文：" + SM2Utils.decrypt(data, privateKey));
//导出公钥
SM2Utils.exportPublicKey(publicKey,"密钥保存路径/publickey.pem");
//导出私钥
SM2Utils.exportPrivateKey(privateKey, "密钥保存路径/privatekey.pem");
//导入公钥
ECPoint importPublicKey =SM2Utils.importPublicKey("密钥保存路径/publickey.pem");
//导入私钥
BigInteger importPrivateKey = SM2Utils.importPrivateKey("密钥保存路径/privatekey.pem");

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

8.国密SM3  
国密SM3算法为杂凑算法，是国家密码局公布并推荐使用的一种单向加密算法，其加密过程不可逆。  
本工具包提供带自定义密钥的加密和不带密钥的加密两种加密模式，其中带自定义密钥的加密中，密钥支持中文字符；  
两种加密模式加密出来的密文均提供简单的签名验证功能 (即验签，验证密文是否由明文通过指定密钥加密所得)，验签    
函数 SM3Utils.verify("明文","密钥","密文"),验签函数返回true（验签通过）或false（验签未通过）；  
两种加密模式使用说明如下：  
a.自定义密钥的加密  
SM3Utils.encrypt("明文", "密钥字符(可以是中文)");  
示例：  
//明文  
String srcStr = "春宵一刻值千金，花有清香月有阴；歌管楼台声细细，秋千院落夜沉沉！";  
//密钥  
String key = "春宵";  
//加密  
String hexStrByKey = SM3Utils.encrypt(srcStr, key);  
System.out.println("        带密钥加密后的密文：" + hexStrByKey);  
//验签  
boolean veryfy=SM3Utils.verify(srcStr, key, hexStrByKey);  
//验签结果  
System.out.println("明文(带密钥)与密文验签结果：" + veryfy);

b.不带密钥的加密  
SM3Utils.encrypt("明文");  
示例：  
//明文  
String srcStr = "春宵一刻值千金，花有清香月有阴；歌管楼台声细细，秋千院落夜沉沉！";  
//加密  
String hexStrNoKey = SM3Utils.encrypt(srcStr);  
System.out.println("        不带密钥加密后的密文：" + hexStrNoKey);  
//验签结果  
System.out.println("明文(不带密钥)与密文验签结果：" + SM3Utils.verify(srcStr, hexStrNoKey));  
9.国密SM4  
国密SM4是国家密码局公布的无线局域网产品使用的加密算法，算法本身为分组算法，该算法分组长度为128比特，密钥长度为128比特；  
加密算法与密钥扩展算法均采用32轮非线性迭代结构，解密算法结构与加密算法结构完全相同，只是密钥轮的使用顺序相反，解密轮密
钥是加密轮密钥的逆序。  
本工具包提供带自定义密钥的加密和系统自动生成密钥的加密两种加密模式和简单的验签功能，这两种模式调用的加密过程完全一致，
需要注意的是，当采用自定义密钥加密时，自定义密码必须是大写的32位的16进制密钥（其他长度的密钥和小写密钥可自行测试）；  
加密过程：  
SM4Utils.encryptByEcb("明文", "密钥")；  
解密过程：  
SM4Utils.decryptEcb("密钥", "密文");  
验签(验证密文是否由明文通过指定密钥加密所得)过程：  
SM4Utils.verifyByEcb("密钥", "密文", "明文");

    完整示例：  
String srcStr = "春宵一刻值千金，花有清香月有阴；歌管楼台声细细，秋千院落夜沉沉！";  
// 自定义的32位16进制密钥  
// String key = "86C63180C2806ED1F47B859DE501215B";  
// 自动生成密钥  
String key = ByteUtils.toHexString(autoGenerateKey(DEFAULT_KEY_SIZE));  
// 加密，key可采用自定义的32位16进制密钥，也可以调用autoGenerateKey生成  
String cipher = SM4Utils.encryptByEcb(srcStr, key);  
System.out.println("自动生成的密钥：" + key);  
// 密文输出  
System.out.println("加密后的密文：" + cipher);  
//验签  
System.out.println("校验密文是否为明文加密所得：" + SM4Utils.verifyByEcb(key, cipher, srcStr));  
// 解密  
srcStr = SM4Utils.decryptEcb(key, cipher);  
System.out.println("采用密钥：" + key);  
System.out.println("解密后的明文：" + srcStr);

10.DH  
DH，全称为“Diffie-Hellman”，是一种确保共享KEY安全穿越不安全网络的方法，即常说的密钥一致协议;  
原理是由甲方产出一对密钥（公钥、私钥），乙方依照甲方公钥产生乙方密钥对（公钥、私钥）。  
以此为基线，作为数据传输保密基础，同时双方使用同一种对称加密算法构建本地密钥（SecretKey）对数据加密。
在互通了本地密钥（SecretKey）算法后，甲乙双方公开自己的公钥，使用对方的公钥和刚才产生的私钥加密数据，
同时可以使用对方的公钥和自己的私钥对数据解密。 可以扩展为多方共享数据通讯，从而实现网络交互数据的安全通讯！
密钥长度：512-1024，必须是64的整数倍

     完整示例：
        // 初始化长度1024的密钥， 并生成甲方密钥对 
		Map<String, Object> keyMap1 = DHUtils.initKey(1024); 
		// 甲方公钥 
		byte[] publicKey1 = DHUtils.getPublicKey(keyMap1); 
		// 甲方私钥 
		byte[] privateKey1 = DHUtils.getPrivateKey(keyMap1); 
		 
		// 乙方根据甲方公钥产生乙方密钥对 
		Map<String, Object> keyMap2 = DHUtils.initKey(publicKey1); 
		// 乙方公钥 
		byte[] publicKey2 = DHUtils.getPublicKey(keyMap2); 
		// 乙方私钥 
		byte[] privateKey2 = DHUtils.getPrivateKey(keyMap2); 
		  
		// 根据甲方私钥和乙方的公钥， 生成甲方本地密钥secretKey1 
		byte[] secretKey1 = DHUtils.getSecretKeyBytes(publicKey2, privateKey1); 
		System.out.println("甲方本地密钥 : " + StringByteHexUtils.byteArrayToHexString(secretKey1)); 
 
		// 乙方根据其私钥和甲方公钥， 生成乙方本地密钥secretKey2 
		byte[] secretKey2 = DHUtils.getSecretKeyBytes(publicKey1, privateKey2); 
		
		// 原始信息 
		String srcStr = "春宵一刻值千金，花有清香月有阴；歌管楼台声细细，秋千院落夜沉沉！"; 
		// 测试数据加密和解密 
		System.out.println("加密前的数据：" + srcStr); 
		// 甲方进行数据的加密 
		// 用的是甲方的私钥和乙方的公钥 
		byte[] encryptDH = DHUtils.encryptDH(srcStr.getBytes(), publicKey2,privateKey1); 
		System.out.println("加密后的数据 字节数组转16进制：" + StringByteHexUtils.byteArrayToHexString(encryptDH)); 
		// 乙方进行数据的解密 
		// 用的是乙方的私钥和甲方的公钥 
		byte[] decryptDH = DHUtils.decryptDH(encryptDH, publicKey1, privateKey2); 
		
		System.out.println("解密后的数据：" + new String(decryptDH)); 
11.SHA
调用方法：
SHAUtils.sha1(原始信息);
SHAUtils.sha256(原始信息);
SHAUtils.sha384(原始信息);
SHAUtils.sha512(原始信息);
12.MD2
调用方法：MD2Utils.Md2(原始信息)；
13.MD4
调用方法：MD4Utils.Md4(原始信息)；
14.RipeMD-160
调用方法：RipeMD160Utils.ripeMD160(原始信息)；

package com.hqqich;

import io.github.hqqich.cat.security.DESUtils;
import java.util.Base64;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

/**
 * Created by chenhao on 2025/3/27 is 09:40.<p/>
 *
 * @author chenhao
 */
public class DesTest {

    @Test
    @SneakyThrows
    void test01() {


        // 待加密内容
        String srcStr = "Enver20191210";
        // 密码，长度必须是8的倍数
        String password = "E526A308";
        byte[] encriptMsg = DESUtils.encrypt(srcStr, password);
        System.out.println("密码：" + srcStr);
        String encoded = Base64.getEncoder().encodeToString(encriptMsg);
        System.out.println("加密后base64：" + encoded);


        //base64 解密
        Base64.Decoder decoder = Base64.getDecoder();
        //byte[] decode = decoder.decode("Sqwu5XejuDoWlZkQ+WH+/g==");
        byte[] decode = decoder.decode("a0FWWNTzps2ucQg1A1o/Mg==");


        byte[] _decryResult = DESUtils.decrypt(decode, "E526A308");
        System.out.println("解密：" + new String(_decryResult));

    }


}

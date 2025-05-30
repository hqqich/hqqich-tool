package io.github.hqqich.idgenerator;

import io.github.hqqich.idgenerator.util.Seq;
import org.junit.jupiter.api.Test;

/**
 * Created by ChenHao on 2023/11/16 is 14:29.
 *
 * @author hqqich
 */
public class SampleTest {

    @Test
    public void test01() {
        System.out.println(System.currentTimeMillis());
        for (int i = 0; i < 10000; i++) {
            System.out.println(Seq.getId());
        }
        System.out.println(System.currentTimeMillis());
    }

}

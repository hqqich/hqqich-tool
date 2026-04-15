package io.github.hqqich.tool.kts;

import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void testApp() {

        KtsLogger aa = Loggers.get("aa");

        aa.info("你好");
    }
}

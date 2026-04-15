package io.github.hqqich.tool.kts;

import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void testApp() {

        Loggers.enableFile("logs/app.log");
        KtsLogger aa = Loggers.get(AppTest.class);

        aa.info("你好");
    }
}

package net.oc_soft.http;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;


public class BasicRulesTest1 {


    @Test
    void textTestOK() {
        var srcText = "OK";
        var srcBytes = srcText.getBytes(StandardCharsets.UTF_8);

        assert BasicRules.isTEXT(srcBytes);
 
    }
}

// vi: se ts=4 sw=4 et:

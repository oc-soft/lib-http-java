package net.oc_soft.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;



public class HeadersTest1 {


    @Test
    void oneLineHeader() throws IOException {
        String headersStr = "Content-Length: 200\r\n\r\n";
        var srcBytes = headersStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {
            var headers = Headers.read(inputStream);
            assert headers != null;
            assert headers.getFieldNames().size() == 1;
            var fieldValue = headers.getFieldValue("Content-Length");
            assert fieldValue.size() == 1;
            var strLength = new String(fieldValue.get(0),
                StandardCharsets.UTF_8);
            assert " 200".equals(strLength);

        }
    }
    @Test
    void header1() throws IOException {
        String headersStr =
            """
            Content-Length: 200\r
            Xyz-abc: 1234-abc\r
            \r
            """;
        var srcBytes = headersStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {
            var headers = Headers.read(inputStream);
            assert headers != null;
            assert headers.getFieldNames().size() == 2;
            var fieldValue = headers.getFieldValue("Xyz-abc");
            assert fieldValue.size() == 1;
            var str = new String(fieldValue.get(0),
                StandardCharsets.UTF_8);
            assert " 1234-abc".equals(str);

        }
    }
    @Test
    void headerMultilines() throws IOException {
        String headersStr =
            """
            Content-Length: 200\r
            Xyz-abc: 1234-abc\r
            Multiline: abcd efg\r
             hijklm\r
            \r
            """;
        var srcBytes = headersStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {
            var headers = Headers.read(inputStream);
            assert headers != null;
            assert headers.getFieldNames().size() == 3;
            var fieldValue = headers.getFieldValue("Multiline");
            assert fieldValue.size() == 1;
            var str = new String(fieldValue.get(0),
                StandardCharsets.UTF_8);
            var expected = " abcd efg\r\n hijklm";
            assert expected.equals(str)
                : String.format("expected : \"%s\" but \"%s\"",
                        expected, str);

        }
    }
    @Test
    void headerQuoted() throws IOException {
        String headersStr =
            """
            Content-Length: 200\r
            Xyz-abc: 1234-abc\r
            Qline: "abcd efg"\r
            \r
            """;
        var srcBytes = headersStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {
            var headers = Headers.read(inputStream);
            assert headers != null;
            assert headers.getFieldNames().size() == 3;
            var fieldValue = headers.getFieldValue("Qline");
            assert fieldValue.size() == 1;
            var str = new String(fieldValue.get(0),
                StandardCharsets.UTF_8);
            var expected = " \"abcd efg\"";
            assert expected.equals(str)
                : String.format("expected : \"%s\" but \"%s\"",
                        expected, str);

        }
    }

    @Test
    void headerMultiQuoted() throws IOException {
        String headersStr =
            """
            Content-Length: 200\r
            Xyz-abc: 1234-abc\r
            MQline: "abcd efg\r
             hijklm" \r
            \r
            """;
        var srcBytes = headersStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {
            var headers = Headers.read(inputStream);
            assert headers != null;
            assert headers.getFieldNames().size() == 3;
            var fieldValue = headers.getFieldValue("MQline");
            assert fieldValue.size() == 1;
            var str = new String(fieldValue.get(0),
                StandardCharsets.UTF_8);
            var expected = " \"abcd efg\r\n hijklm\" ";
            assert expected.equals(str)
                : String.format("expected : \"%s\" but \"%s\"",
                        expected, str);

        }
    }
}

// vi: se ts=4 sw=4 et:

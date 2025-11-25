package net.oc_soft.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Test;


public class HeadersTest2 {

    @Test
    void readWrite1() throws IOException {
        var headersStr = 
            """
            Content-Length: 401\r
            \r
            """;
        var srcBytes = headersStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes);
            var outputStream = new ByteArrayOutputStream()) {
            var headers = Headers.read(inputStream);

            headers.write(outputStream);
            var writtenBytes = outputStream.toByteArray();
            
            assert srcBytes.length == writtenBytes.length;
            assert Arrays.equals(srcBytes, writtenBytes);

        }
    }

    @Test
    void readTrasferEncoding1() throws IOException {
        var headersStr = 
            """
            Transfer-Encoding: abc\r
            Transfer-Encoding: abc,efg\r
            Transfer-Encoding: "hello world"abc,efg, " abd,123"\r
            \r
            """;
        var srcBytes = headersStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes);
            var outputStream = new ByteArrayOutputStream()) {
            var headers = Headers.read(inputStream);

            var transferEncodings = headers.getTransferEncoding();
            assert transferEncodings != null;
            var expectedEncodings = new String[] {
                "abc", "abc", "efg", "hello worldabc", "efg", "abd", "123"
            };
            assert transferEncodings.size() == expectedEncodings.length :
                String.format("Encodding count expected: %d but %d",
                    expectedEncodings.length,
                    transferEncodings.size());
 
            for (var idx = 0; idx < expectedEncodings.length; idx++) {
                var encoding = transferEncodings.get(idx);
                var expected = expectedEncodings[idx];
                assert expected.equals(encoding) : String.format(
                    "expected \"%s\" but \"%s\"", expected, encoding);
            }
        }
    }

    @Test
    void readContentLength1() throws IOException {
        var headersStr = 
            """
            Content-Length: 1432\r
            \r
            """;
        var srcBytes = headersStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes);
            var outputStream = new ByteArrayOutputStream()) {
            var headers = Headers.read(inputStream);

            var contentLength = headers.getContentLength();
            assert contentLength != null;
            assert contentLength.equals(1432)  :
                String.format("contentLength expected %d but %d",
                    1432,
                    contentLength);
        }
    }

    @Test
    void readContentLength2() throws IOException {
        var headersStr = 
            """
            Content-Length: 1432\r
            Abc: abc\r
            Content-Length: 1432 \r
            \r
            """;
        var srcBytes = headersStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes);
            var outputStream = new ByteArrayOutputStream()) {
            var headers = Headers.read(inputStream);

            var contentLength = headers.getContentLength();
            assert contentLength != null;
            assert contentLength.equals(1432)  :
                String.format("contentLength expected %d but %d",
                    1432,
                    contentLength);
        }
    }
    @Test
    void readContentLength3() throws IOException {
        var headersStr = 
            """
            Content-Length: 1432\r
            Abc: abc\r
            Content-Length: 1433\r
            \r
            """;
        var srcBytes = headersStr.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes);
            var outputStream = new ByteArrayOutputStream()) {
            var headers = Headers.read(inputStream);

            var contentLength = headers.getContentLength();
            assert contentLength == null;
        }
    }
}
// vi: se ts=4 sw=4 et:

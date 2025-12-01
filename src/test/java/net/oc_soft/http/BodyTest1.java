package net.oc_soft.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

public class BodyTest1 {

    @Test
    void memoryBody1() throws IOException {
        String bodyStr = "Hello world";
        var srcBytes = bodyStr.getBytes(StandardCharsets.UTF_16);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {
            var body = Body.read(inputStream, srcBytes.length);
            assert body.getContents() instanceof BodyByteArrayContents;
            try (var contentsStream = body.getInputStream()) {
                var contents = contentsStream.readAllBytes();
                var contentsStr = new String(contents, StandardCharsets.UTF_16);
                assert bodyStr.equals(contentsStr);
            }
            body.close();
            assert body.getContents() == null;
        }
    }

    @Test
    void fileBody1() throws IOException {
        String bodyStr = "Hello world";
        var srcBytes = bodyStr.getBytes(StandardCharsets.UTF_16);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {
            var body = Body.read(inputStream, srcBytes.length - 1);
            assert body.getContents() instanceof BodyFileContents;
            try (var contentsStream = body.getInputStream()) {
                var contents = contentsStream.readAllBytes();
                var contentsStr = new String(contents, StandardCharsets.UTF_16);
                assert bodyStr.equals(contentsStr);
            }
            body.close();
            assert body.getContents() == null;
        }
    }


    @Test
    void readToEnd1() throws IOException {
        var messageStr =
            """
            This is first line.
            This is second line.
            """;
        var messageBytes = messageStr.getBytes(StandardCharsets.UTF_16);
        try (var inputStream = new ByteArrayInputStream(messageBytes)) {
            var body = Body.readToEnd(inputStream,
                128, 10);
            try (var contentsStream = body.getInputStream()) {
                var contents = contentsStream.readAllBytes();
                var contentsStr = new String(contents, StandardCharsets.UTF_16);
                assert messageStr.equals(contentsStr);
            }
            body.close();
        }
    }
}

// vi: se ts=4 sw=4 et:

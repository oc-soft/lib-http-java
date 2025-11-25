package net.oc_soft.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class StatusLineTest1 {


    @Test
    void statusLine200OK() throws IOException {
        var testLine = "HTTP/123.221 200 OK\r\n";
        var srcBytes = testLine.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {

            var statusLine = StatusLine.read(inputStream);

            assert statusLine != null;
            assert "HTTP/123.221".equals(statusLine.getHttpVersion());
            assert "200".equals(statusLine.getStatusCode());
            assert "OK".equals(statusLine.getReasonPhrase());
        }
    }

    @Test
    void statusLine200() throws IOException {
        var testLine = "HTTP/123.221 200 \r\n";
        var srcBytes = testLine.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {

            var statusLine = StatusLine.read(inputStream);

            assert statusLine != null;
            assert "HTTP/123.221".equals(statusLine.getHttpVersion());
            assert "200".equals(statusLine.getStatusCode());
            assert statusLine.getReasonPhrase().isEmpty();
        }
    }

    @Test
    void statusLineBad1() throws IOException {
        var testLine = "HTTP/AA.221 200 OK\r\n";
        var srcBytes = testLine.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {
            var statusLine = StatusLine.read(inputStream);
            assert statusLine == null;
        }
    }

    @Test
    void statusLineBad2() throws IOException {
        var testLine = "HTTP/1.221 2A0 OK\r\n";
        var srcBytes = testLine.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {

            var statusLine = StatusLine.read(inputStream);

            assert statusLine == null;
        }
    }

    @Test
    void statusLineBad3() throws IOException {
        var testLine = "HTTP/1.221 2A0 OK\n";
        var srcBytes = testLine.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {
            var statusLine = StatusLine.read(inputStream);
            assert statusLine == null;
        }
    }

    @Test
    void reasonPhraseContainsSP() throws IOException {
        var testLine = "HTTP/1.1 400 no data\r\n";
        var srcBytes = testLine.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {
            var statusLine = StatusLine.read(inputStream);
            assert statusLine != null;
            assert "no data".equals(statusLine.getReasonPhrase());
        }
    }


    @Test
    void streamPointerCorrect() throws IOException {
        var testLine = 
            """
            HTTP/1.1 400 no data\r
            a
            """;
        var srcBytes = testLine.getBytes(StandardCharsets.UTF_8);
        try (var inputStream = new ByteArrayInputStream(srcBytes)) {
            var statusLine = StatusLine.read(inputStream);
            assert statusLine != null;
            assert "no data".equals(statusLine.getReasonPhrase());
            assert 'a' == inputStream.read();
        }
    }


}

// vi: se ts=4 sw=4 et:

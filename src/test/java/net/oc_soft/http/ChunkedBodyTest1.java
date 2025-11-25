package net.oc_soft.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;


public class ChunkedBodyTest1 {

    @Test void readBody1() throws IOException {

        var bodyStr =
            """
            This is line1
            This is line2
            """;
        var bodyData = bodyStr.getBytes(StandardCharsets.UTF_8);

        try (var dataSourceStream = new ByteArrayOutputStream()) {
            var sizeLine = String.format("%x\r\n", bodyData.length);
            dataSourceStream.write(sizeLine.getBytes(StandardCharsets.UTF_8));
            dataSourceStream.write(bodyData);
            dataSourceStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
            dataSourceStream.write("0\r\n".getBytes(StandardCharsets.UTF_8));
            dataSourceStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
            try (var inputStream = new ByteArrayInputStream(
                dataSourceStream.toByteArray());
                var rawBodyStream = new ByteArrayOutputStream()) {

                var chunkedBody = ChunkedBody.read(inputStream, 128, 10); 
                assert chunkedBody != null;

                var body = chunkedBody.getMessageBody();
                var srcStream = body.getInputStream();
                
                for (var chunkSizeLine : chunkedBody.getChunkLines()) {
                    var readSize = chunkSizeLine.chunkSizeLine().getChunkSize();
                    if (readSize > 0) {
                        var skipSize = chunkSizeLine.size();
                        srcStream.skip(skipSize);
                        var copyBuf = new byte[readSize]; 
                        var actualReadSize = srcStream.read(copyBuf);
                        assert actualReadSize == copyBuf.length;
                        rawBodyStream.write(copyBuf);
                    }
                }
                var rawBodyStr = new String(rawBodyStream.toByteArray(),
                    StandardCharsets.UTF_8);
                assert rawBodyStr.equals(bodyStr) : String.format(
                    """
                    exptected:
                    %s
                    actual:
                    %s
                    """, bodyStr, rawBodyStr); 
            }
        }
    }


    @Test void 
    readBody2() throws IOException {

        var bodyStr1 =
            """
            This is line1
            This is line2
            """;
        var bodyData1 = bodyStr1.getBytes(StandardCharsets.UTF_8);
        var bodyStr2 =
            """
            This is line3
            This is line4
            This is line5
            """;
        var bodyData2 = bodyStr2.getBytes(StandardCharsets.UTF_8);
        try (var dataSourceStream = new ByteArrayOutputStream()) {
            var sizeLine = String.format("%x\r\n", bodyData1.length);
            dataSourceStream.write(sizeLine.getBytes(StandardCharsets.UTF_8));
            dataSourceStream.write(bodyData1);
            dataSourceStream.write("\r\n".getBytes(StandardCharsets.UTF_8));

            sizeLine = String.format("%x\r\n", bodyData2.length);
            dataSourceStream.write(sizeLine.getBytes(StandardCharsets.UTF_8));
            dataSourceStream.write(bodyData2);
            dataSourceStream.write("\r\n".getBytes(StandardCharsets.UTF_8));

            dataSourceStream.write("0\r\n".getBytes(StandardCharsets.UTF_8));
            dataSourceStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
            try (var inputStream = new ByteArrayInputStream(
                dataSourceStream.toByteArray());
                var rawBodyStream = new ByteArrayOutputStream()) {

                var chunkedBody = ChunkedBody.read(inputStream, 128, 10); 
                assert chunkedBody != null;

                var body = chunkedBody.getMessageBody();
                var srcStream = body.getInputStream();
                
                for (var chunkSizeLine : chunkedBody.getChunkLines()) {
                    var readSize = chunkSizeLine.chunkSizeLine().getChunkSize();
                    if (readSize > 0) {
                        var skipSize = chunkSizeLine.size();
                        srcStream.skip(skipSize);
                        var copyBuf = new byte[readSize]; 
                        var actualReadSize = srcStream.read(copyBuf);
                        assert actualReadSize == copyBuf.length;
                        rawBodyStream.write(copyBuf);
                        srcStream.skip(2);
                    }
                }
                var rawBodyStr = new String(rawBodyStream.toByteArray(),
                    StandardCharsets.UTF_8);
                var bodyStr = bodyStr1 + bodyStr2;
                assert rawBodyStr.equals(bodyStr) : String.format(
                    """
                    exptected:
                    %s
                    actual:
                    %s
                    """, bodyStr, rawBodyStr); 
            }
        }
    }
} 



// vi: se ts=4 sw=4 et:

package net.oc_soft.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Test;


public class ResponseTest1 {


    static byte[] createChunkedResponseData(String statusLineAndHeaders,
        byte[]... chunkedDataArray) 
            throws IOException {

        byte[] result = null;
        try (var os = new ByteArrayOutputStream()) {
            os.write(statusLineAndHeaders.getBytes(StandardCharsets.UTF_8));
            for (var chunkedData : chunkedDataArray) {
                os.write(
                    String.format(
                        "%x\r\n", chunkedData.length).getBytes(
                            StandardCharsets.UTF_8));
                os.write(chunkedData);
                os.write("\r\n".getBytes(StandardCharsets.UTF_8));
            }
            os.write("0\r\n".getBytes(StandardCharsets.UTF_8));
            os.write("\r\n".getBytes(StandardCharsets.UTF_8));
            result = os.toByteArray();
        }
        return result;
 
    }

    static byte[] createResponseData(String statusLineAndHeaders,
        byte[] body) throws IOException {

        byte[] result = null;
        try (var os = new ByteArrayOutputStream()) {
            os.write(statusLineAndHeaders.getBytes(StandardCharsets.UTF_8));
            os.write(body);
            result = os.toByteArray();
        }
        return result;
    }

    @Test
    public void responseTest1() throws IOException {

        var responseStr =
            """
            HTTP/1.1 200 OK\r
            X-SomeHeader: "no-data"\r
            \r
            """;
        var responseData = responseStr.getBytes(StandardCharsets.UTF_8);
        try (var stream = new ByteArrayInputStream(responseData)) {
            var response = Response.read(stream, 124, 10);

            assert response != null;
            assert response.getStatusLine() != null;
            assert response.getHeaders() != null;
            assert response.getMessageBody() == null;
            response.close();
        }
    }

    @Test
    public void responseTest2() throws IOException {

        var messageBodyStr =
            """
            This is a message body
            This body will be encoded in UTF-18
            """;
        var messageBodyData = messageBodyStr.getBytes(StandardCharsets.UTF_16);

        var statusAndHeadersStrF =
            """
            HTTP/1.1 200 OK\r
            X-SomeHeader: "no-data"\r
            Content-Length: %d\r
            \r
            """;
        var statusAndHeadersStr = String.format(statusAndHeadersStrF,
            messageBodyData.length);
        try (var stream = new ByteArrayInputStream(
                createResponseData(statusAndHeadersStr, messageBodyData))) {
            var response = Response.read(stream, 124, 10);

            assert response != null;
            assert response.getStatusLine() != null;
            assert response.getHeaders() != null;
            assert response.getMessageBody() != null;
            assert response.getMessageBody().isSimpleBody();

            var parsedBody = response.getMessageBody().getSimpleBody(); 
           
            var parsedMessageBodyStr =
                new String(parsedBody.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_16); 
            
            assert messageBodyStr.equals(parsedMessageBodyStr);

            response.close();
        }
    }
    @Test
    public void responseTest3() throws IOException {
        var messageBodyStr1 =
            """
            This is first message body.
            """;
        var messageBodyData1 = messageBodyStr1.getBytes(
            StandardCharsets.UTF_16BE);
        var messageBodyStr2 =
            """
            This is second message body.
            """;
        var messageBodyData2 = messageBodyStr2.getBytes(
            StandardCharsets.UTF_16BE);

        var statusAndHeadersStr =
            """
            HTTP/1.1 200 OK\r
            X-SomeHeader: "no-data"\r
            Transfer-Encoding: chunked\r
            \r
            """;
        try (var stream = new ByteArrayInputStream(
                createChunkedResponseData(
                    statusAndHeadersStr,
                    messageBodyData1, messageBodyData2))) {
            var response = Response.read(stream, 124, 10);

            assert response != null;
            assert response.getStatusLine() != null;
            assert response.getHeaders() != null;
            assert response.getMessageBody() != null;
            assert !response.getMessageBody().isSimpleBody();
            var chunkedBody = response.getMessageBody().getChunkedBody();

            String responseMessage = null;
            try (var chunkedStream =
                chunkedBody.getMessageBody().getInputStream();
                var rawBodyStream = new ByteArrayOutputStream()) {
                var messageSourceStrs = new String[] {
                    messageBodyStr1,
                    messageBodyStr2,
                    null
                };
                var messageSourceBytes = new byte[][] {
                    messageBodyData1,
                    messageBodyData2,
                    null
                };
                var chunkedLines = chunkedBody.getChunkLines();
                assert messageSourceStrs.length == chunkedLines.size();
                for (var idx = 0; idx < chunkedLines.size(); idx++) {
                    var chunkSizeLine = chunkedLines.get(idx);
                    var readSize = chunkSizeLine.chunkSizeLine().getChunkSize();
                    if (readSize > 0) {
                        var skipSize = chunkSizeLine.size();
                        chunkedStream.skip(skipSize);
                        var copyBuf = new byte[readSize]; 
                        var actualReadSize = chunkedStream.read(copyBuf);
                        assert actualReadSize == copyBuf.length;
                        assert Arrays.equals(
                            copyBuf, messageSourceBytes[idx]);

                        var decodeStr = new String(
                            copyBuf, StandardCharsets.UTF_16BE);
                        assert messageSourceStrs[idx].equals(decodeStr) :
                            String.format("expect : %s but %s",
                                messageSourceStrs[idx], decodeStr);
                        rawBodyStream.write(copyBuf);
                        chunkedStream.skip(2);
                    }
                }
                responseMessage = new String(rawBodyStream.toByteArray(),
                    StandardCharsets.UTF_16BE);
            }

            var messageBodyStr = messageBodyStr1 + messageBodyStr2;
            assert messageBodyStr.equals(responseMessage) :
                String.format("expect : %s but : %s",
                    messageBodyStr, responseMessage);
            response.close();
        }
    }

    @Test
    public void responseTest4() throws IOException {
        var messageBodyStr1 =
            """
            This is first message body.
            """;
        var messageBodyData1 = messageBodyStr1.getBytes(
            StandardCharsets.UTF_16);

        var statusAndHeadersStr =
            """
            HTTP/1.1 200 OK\r
            X-SomeHeader: "no-data"\r
            \r
            """;
        try (var stream = new ByteArrayInputStream(
                createResponseData(
                    statusAndHeadersStr,
                    messageBodyData1))) {
            var response = Response.read(stream, 124, 10);

            assert response != null;
            assert response.getStatusLine() != null;
            assert response.getHeaders() != null;
            assert response.getMessageBody() != null;
            assert response.getMessageBody().isSimpleBody();

            var parsedBody = response.getMessageBody().getSimpleBody(); 
           
            var parsedMessageBodyStr =
                new String(parsedBody.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_16); 
            
            assert messageBodyStr1.equals(parsedMessageBodyStr);

            response.close();
        }
    }
}

// vi: se ts=4 sw=4 et:

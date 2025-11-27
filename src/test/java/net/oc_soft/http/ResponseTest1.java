package net.oc_soft.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Test;


public class ResponseTest1 {


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
        }
    }
}

// vi: se ts=4 sw=4 et:

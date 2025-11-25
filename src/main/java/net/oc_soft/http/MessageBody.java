package net.oc_soft.http;

import java.io.InputStream;

/**
 * http message body
 */
public class MessageBody {



    /**
     * read body from input stream
     * @param headers http headers which is parsed in same stream.
     * @param strean input stream.
     * @param memoryBodySize the threshold to keep body content in memory.
     * @return body
     */
    static Body read(InputStream stream,
        Headers headers,
        int memoryBodySize) {

        var containsContentLength = headers.containsField(
            "Content-Length");
        var containsTransferEnconding = headers.containsField(
            "Transfer-Encoding");

        Body result = null;
        if (containsContentLength) {
            if (!containsTransferEnconding) {
                var contentLength = headers.getContentLength();
                if (contentLength != null) {
                     result = Body.read(stream, contentLength, memoryBodySize);
                }
            }
        } else if (containsTransferEnconding) {
            if (!containsContentLength) {
            }
        }
        return result;
    }
}

// vi: se ts=4 sw=4 et:

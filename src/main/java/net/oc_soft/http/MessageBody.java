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
     * @param bufferSizeForCopyMemory the buffer size to memory copy.
     * @return body
     */
    static MessageBody read(InputStream stream,
        Headers headers,
        int memoryBodySize,
        int copyBufferSize) {

        var containsContentLength = headers.containsField(
            "Content-Length");
        var containsTransferEnconding = headers.containsField(
            "Transfer-Encoding");

        MessageBody result = null;
        if (containsContentLength) {
            if (!containsTransferEnconding) {
                var contentLength = headers.getContentLength();
                if (contentLength != null) {
                     var body = Body.read(stream,
                        contentLength, memoryBodySize);
                     if (body != null) {
                         result = new MessageBody(body);
                     }
                }
            }
        } else if (containsTransferEnconding) {
            if (!containsContentLength) {
                var chunkedBody = ChunkedBody.read(
                    stream, memoryBodySize, copyBufferSize); 
                if (chunkedBody != null) {
                    result = new MessageBody(chunkedBody);
                }
            }
        }
        return result;
    }

    /**
     * message body or chunked body
     */
    private Object body;


    /**
     * construct message mody
     */
    MessageBody(Body body) {
        this.body = body;
    }

    /**
     * construct message body
     */
    MessageBody(ChunkedBody body) {
        this.body = body;
    }
     

    /**
     * You get true if body is not chunked body.
     * @return the flag whether body is not chunked body
     */
    public boolean isSimpleBody() {
        return body instanceof Body;
    }

    /**
     * You get chunked body if messageh body is chunked body or null
     * @return chunked body
     */
    public ChunkedBody getChunkedBody() {
        ChunkedBody result = null;
        if (body instanceof ChunkedBody) {
            result = (ChunkedBody)body;
        }
        return result;
    }

    /**
     * You get body if messageh body is not chunked body or null
     * @return chunked body
     */
    public Body getSimpleBody() {
        Body result = null;
        if (isSimpleBody()) {
            result = (Body)body; 
        }
        return result;
    }
}

// vi: se ts=4 sw=4 et:

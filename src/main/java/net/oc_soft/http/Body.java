package net.oc_soft.http;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * represent message body
 */
public class Body {

    /**
     * get body contents
     */
    interface Contents {
        /**
         * free resource 
         */
        void close();
        /**
         * get input stream to read contents
         */
        InputStream getInputStream() throws IOException;
    }

    /**
     * read body from stream.
     * @param stream body contents
     * @param memoryBodySize specify threshold about keep in memory or file.
     */
    static Body read(InputStream stream,
        int memoryBodySize) {
        return read(stream, Integer.MAX_VALUE, memoryBodySize);
    }
    /**
     * read body from stream.
     * @param stream body contents
     * @param bodySize reading size to create body
     * @param memoryBodySize specify threshold about keep in memory or file.
     */
    static Body read(InputStream stream,
        int bodySize,
        int memoryBodySize) {
        Body result = null;
        var bodyBuffer = new byte[Math.min(memoryBodySize, bodySize)];
        try (var sourceStream = new BufferedInputStream(stream,
                bodyBuffer.length + 1)) {
            var doProcess = true;
            sourceStream.mark(bodyBuffer.length + 1);
            int readSize = sourceStream.read(bodyBuffer);
            if (0 <= readSize) {
                var useMemoryBody = true;
                if (readSize == bodyBuffer.length) {
                    var aByte = sourceStream.read();
                    useMemoryBody = aByte == -1; 
                } else { 
                    bodyBuffer = Arrays.copyOf(bodyBuffer, readSize);
                }
                if (doProcess) {
                    Contents contents = null;
                    if (useMemoryBody) {
                        contents = new BodyByteArrayContents(bodyBuffer);
                    } else {
                        sourceStream.reset();
                        contents = BodyFileContents.create(sourceStream);
                    }
                    if (contents != null) {
                        result = new Body(contents);
                    }
                }
            }
        } catch (IOException ex) {
        }
        return result;
    }

    /**
     * body contents
     */
    private Contents contents;


    /**
     * create body with contents
     * @param contents body contents
     */
    Body(Contents contents) {
        this.contents = contents;
    }


    /**
     * free resource.
     */
    public synchronized void close() {
        Contents contents = this.contents; 
        if (contents != null) {
            contents.close();
            this.contents = null;
        }
    }

    /**
     * get input stream of body contents
     */
    public InputStream getInputStream() throws IOException {
        Contents contents = this.contents; 
        InputStream result = null;
        if (contents != null) {
            result = contents.getInputStream();
        }
        return result;
    }

    /**
     * get contents object. 
     */
    Contents getContents() {
        return contents;
    }
}

// vi: se ts=4 sw=4 et:

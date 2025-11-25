package net.oc_soft.http;

import java.io.InputStream;
import java.io.ByteArrayInputStream;

/**
 * this object keeps body contens in byte array
 */
public class BodyByteArrayContents
    implements Body.Contents {

    /**
     * body contents
     */
    private byte[] contents;



    /**
     * create body contents object which keep contents in byte array.
     */
    BodyByteArrayContents(byte[] contents) {
                
        this.contents = contents;
    }

    public void close() {
        contents = null;
    }

    /**
     * get input stream;
     */
    public InputStream getInputStream() {
        var contents = this.contents;
        InputStream result = null;
        if (contents != null) {
            result = new ByteArrayInputStream(contents);
        }
        return result;
    }
}

// vi: se ts=4 sw=4 et:

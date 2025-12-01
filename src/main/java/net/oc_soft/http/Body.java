package net.oc_soft.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Logger;
import java.nio.file.Path;

import net.oc_soft.io.MemFileOutputStream;

/**
 * represent message body
 */
public class Body {

    /**
     * logger
     */
    static Logger LOGGER;

    /**
     * get logger
     */
    private synchronized static Logger getLogger() {
        if (LOGGER == null) {
            LOGGER = Logger.getLogger(Body.class.getName());
        }
        return LOGGER;
    }


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
     * read body contents from stream until end of stream.
     * @param stream body contents
     * @param memoryBodySize specify threshold about keep in memory or file.
     * @param copyBuffSize specify size of temporarry buffer for copy. 
     * @return content 
     */
    static Contents readContentsToEnd(InputStream stream,
        int memoryBodySize,
        int copyBuffSize) {
        Contents result = null;
        Path filePath = null;

        boolean doProcess = true;

        try {
            filePath = BodyFileContents.createTempFile();
        } catch (IOException ex) {
            getLogger().log(java.util.logging.Level.SEVERE,
                "exception occured", ex);
            doProcess = false;
        }
        
        if (doProcess) {
            var copyBuf = new byte[copyBuffSize];

            try (var memFileStream = new MemFileOutputStream(
                    memoryBodySize, filePath)) {
                while (true) {
                    var readSize = stream.read(copyBuf);

                    if (readSize > 0) {
                        memFileStream.write(copyBuf, 0, readSize);
                    }
                    if (readSize == -1) {
                        break;
                    }
                }
                if (memFileStream.getSize() > 0) {
                    if (memFileStream.isSavedIntoPath()) {
                        result = new BodyFileContents(
                            memFileStream.getOutputPath(),
                            true);
                        filePath = null;
                    } else {
                        result = new BodyByteArrayContents(
                            memFileStream.getMemoryData());
                    }
                }
            } catch (IOException ex) {
                getLogger().log(java.util.logging.Level.SEVERE,
                    "exception occured", ex);
            } finally {
                if (filePath != null) {
                    filePath.toFile().delete();
                    filePath = null;
                }
            }
        }
        if (filePath != null) {
            filePath.toFile().delete();
        }
        return result;
    }

    /**
     * read body content from stream until end of stream.
     * @param stream body contents
     * @param memoryBodySize specify threshold about keep in memory or file.
     * @param copyBuffSize specify size of temporarry buffer for copy. 
     * @return content 
     */ 
    public static Body readToEnd(
        InputStream stream,
        int memoryBodySize,
        int copyBuffSize) {
        Body result = null;
        var contents = readContentsToEnd(stream, memoryBodySize, copyBuffSize);
        if (contents != null) {
            result = new Body(contents);
        }
        return result;
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
            getLogger().log(java.util.logging.Level.SEVERE,
                "exception occured", ex);
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

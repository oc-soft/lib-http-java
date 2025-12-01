package net.oc_soft.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.oc_soft.io.BackupInputStream;
import net.oc_soft.io.MemFileOutputStream;

/**
 * chunked body
 */
public class ChunkedBody {

    /**
     * logger
     */
    static Logger LOGGER;

    /**
     * get logger
     */
    private synchronized static Logger getLogger() {
        if (LOGGER == null) {
            LOGGER = Logger.getLogger(ChunkedBody.class.getName());
        }
        return LOGGER;
    }

    /**
     * read chunked body from stream
     */
    public static ChunkedBody read(InputStream stream,
        int memoryBodySize,
        int copyBufferSize) {
        ChunkedBody result = null;
        var doProcess = true;
        Path path = null;
        try {
            path = BodyFileContents.createTempFile();
        } catch (IOException ex) {
            getLogger().log(java.util.logging.Level.SEVERE,
                "can not create temporary file because of exception",
                ex);
            doProcess = false;
        }
        
        if (doProcess) {
            var copyBuffer = new byte[copyBufferSize];
            var memFileStream = new MemFileOutputStream(
                memoryBodySize, path);
            Map<String, List<byte[]>> trailerFields = null;
            var chunkSizeLineSizes = new ArrayList<ChunkSizeLineSize>();
            Body messageBody = null;
            try (var sourceInputStream = new BackupInputStream(
                    stream, memFileStream)) {
                while (doProcess) {
                    ChunkSizeLineSize chunkSizeLineSize = null;
                    var mfStartSize = memFileStream.getSize(); 
                    if (doProcess) {
                        chunkSizeLineSize = ChunkSizeLine.read(
                            sourceInputStream);
                        doProcess = chunkSizeLineSize != null;
                    }
                    if (doProcess) {
                        chunkSizeLineSizes.add(chunkSizeLineSize);
                        var chunkSize =
                            chunkSizeLineSize.chunkSizeLine().getChunkSize();
                        var mfCurSize = memFileStream.getSize(); 
                        var alreadyRead = mfCurSize - (
                            mfStartSize + chunkSizeLineSize.size());
                        var restReadSize = chunkSize - alreadyRead;
                        if (restReadSize > 0) {
                            var bufferCopyCount = 
                                restReadSize / copyBuffer.length;
                            var byteCopyCount = 
                                restReadSize % copyBuffer.length;

                            for (var idx = 0; idx < bufferCopyCount; idx++) {
                                var readSize = sourceInputStream.read(
                                    copyBuffer);
                                doProcess = readSize == copyBuffer.length;
                                if (!doProcess) {
                                    break;
                                }
                            }
                            if (doProcess) {
                                if (byteCopyCount > 0) {
                                    var readSize = sourceInputStream.read(
                                        copyBuffer, 0, byteCopyCount);
                                    doProcess = readSize == byteCopyCount;
                                }
                            }
                            if (doProcess) {
                                var endMarkBuf = new byte[2];
                                var actualReadSize = sourceInputStream.read(
                                    endMarkBuf);
                                doProcess = actualReadSize == endMarkBuf.length;
                                if (doProcess) {
                                    doProcess = BasicRules.isCR(endMarkBuf[0])
                                        && BasicRules.isLF(endMarkBuf[1]);
                                }
                            }
                            
                        } else {
                            break;
                        }
                    }
                }
                if (doProcess && memFileStream.getSize() > 0) {
                    Body.Contents bodyContents = null;
                    if (memFileStream.isSavedIntoPath()) {
                        bodyContents = new BodyFileContents(
                            memFileStream.getOutputPath(),
                            true);
                        path = null;
                    } else {
                        bodyContents = new BodyByteArrayContents(
                            memFileStream.getMemoryData());
                    }
                    messageBody = new Body(bodyContents);
                }
                if (doProcess) {
                    trailerFields = FieldMap.readFieldMap(
                        new BufferedInputStream(sourceInputStream, 2));
                    doProcess = trailerFields != null;
                }
                if (!doProcess) {
                    if (messageBody != null) {
                        messageBody.close();
                    }
                }
            } catch (IOException ex) {
                getLogger().log(java.util.logging.Level.SEVERE,
                    "can not load chunked body because of exception",
                    ex);
            } finally {
                if (path != null) {
                    path.toFile().delete();
                    path = null;
                }
            }
            if (doProcess) {
                result = new ChunkedBody(chunkSizeLineSizes,
                    messageBody, trailerFields);
            }
        }
        if (path != null) {
            path.toFile().delete();
        }
        return result;
    }

    /**
     * list of chunk size line and actual chunk line size
     */
    private List<ChunkSizeLineSize> chunkSizeLines;

    /**
     * chunked data
     */
    private Body messageBody;

    /**
     * trailer fields
     */
    private Map<String, List<byte[]>> trailerFields;


    /**
     * constructor
     */
    ChunkedBody(
        List<ChunkSizeLineSize> chunkSizeLines,
        Body messageBody,
        Map<String, List<byte[]>> trailerFields) {

        this.chunkSizeLines = chunkSizeLines;
        this.messageBody = messageBody;
        this.trailerFields = trailerFields;
    }


    /**
     * release system resource
     */
    public synchronized void close() {
        if (messageBody != null) {
            messageBody.close();
            messageBody = null;
        }
        chunkSizeLines = null;
        trailerFields = null;
    }

    /**
     * get chunk line size list
     */
    public List<ChunkSizeLineSize>
    getChunkLines() {
        return Collections.unmodifiableList(chunkSizeLines);
    }

    /**
     * get message body.
     */
    public Body getMessageBody() {
        return messageBody;
    }

    /**
     * get trailer field map
     * @return trailer field map
     */
    Map<String, List<byte[]>> getTrailerFields() {
        return Collections.unmodifiableMap(trailerFields);
    }

    /**
     * get trailer field names
     */
    public Set<String> getTrailerFieldNames() {
        return trailerFields.keySet();
    }

    /**
     * get trailer field map
     * @return trailer field map
     */
     public List<byte[]> getTrailerField(String name) {
        List<byte[]> result = null;
        var fields = trailerFields.get(name);
        if (fields != null) {
            result = Collections.unmodifiableList(fields);
        }
        return result;
    }
}

// vi: se ts=4 sw=4 et:

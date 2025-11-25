package net.oc_soft.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;


/**
 * field map
 */
public class FieldMap {

    /**
     * logger
     */
    static Logger LOGGER;

    /**
     * get logger
     */
    private synchronized static Logger getLogger() {
        if (LOGGER == null) {
            LOGGER = Logger.getLogger(FieldMap.class.getName());
        }
        return LOGGER;
    } 
    /**
     * convert byte arry to hex string
     */
    static String toHexString(byte[] byteArray) {
        String result = null;
        if (byteArray != null) {
            var strByteList = new ArrayList<String>();
            for (var aByte : byteArray) {
                strByteList.add(String.format("%02x", aByte));
            }
            result = String.join(" ", strByteList);
        }
        return result;
    }


    /**
     * read field value
     */
    static byte[] readFieldValue(InputStream stream) {
        byte[] result = null;
        assert stream.markSupported();
        try (var processingStream = new ByteArrayOutputStream()) {
            var lastByte = -1;
            var doProcess = true;
            final int NORMAL_MODE = 0;
            final int QUOTE_MODE = 1;
            int mode = NORMAL_MODE; 
            var quoteEscape = false;
            Integer dataSize = null;
            while (doProcess) {
                stream.mark(1);
                var aByte = stream.read();
                doProcess = aByte != -1;
                if (doProcess) {
                    if (mode == NORMAL_MODE) {
                        if (BasicRules.isLF(aByte)) {
                            doProcess = BasicRules.isCR(lastByte);
                        } else if (BasicRules.isLF(lastByte)) {
                            // if state is in \r\n[SP] or \r\n[HT] pattern,
                            // field value is continue. Or finished field value.
                            if (!BasicRules.isHT(aByte)
                                && !BasicRules.isSP(aByte)) {
                                stream.reset();
                                dataSize = processingStream.size() - 2;
                                break;
                            }
                        }
                        if (doProcess) {
                            // check aByte is TEXT 
                            if (BasicRules.isCTL(aByte)) {
                                doProcess = BasicRules.isSP(aByte)
                                    || BasicRules.isHT(aByte)
                                    || BasicRules.isCR(aByte)
                                    || BasicRules.isLF(aByte);
                            } else  {
                                if (aByte == '"') {
                                    mode = QUOTE_MODE;
                                // } else if (BasicRules.isSeparator(aByte)) {
                                //    doProcess = true;
                                // } else if (BasicRules.isToken(aByte)) {
                                //    doProcess = true;
                                } // else { accept as TEXT data }
                                    
                            }
                        }
                        if (doProcess) {
                            processingStream.write(aByte);
                        }
                    } else { // QUOTE_MODE
                        if (!quoteEscape) {
                            if (aByte == '"') {
                                mode = NORMAL_MODE;
                            } else if (aByte == '\\') {
                                quoteEscape = true;
                            } else {
                                if (BasicRules.isLF(aByte)) {
                                    doProcess = BasicRules.isCR(lastByte);
                                } else if (BasicRules.isLF(lastByte)) {
                                    // if state is in \r\n[SP] or \r\n[HT]
                                    // pattern, field value is continue.
                                    // Or finished field value.
                                    if (!BasicRules.isHT(aByte)
                                        && !BasicRules.isSP(aByte)) {
                                        doProcess = false;
                                    }
                                } else if (BasicRules.isCTL(aByte)) {
                                    // check TEXT again
                                    doProcess = BasicRules.isSP(aByte)
                                        || BasicRules.isHT(aByte)
                                        || BasicRules.isCR(aByte)
                                        || BasicRules.isLF(aByte);
                                }
                            }
                            if (doProcess) {
                                processingStream.write(aByte);
                            }
                        } else {
                            processingStream.write(aByte);
                            quoteEscape = false;
                        }
                    }
                    lastByte = aByte;
                }
            }
            if (doProcess) {
                result = processingStream.toByteArray();
                if (dataSize != null) {
                    result = Arrays.copyOf(result, dataSize);
                }
            } else {
                getLogger().log(
                    java.util.logging.Level.FINE,
                        "not accepted data: {0}",
                        toHexString(processingStream.toByteArray()));
            }
        } catch (IOException ex) {
        }
        return result;
    }


    /**
     * parse byte data which has field value
     */
    static List<String> parseFieldValue(byte[] byteData) {
        List<String> result = null;
        var doProcess = byteData != null;
        if (doProcess) {
            var fieldList = new ArrayList<String>();
            try (var stream = new ByteArrayInputStream(byteData);
                var bufferStream = new ByteArrayOutputStream()) {
                final int NORMAL_MODE = 0;
                final int QUOTE_MODE = 1;
                int mode = NORMAL_MODE;
                var quoteEscape = false;
                while (doProcess) {
                    var aByte = stream.read();
                    if (aByte != -1) {
                        if (mode == NORMAL_MODE) {
                            if (aByte != '"') {
                                bufferStream.write(aByte);
                            } else {
                                if (bufferStream.size() > 0) {
                                    bufferStream.flush();
                                    fieldList.add(
                                        new String(bufferStream.toByteArray(),
                                        StandardCharsets.UTF_8));
                                    bufferStream.reset();
                                }
                                mode = QUOTE_MODE;
                            }
                        } else {
                            if (!quoteEscape) {
                                if (aByte == '\\') {
                                    quoteEscape = true;
                                } else {
                                    if (aByte != '"') {
                                        bufferStream.write(aByte);
                                    } else {
                                        if (bufferStream.size() > 0) {
                                            bufferStream.flush();
                                            fieldList.add(
                                                new String(
                                                    bufferStream.toByteArray(),
                                                    StandardCharsets.UTF_8));
                                            bufferStream.reset();
                                        }
                                        mode = NORMAL_MODE;
                                    }
                                }
                            } else {
                                bufferStream.write(aByte);
                                quoteEscape = false;
                            }
                        }
                    } else {
                        doProcess = mode == NORMAL_MODE;
                        break;
                    }
                }
                if (doProcess) {
                    if (bufferStream.size() > 0) {
                        bufferStream.flush();
                        fieldList.add(
                            new String(
                                bufferStream.toByteArray(),
                                StandardCharsets.UTF_8));
                        bufferStream.reset();
                    }
                }
            } catch (IOException ex) {
                getLogger().log(
                    java.util.logging.Level.SEVERE,
                    "exception occured", ex);
            }
            if (doProcess) {
                result = fieldList;
            }
        }
        return result;
    }


    /**
     * read field name from stream
     * @param stream source steam.
     * @return field name if the stream has valid data sequence.
     */
    static String readFieldName(InputStream stream) {
        String result = null;
        try (var processingStream = new ByteArrayOutputStream()) {
            var doProcess = true;
            while (doProcess) {
                var aByte = stream.read();
                doProcess = aByte != -1;
                if (doProcess) {
                    if (aByte == ':') {
                        doProcess = processingStream.size() > 0;
                        if (doProcess) {
                           break;
                        } 
                    } else {
                        doProcess = BasicRules.isToken(aByte); 
                    }
                }
                if (doProcess) {
                    processingStream.write(aByte);
                }
            }
            if (doProcess) {
                var tmpBuf = processingStream.toByteArray();
                result = new String(tmpBuf, StandardCharsets.UTF_8);
            }
        } catch (IOException ex) {
        }
        getLogger().log(java.util.logging.Level.FINE,
            "field name: {0}", result); 
        return result;
    }

 
    /**
     * you get true if stream might have headers .
     * @param stream Tested stream which may be have header or some headers.
     * @return true if stream might have headers.
     */
    static Boolean hasFields(InputStream stream) {
        assert stream.markSupported();
        var nextBytes = new byte[2]; 
        Boolean result = null;
        stream.mark(nextBytes.length);
        try {
            var readSize = stream.read(nextBytes);
            stream.reset();
            var doProcess = readSize == nextBytes.length;
            if (doProcess) {
                if (BasicRules.isCR(nextBytes[0])) {
                    if (BasicRules.isLF(nextBytes[1])) {
                        result = false;
                    }
                } else {
                    result = true;
                }
            }
        } catch (IOException ex) {
            result = false;
        }
        return result;
    }

    /**
     * read headers as map from stream
     * @param stream which has http header byte array. 
     */
    static Map<String, List<byte[]>> readFieldMap(InputStream stream) {
        assert stream.markSupported();
        var hasFields = hasFields(stream);
        var doProcess = true;
        doProcess = hasFields != null;
        var headers = new TreeMap<String, List<byte[]>>(
            String.CASE_INSENSITIVE_ORDER);  

        if (doProcess && hasFields) {
            while (doProcess) {
                var fieldName = readFieldName(stream);
                doProcess = fieldName != null;
                byte[] fieldValue = null;
                if (doProcess) {
                    fieldValue = readFieldValue(stream);
                    doProcess = fieldValue != null;
                }
                if (doProcess) {
                    var fieldValues = headers.get(fieldName);
                    if (fieldValues == null) {
                        fieldValues = new ArrayList<byte[]>();
                        headers.put(fieldName, fieldValues);
                    }
                    fieldValues.add(fieldValue); 
                }
                hasFields = hasFields(stream);
                doProcess = hasFields != null;
                if (doProcess) {
                    if (!hasFields) {
                        break;
                    }
                }
            }
        } 
        Map<String, List<byte[]>> result = null;
        if (doProcess) {
            result = headers;
        }
        return result;
    }
    /**
     * write headers map into output stream
     * @param headersMap headers
     * @param stream It will be written encorded data.
     */
    static void
    writeFieldMap(
        Map<String, List<byte[]>> fieldMap,
        OutputStream stream) throws IOException {

        if (fieldMap.size() > 0) {
            final var crlfBytes = new byte[] { (byte)'\r', (byte)'\n' };
            final var outStream = stream;

            final var ioExRef = new  IOException[] { null };
            fieldMap.forEach((key, values) -> {
                final var keyBytes = key.getBytes(StandardCharsets.UTF_8);
                values.forEach((value) -> {
                    if (ioExRef[0] == null) {
                        try {
                            outStream.write(keyBytes); 
                            outStream.write(':');
                            outStream.write(value);
                            outStream.write(crlfBytes);
                        } catch (IOException ex) {
                            ioExRef[0] = ex;
                        }
                    }
                });
            });
            if (ioExRef[0] != null) {
                throw ioExRef[0];
            }
            outStream.write(crlfBytes);
        }
    }
}

// vi: se ts=4 sw=4 et:

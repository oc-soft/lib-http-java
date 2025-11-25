package net.oc_soft.http;

import java.io.PushbackInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * manage chunk size and extentions
 */
public class ChunkSizeLine {

    /**
     * extensions and read size.
     */
    static record ExtensionsSize(
        Map<String, List<String>> extensions,
        int size) { 
    }

    /**
     * logger
     */
    static Logger LOGGER;

    /**
     * get logger
     */
    private synchronized static Logger getLogger() {
        if (LOGGER == null) {
            LOGGER = Logger.getLogger(ChunkSizeLine.class.getName());
        }
        return LOGGER;
    }


    /**
     * update extensions map with name stream and value stream
     * @param extensionMap extensions 
     * @param nameStream contains extension name 
     * @param valueStream contains extension value
     */
    static void updateExtensionsMap( 
        Map<String, List<String>> extensionMap,
        ByteArrayOutputStream nameStream,
        ByteArrayOutputStream valueStream) {

        if (nameStream.size() > 0) {
            try {
                nameStream.flush();
                if (valueStream.size() > 0) {
                    valueStream.flush();
                    var nameStr = new String(nameStream.toByteArray(),
                        StandardCharsets.UTF_8);
                    var valueStr = new String(valueStream.toByteArray(),
                        StandardCharsets.UTF_8);

                    var values = extensionMap.get(nameStr);
                    if (values == null) {
                        values = new ArrayList<String>();
                        extensionMap.put(nameStr, values);
                    }
                    values.add(valueStr);
                    valueStream.reset();
                }
                nameStream.reset();
            } catch (IOException ex) {
                getLogger().log(java.util.logging.Level.SEVERE,
                    "can not update map because exception is occured",
                    ex);
            }
        }
    }


    /**
     * read extensions from stream
     */
    static ExtensionsSize
    readExtensions(PushbackInputStream stream) {
        ExtensionsSize result = null;
        var extensionMap = new LinkedHashMap<String, List<String>>(); 
        var doProcess = true;
        final int NAME_MODE = 0;
        final int VALUE_MODE = 1;
        int lastByte = -1;
        var mode = NAME_MODE;
        var nameStart = false;
        var valueStart = false;
        var quoteStr = false;
        var quoteEscape = false;
        var readSize = 0;
        try (var nameStream = new ByteArrayOutputStream();
            var valueStream = new ByteArrayOutputStream()) {
            while (doProcess) {
                var aByte = stream.read();     
                doProcess = aByte != -1;
                if (doProcess) {
                    readSize++;
                    if (BasicRules.isLF(aByte)) {
                        doProcess = BasicRules.isCR(lastByte);
                        if (doProcess) {
                            doProcess = mode == NAME_MODE;
                        }
                        break;
                    } else {
                        if (mode == NAME_MODE) {
                            if (nameStream.size() == 0) {
                                if (BasicRules.isSP(aByte)
                                    || BasicRules.isHT(aByte)
                                    || BasicRules.isCR(aByte)) {
                                } else if (BasicRules.isToken(aByte)) {
                                    if (nameStart) {
                                        nameStream.write(aByte);
                                    } else {
                                        doProcess = false;
                                    }
                                } else if (aByte == ';') {
                                    if (!nameStart) {
                                        nameStart = true;
                                    } else {
                                        doProcess = false;
                                    }
                                } else {
                                    doProcess = false;
                                }
                            } else {
                                if (nameStart) {
                                    if (BasicRules.isToken(aByte)) {
                                        nameStream.write(aByte);
                                    } else if (aByte == '=') {
                                        nameStart = false;
                                        mode = VALUE_MODE;
                                    } else if (BasicRules.isSP(aByte)
                                        || BasicRules.isHT(aByte)) {
                                        nameStart = false; 
                                    } else {
                                        doProcess = false;
                                    }
                                } else {
                                    if (aByte == '=') {
                                        mode = VALUE_MODE;
                                    } else if (BasicRules.isSP(aByte)
                                        || BasicRules.isHT(aByte)) {
                                    } else {
                                        doProcess = false;
                                    }
                                }
                            }
                        } else if (mode == VALUE_MODE) {
                            if (valueStream.size() == 0) {
                                if (BasicRules.isSP(aByte)
                                    || BasicRules.isHT(aByte)) {
                                } else if (BasicRules.isToken(aByte)) {
                                    valueStream.write(aByte);
                                    valueStart = true;
                                } else if (aByte == '"') {
                                    if (!valueStart) {
                                        valueStart = true;
                                        quoteStr = true;
                                    } else {
                                        valueStart = false;
                                        quoteStr = false;
                                        mode = NAME_MODE;
                                    }
                                } else {
                                    doProcess = false;
                                }
                            } else {
                                if (valueStart) {
                                    if (!quoteStr) {
                                        if (BasicRules.isToken(aByte)) {
                                            valueStream.write(aByte);
                                        } else {
                                            stream.unread(aByte);
                                            readSize--;
                                            valueStart = false;
                                            mode = NAME_MODE;
                                        }
                                    } else {
                                        if (quoteEscape) {
                                            doProcess = BasicRules.isVCHAR(
                                                aByte);
                                            if (doProcess) {
                                                valueStream.write(aByte);
                                            }
                                        } else {
                                            if (aByte == '\\') {
                                                quoteEscape = true;
                                            } else {
                                                var isQdText =
                                                    BasicRules.isHT(aByte)
                                                    || BasicRules.isSP(aByte)
                                                    || aByte == 0x21
                                                    || (0x23 <= aByte
                                                        && aByte <= 0x5b) 
                                                    || (0x5d <= aByte
                                                        && aByte <= 0x7e) 
                                                    || BasicRules.isObsText(
                                                        aByte);
                                                if (isQdText) {
                                                    valueStream.write(aByte);
                                                } else if (aByte == '"') {
                                                    valueStart = false;
                                                    quoteStr = false;
                                                    mode = NAME_MODE;
                                                } else {
                                                    doProcess = false;
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    doProcess = false;
                                }
                            }
                            if (doProcess) {
                                if (mode == NAME_MODE) {
                                    updateExtensionsMap(extensionMap,
                                        nameStream, valueStream);
                                }
                            }
                        } else {
                            doProcess = false;
                        }
                    }
                }
                lastByte = aByte; 
            }
            if (doProcess) {
                updateExtensionsMap(extensionMap,
                    nameStream, valueStream);
                result = new ExtensionsSize(extensionMap, readSize);
            }
        } catch (IOException ex) {
            getLogger().log(java.util.logging.Level.SEVERE,
                "can not load extension map because of exception",
                ex);
        }
        return result;
    }


    /**
     * read size
     */
    static IntegerSize readSize(PushbackInputStream stream) {
        
        IntegerSize result = null;
        try (var chunkSizeBuffer = new ByteArrayOutputStream()) {
            var doProcess = true; 
            var sizeRead = 0;
            while (doProcess) {
                var aByte = stream.read();
                sizeRead++;
                doProcess = aByte != -1;
                if (doProcess) {
                    if (BasicRules.isHEXDIG(aByte)) {
                        chunkSizeBuffer.write(aByte); 
                    } else {
                        stream.unread(aByte);
                        sizeRead--;
                        break;
                    }
                }
            }
            if (doProcess) {
                if (chunkSizeBuffer.size() > 0) {
                    result = new IntegerSize(
                        Integer.valueOf(
                            new String(chunkSizeBuffer.toByteArray(),
                                    StandardCharsets.UTF_8), 16),
                        sizeRead);
                }
            }
        } catch (IOException ex) {
            getLogger().log(java.util.logging.Level.SEVERE,
                "can not read chunk size because of exception",
                ex);
        }
        return result;
    }


    /**
     * read chunk size line
     */
    static ChunkSizeLineSize read(InputStream stream) {
        ChunkSizeLineSize result = null;
        try (var bufStream = new PushbackInputStream(stream, 1)) {
            var chunkSize = readSize(bufStream);
            var doProcess = chunkSize != null; 
            ExtensionsSize extensionsSize = null;
            if (doProcess) {
                extensionsSize = readExtensions(bufStream);
                doProcess = extensionsSize != null;
            }
            if (doProcess) {
                result = new ChunkSizeLineSize(
                    new ChunkSizeLine(
                        chunkSize.value(),
                        extensionsSize.extensions()),
                    chunkSize.size() + extensionsSize.size());
            }
        } catch (IOException ex) {
            getLogger().log(java.util.logging.Level.SEVERE,
                "can not read chunk size line because of exception",
                ex);
        }
        return result;
    }
    
    /**
     * chunk size
     */
    private int chunkSize;

    /**
     * extensions
     */
    private Map<String, List<String>> extensions;

    /**
     * construct chunk size line.
     * @param chunkSize chunk size
     * @param extensions extension map
     */
    ChunkSizeLine(int chunkSize,
        Map<String, List<String>> extensions) {
        this.chunkSize = chunkSize;
        this.extensions = extensions;
    }


    /**
     * get chunk size
     */
    public int
    getChunkSize() {
        return chunkSize;
    }

    /**
     * get extensions
     */
    public Map<String, List<String>>
    getExtensions() {
        return Collections.unmodifiableMap(this.extensions);
    }

    /**
     * get extension value
     * @param name extension name
     * @return extension value
     */
    public List<String>
    getExtension(String name) {
        return Collections.unmodifiableList(extensions.get(name));
    }
}

// vi: se ts=4 sw=4 et:

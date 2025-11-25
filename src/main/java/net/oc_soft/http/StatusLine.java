package net.oc_soft.http;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import java.nio.charset.StandardCharsets;

/**
 * status line
 */
public class StatusLine {
    /**
     * read http version from stream
     */
    private static String readVersion(InputStream stream) {
        var doProcess = true;
        var httpAndSlashBuffer = new byte[5];
        try {
            int readSize = -1;  
            readSize = stream.read(httpAndSlashBuffer);
            doProcess = readSize == httpAndSlashBuffer.length;
        } catch (IOException ex) {
            doProcess = false;
        }
        String result = null;
        var strBuilder = new StringBuilder();
       
        if (doProcess) {
            var tmpStr = new String(httpAndSlashBuffer, StandardCharsets.UTF_8);
            doProcess = "HTTP/".equals(tmpStr);
            if (doProcess) {
                strBuilder.append(tmpStr);
            }
        }
        if (doProcess) {
            try (var majorStream = new ByteArrayOutputStream();
                var minorStream = new ByteArrayOutputStream()) {
                var processingStream = majorStream;
                while (doProcess) {
                    var aByte = stream.read();
                    doProcess = aByte != -1;
                    if (doProcess) {
                        if (BasicRules.isSP(aByte)) {
                            doProcess = processingStream == minorStream;
                            if (doProcess) {
                                doProcess = majorStream.size() > 0;
                            }
                            if (doProcess) {
                                doProcess = minorStream.size() > 0;
                            }
                            if (doProcess) {
                                var tmpStr = majorStream.toString(
                                    StandardCharsets.UTF_8);
                                try {
                                    Integer.valueOf(tmpStr);
                                    strBuilder.append(tmpStr);
                                } catch (NumberFormatException ex) {
                                    doProcess = false;
                                }
                            }
                            if (doProcess) {
                                var tmpStr = minorStream.toString(
                                    StandardCharsets.UTF_8);
                                try {
                                    Integer.valueOf(tmpStr);
                                    strBuilder.append(".");
                                    strBuilder.append(tmpStr);
                                    break;
                                } catch (NumberFormatException ex) {
                                    doProcess = false;
                                }
                            } 
                        } else {
                            if ('.' == aByte) {
                                doProcess = processingStream == majorStream;
                                if (doProcess) {
                                    processingStream = minorStream;
                                }
                            } else {
                                doProcess = BasicRules.isDIGIT(aByte);
                                if (doProcess) {
                                    processingStream.write(aByte);
                                }
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                doProcess = false;
            }
        }
        if (doProcess) {
            result = strBuilder.toString();
        }
        return result;
    }

    /**
     * read status code
     */
    private static String readStatusCode(InputStream stream) {
        String result = null;
        var strBuilder = new StringBuilder();
        var doProcess = true;
        try (var processingStream = new ByteArrayOutputStream()) {
            while (doProcess) {
                var aByte = stream.read();
                doProcess = aByte != -1;
                if (doProcess) {
                    if (BasicRules.isSP(aByte)) {
                        doProcess = processingStream.size() > 0;
                        if (doProcess) {
                            var tmpStr = processingStream.toString(
                                StandardCharsets.UTF_8);
                            strBuilder.append(tmpStr);
                            break;
                        }
                    } else {
                        doProcess = BasicRules.isDIGIT(aByte);
                        if (doProcess) {
                            processingStream.write(aByte);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            doProcess = false;
        }
        if (doProcess) {
            result = strBuilder.toString();
        }
        return result;
    }

    /**
     * read reason phrase
     */
    private static String readReasonPhrase(InputStream stream) {
        String result = null;
        var strBuilder = new StringBuilder();
        var doProcess = true;
        try (var processingStream = new ByteArrayOutputStream()) {
            int lastByte = -1;
            while (doProcess) {
                var aByte = stream.read();
                doProcess = aByte != -1;
                if (doProcess) {
                    if (BasicRules.isLF(aByte)) {
                        doProcess = BasicRules.isCR(lastByte);
                        if (doProcess) {
                            if (processingStream.size() > 0) {
                                var tmpBuf = processingStream.toByteArray();
                                doProcess = BasicRules.isTEXT(tmpBuf);
                                if (doProcess) {
                                    var tmpStr = new String(tmpBuf,
                                        StandardCharsets.UTF_8);
                                    strBuilder.append(tmpStr);
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    } else if (!BasicRules.isCR(aByte)) {
                        processingStream.write(aByte);
                    }
                }
                lastByte = aByte;
            }
        } catch (IOException ex) {
            doProcess = false;
        }
        if (doProcess) {
            result = strBuilder.toString();
        }
        return result;
    }

    /**
     * read status line
     * If stream dose not have valid status line, you get null.
     * @param stream respose stream
     * @return status line 
     */
    public static StatusLine read(
        InputStream stream) {
        StatusLine result = null;
        var doProcess = true;
        var version = readVersion(stream);
        doProcess = version != null;
        String statusCode = null;
        if (doProcess) {
            statusCode = readStatusCode(stream);
            doProcess = statusCode != null;
        }
        String reasonPhrase = null;
        if (doProcess) {
            reasonPhrase = readReasonPhrase(stream);
            doProcess = reasonPhrase != null;
        } 
        if (doProcess) {
            result = new StatusLine(version, statusCode, reasonPhrase);
        }
        return result; 
    }


    /**
     * http version string
     */
    private String httpVersion;

    /**
     * status code string
     */
    private String statusCode;

    /**
     * reason phrase
     */
    private String reasonPhrase;

    /**
     * construct object.
     * @param httpVersion This is like a "HTTP/X.X" string.
     * @param statusCode This is like a "200"
     * @param reasonPhrase This is like a "OK"
     */
    private
    StatusLine(
        String httpVersion,
        String statusCode,
        String reasonPhrase) { 
        this.httpVersion = httpVersion;
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * http version string
     */
    public String getHttpVersion() {
        return httpVersion;
    }

    /**
     * status code string
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * reason phrase
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }
}


// vi: se ts=4 sw=4 et:

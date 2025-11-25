package net.oc_soft.http;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * basic rules about rfc2616
 */
public class BasicRules {

    /**
     * separators
     */
    private static Set<Integer> SEPARATORS;

    /**
     * get separators
     */
    private synchronized static Set<Integer> getSeparators() {
        if (SEPARATORS == null) {
            var separators = new HashSet<Integer>();
            int[] separatorValues = {
                '(', ')', '<', '>', '@',
                ',', ';', ':', '\\', '"',
                '/', '[', ']', '?', '=',
                '{', '}', ' ', '\t'
            };
            for (var separatorValue : separatorValues) {
                separators.add(separatorValue);
            }
            SEPARATORS = Collections.unmodifiableSet(separators);
        } 
        return SEPARATORS;
    }

    /**
     * you get true if byteData is CHAR.
     * @param byteData tested whether is CHAR.
     * @return true if byteData is CHAR.
     */
    public static boolean isCHAR(int byteData) {
        return 0 <= byteData && byteData <= 127;
    } 

    /**
     * you get true if byteData is UPALPHA.
     * @param byteData tested whether is UPALPHA "A".."Z".
     * @return true if byteData is UPALPHA.
     */
    public static boolean isUPALPHA(int byteData) {
        return 0x41 <= byteData && byteData <= 0x5A;
    } 
    
    /**
     * you get true if byteData is LOALPHA.
     * @param byteData tested whether is LOALPHA "a".."z".
     * @return true if byteData is LOALPHA.
     */
    public static boolean isLOALPHA(int byteData) {
        return 0x61 <= byteData && byteData <= 0x7A;
    } 
 
    /**
     * you get true if byteData is ALPHA.
     * @param byteData tested whether is LOALPHA or UPALPHA.
     * @return true if byteData is LOALPHA or UPALPHA.
     */
    public static boolean isALPHA(int byteData) {
        return isUPALPHA(byteData) || isLOALPHA(byteData);
    } 

    /**
     * you get true if byteData is CTL.
     * @param byteData tested whether is CTL. 
     * @return true if byteData is CTL. 
     */
    public static boolean isCTL(int byteData) {
        return (0 <= byteData && byteData <= 31) || byteData == 127;
    } 

    /**
     * you get true if byteData is CR.
     * @param byteData tested whether is CR. 
     * @return true if byteData is CR. 
     */
    public static boolean isCR(int byteData) {
        return byteData == 13; 
    } 

    /**
     * you get true if byteData is LF.
     * @param byteData tested whether is LF. 
     * @return true if byteData is LF. 
     */
    public static boolean isLF(int byteData) {
        return byteData == 10; 
    } 

    /**
     * you get true if byteData is SP.
     * @param byteData tested whether is SP. 
     * @return true if byteData is SP. 
     */
    static boolean isSP(int byteData) {
        return byteData == 32; 
    } 


    /**
     * you get true if byteData is HT.
     * @param byteData tested whether is HT. 
     * @return true if byteData is HT. 
     */
    static boolean isHT(int byteData) {
        return byteData == 9; 
    } 

    /**
     * you get true if byteData is VCHAR.
     * @param byteData tested whether is VCHAR. 
     * @return true if byteData is VCHAR. 
     */
    static boolean isVCHAR(int byteData) {
        return 0x21 <= byteData && byteData <= 0x7E; 
    } 

    /**
     * you get true if byteData is HEXDIG.
     * @param byteData tested whether is HEXDIG. 
     * @return true if byteData is HEXDIG. 
     */
    static boolean isHEXDIG(int byteData) {
        return 0x30 <= byteData && byteData <= 0x39
            || 0x41 <= byteData && byteData <= 0x46
            || 0x61 <= byteData && byteData <= 0x66; 
    } 


    /**
     * you get true if byteData is double quote mark.
     * @param byteData tested whether is double quote mark. 
     * @return true if byteData is double quote mark. 
     */
    static boolean isDubleQuoteMark(int byteData) {
        return byteData == 34;
    }

    /**
     * you get true if byteData is DIGIT.
     * @param byteData tested whether is DIGIT. 
     * @return true if byteData is DIGIT. 
     */
    public static boolean isDIGIT(int byteData) {
        return 0x30 <= byteData && byteData <= 0x39;
    }

    /**
     * you get true if byteData is token.
     * @param byteData tested whether is token. 
     * @return true if byteData is token. 
     */
    static boolean isToken(int byteData) {
        var result = isCHAR(byteData);
        if (result) {
            result = !isCTL(byteData);
        }
        if (result) {
            result = !getSeparators().contains(byteData);
        }
        return result;
    }
    /**
     * you get true if byteData is obs-text code
     * @param byteData tested whether is obs-text ocd. 
     * @return true if byteData is separator. 
     */
    static boolean isObsText(int byteData) {
        return 0x80 <= byteData && byteData <= 0xFF;
    }

    /**
     * you get true if byteData is separator.
     * @param byteData tested whether is separator. 
     * @return true if byteData is separator. 
     */
    static boolean isSeparator(int byteData) {
        return getSeparators().contains(byteData);
    }

 
    /**
     * you get true if data is TEXT.
     * @param data tested where are TEXT.
     * @return true if data is TEXT.
     */
    static boolean isTEXT(byte[] data) {
        var result = data != null && data.length > 0;
        if (result) {
            final int NORMAL_MODE = 0;
            final int LWS_MODE = 1;
            var lastData = -1;
            var mode = NORMAL_MODE; 

            for (int idx = 0; idx < data.length; idx++) {
                int curData = data[idx];
                if (mode == NORMAL_MODE) {
                    if (isLF(curData)) {
                        result = isCR(lastData);
                        if (result) {
                            mode = LWS_MODE;
                        }
                    } else {
                        if (isSP(curData) || isHT(curData)) {
                            result = true;
                        } else if (isCR(curData)) {
                            result = idx != 0 && !isCTL(lastData);
                        } else {
                            result = !isCTL(curData);
                        }
                    }
                } else {
                    if (isSP(curData) || isHT(curData)) {
                        mode = NORMAL_MODE;
                    } else {
                        result = false;
                    }
                }
                lastData = curData;
                if (!result) {
                    break;
                }
            }
        }
        return result;
    }


    /**
     * read quoted string from input stream. stream will be stop at next
     * position of '"'.
     * @param stream input stream
     * @return qdtext
     */
    static byte[] readQuotedString(InputStream stream) {
        byte[] result = null;
        try (var processingStream = new ByteArrayOutputStream()) {
            var doProcess = true; 
            var lastByte = -1; 
            while (doProcess) {
                var aByte = stream.read();
                doProcess = aByte != -1;
                if (doProcess) {
                    if (lastByte == '\\') {
                        doProcess = isCHAR(aByte);
                        if (doProcess) {
                            processingStream.write(aByte);
                        }
                    } else {
                        if (aByte != '"') {
                            if (aByte != '\\') {
                                if (isHT(aByte) || isSP(aByte)) {
                                    doProcess = true;
                                } else {  
                                    if (isLF(aByte)) {
                                        doProcess = isCR(lastByte);
                                    } else { 
                                        doProcess = !isLF(lastByte);
                                        if (doProcess) {
                                            doProcess = !isCTL(aByte);
                                        }
                                    }
                                }
                                if (doProcess) { 
                                    processingStream.write(aByte);
                                }
                            }
                        } else {
                            break;
                        }
                    }
                    lastByte = aByte;
                }
            }
            if (doProcess) {
                result = processingStream.toByteArray();
            }
        } catch (IOException ex) {
        }
        return result;
    }
}

// vi: se ts=4 sw=4 et:

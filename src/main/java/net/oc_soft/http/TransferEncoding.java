package net.oc_soft.http;

/**
 * manage trasfer-encoding header fields
 */
public class TransferEncoding {

    /**
     * you get true if header has transfer-encoding field end with chunked. 
     */
    public static boolean endsWithChunked(Headers headers) {
        var result = false;
        var fieldValues = headers.getTransferEncoding();
        if (fieldValues != null && fieldValues.size() > 0) {
            result = "chunked".equals(
                fieldValues.get(fieldValues.size() - 1));
        }
        return result;
    }
}


// vi: se ts=4 sw=4 et:

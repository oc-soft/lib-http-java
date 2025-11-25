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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * http response headers.
 */
public class Headers {

    /**
     * logger
     */
    static Logger LOGGER;

    /**
     * get logger
     */
    private synchronized static Logger getLogger() {
        if (LOGGER == null) {
            LOGGER = Logger.getLogger(Headers.class.getName());
        }
        return LOGGER;
    }

    /**
     * read headers from stream
     * @param stream It has byte stream about http headers.
     */
    public static Headers read(InputStream stream) {
        Headers result = null;

        var nameValueMap = FieldMap.readFieldMap(stream);
        if (nameValueMap != null) {
            result = new Headers(nameValueMap);
        }
        return result;
    }


    /**
     * write headers into stream
     */
    public static void
    write(Headers headers, OutputStream stream) throws IOException {
        FieldMap.writeFieldMap(headers.nameValues, stream);
    }
    

    /**
     * map of field name to field value
     */
    private Map<String, List<byte[]>> nameValues;


    /**
     * construct headers.
     * @param nameValue It is nameValues field value.
     */
    private Headers(Map<String, List<byte[]>> nameValues) {
        assert nameValues != null;
        this.nameValues = nameValues;
    }

    /**
     * get field value associated with field name.
     * @param fieldName field name
     * @return field value. if this object dose not have field value associated
     * with fieldName, you will get null.
     */
    public List<byte[]> getFieldValue(String fieldName) {
        var result = nameValues.get(fieldName);
        if (result != null) {
            result = Collections.unmodifiableList(result);
        }
        return result;
    }

    /**
     * get field names 
     */
    public Set<String> getFieldNames() {
        return nameValues.keySet();
    }


    /**
     * write this object into stream.
     * @param stream output stream.
     */
    public void write(OutputStream stream) throws IOException {
        write(this, stream);
    }

    /**
     * You get true if this object has specified field name value
     * @param fieldName field name
     * @return You get true if this object has specified field name value.
     */
    public boolean containsField(String fieldName) {
        return nameValues.containsKey(fieldName);
    }
     

    /**
     * get content length. If headers dose not have Content-Length: field, you
     * got null.
     * @return content length.
     */
    public Integer getContentLength() {
        Integer result = null;
        var values = nameValues.get("Content-Length");
        if (values != null && values.size() > 0) {
            var lengthSet = new HashSet<Integer>();

            var doProcess = true;

            VALUES_LOOP: for (var value : values) {
                var strValues = new String(value, StandardCharsets.UTF_8);

                var strValuesArray = strValues.split(",");  
                for (var strValue : strValuesArray) {
                    try {
                        lengthSet.add(Integer.valueOf(strValue.trim())); 
                    } catch (NumberFormatException ex) {
                        doProcess = false;
                    }
                    if (!doProcess) {
                        break VALUES_LOOP;
                    }
                }
            }
            if (doProcess) {
                doProcess = lengthSet.size() == 1;
            }
            if (doProcess) {
                result = lengthSet.iterator().next();
            }
        }
        return result;
    }

    /**
     * get transfer encoding
     * @return transfer coding list
     */
    public List<String> getTransferEncoding() {
        List<String> result = null;
        var values = nameValues.get("Transfer-Encoding");
        if (values != null &&  values.size() > 0) {
            var codings = new ArrayList<String>();
            var doProcess = true;
            for (var value : values) {
                var fieldValues = FieldMap.parseFieldValue(value); 
                doProcess = fieldValues != null;
                if (doProcess) {
                    var fieldValue = String.join("", fieldValues);
                    var strValuesArray = fieldValue.split(",");  
                    for (var strValue : strValuesArray) {
                        var coding = strValue.trim();
                        if (!coding.isEmpty()) { 
                            codings.add(coding); 
                        }
                    }
                }
                if (!doProcess) {
                    break;
                }
            }
            if (doProcess) {
                result = codings;
            }
        }
        return result; 
    }
}

// vi: se ts=4 sw=4 et:

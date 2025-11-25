package net.oc_soft.http;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;


/**
 * http response.
 */
public class Response {

    /**
     * create response object from http response stream
     * @param stream contains http response
     * @param memoryBodySize threshould about body contents into file.
     * @return response object
     */
    public static Response read(
        InputStream stream, int memoryBodySize) {
        var sourceStream = stream;
        if (!stream.markSupported()) {
            sourceStream = new BufferedInputStream(stream, 0xf);
        }

        var statusLine = StatusLine.read(sourceStream);  
        var doProcess = statusLine != null;
        Headers headers = null;
        if (doProcess) {
            headers = Headers.read(sourceStream);
            doProcess = headers != null;
        }
        if (doProcess) {
            try {
                var endOfHeads = new byte[2];
                var readSize = sourceStream.read(endOfHeads);
                doProcess = readSize == endOfHeads.length;    
                if (doProcess) {
                    doProcess = BasicRules.isCR(endOfHeads[0])
                        && BasicRules.isLF(endOfHeads[1]);
                }
            } catch (IOException ex) {
                doProcess = false;
            }
        }
        Body body = null;
        if (doProcess) {
            body = Body.read(sourceStream, memoryBodySize);
        } 
        Response result = null;
        if (doProcess) {
            result = new Response(statusLine, headers, body);
        }
        return result;
    } 

    /**
     * status line
     */
    private StatusLine statusLine;

    /**
     * response header
     */
    private Headers headers;


    /**
     * response body 
     */
    private Body body;

    /**
     * construct response
     * @param statusLine status line.
     * @param headres headers.
     * @param response body
     */
    Response(StatusLine statusLine,
        Headers headers,
        Body body) {
        this.statusLine = statusLine;
        this.headers = headers;
        this.body = body;
    }
    
    /**
     * status line
     */
    public StatusLine getStatusLine() {
        return statusLine;
    }
    /**
     * response header
     */
    public Headers getHeaders() {
        return headers;
    }
    /**
     * response body 
     */
    public Body getBody() {
       return body;
    }
}

// vi: se ts=4 sw=4 et:

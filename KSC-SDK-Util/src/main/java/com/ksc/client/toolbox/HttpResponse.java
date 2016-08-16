package com.ksc.client.toolbox;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Alamusi on 2016/7/7.
 */
public class HttpResponse {
    /* The HTTP status code */
    private int mStatusCode;
    /* Raw data from this response */
    private byte[] mData;
    /* Response headers*/
    private Map<String, String> mHeaders;
    /* True if the server returned a 304 (Not Modified). */
    private boolean mNotModified;

    /**
     * Create a new Http Response
     *
     * @param statusCode  the HTTP status code
     * @param data        data Response body
     * @param headers     Headers returned with this response, or null for none
     * @param notModified True if the server returned a 304
     */
    public HttpResponse(int statusCode, byte[] data, Map<String, String> headers, boolean notModified) {
        mStatusCode = statusCode;
        mData = data;
        mHeaders = headers;
        mNotModified = notModified;
    }

    public HttpResponse(byte[] data) {
        this(HttpURLConnection.HTTP_OK, data, Collections.<String, String>emptyMap(), false);
    }

    public HttpResponse(byte[] data, Map<String, String> headers) {
        this(HttpURLConnection.HTTP_OK, data, headers, false);
    }

    public int getCode() {
        return mStatusCode;
    }

    public byte[] getBody() {
        return mData;
    }

    public String getBodyString() {
        return new String(mData);
    }
}

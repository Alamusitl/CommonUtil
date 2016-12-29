package com.afk.client.toolbox;

import java.util.Map;

/**
 * Created by Alamusi on 2016/7/7.
 */
public class HttpRequestParam {

    public static final int METHOD_GET = 0;
    public static final int METHOD_POST = 1;
    public static final int DEFAULT_TIME_OUT_MS = 20 * 1000;

    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";
    private String mContentType = "application/x-www-form-urlencoded";
    private String mUrl;
    private String mDownloadPath;
    private int mMethod;
    private int mTimeOutMs;
    private byte[] mPostParams;
    private Map<String, String> mHeaders;

    public HttpRequestParam(String url) {
        this(url, METHOD_GET);
    }

    public HttpRequestParam(String url, int method) {
        mUrl = url;
        mMethod = method;
        mTimeOutMs = DEFAULT_TIME_OUT_MS;
        mPostParams = null;
        mHeaders = null;
    }

    public String getUrl() {
        return mUrl;
    }

    public int getMethod() {
        return mMethod;
    }

    public int getTimeOutMs() {
        return mTimeOutMs;
    }

    public void setTimeOutMs(int timeOutMs) {
        mTimeOutMs = timeOutMs;
    }

    public byte[] getBody() {
        return mPostParams;
    }

    public void setBody(byte[] body) {
        mPostParams = body;
    }

    public void setBody(String params) {
        mPostParams = params.getBytes();
    }

    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    public void setHeaders(Map<String, String> headers) {
        mHeaders = headers;
    }

    private String getParamsEncoding() {
        return DEFAULT_PARAMS_ENCODING;
    }

    public void setContentType(String contentType) {
        mContentType = contentType;
    }

    public String getBodyContentType() {
        return mContentType + "; charset=" + getParamsEncoding();
    }

    public String getDownloadPath() {
        return mDownloadPath;
    }

    public void setDownloadPath(String path) {
        mDownloadPath = path;
    }

}


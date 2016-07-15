package com.ksc.client.toolbox;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by Alamusi on 2016/7/7.
 */
public class HttpRequestParam {

    public static final int METHOD_GET = 0;
    public static final int METHOD_POST = 1;
    public static final int DEFAULT_TIME_OUT_MS = 3 * 1000;
    public static final int TYPE_DOWNLOAD_FILE = 1;
    public static final int TYPE_GET_PARAM = 2;
    protected static final int DOWNLOAD_FILE_START = 10000;
    protected static final int DOWNLOAD_FILE_TOTAL = 10001;
    protected static final int DOWNLOAD_FILE_CURRENT = 10002;
    protected static final int DOWNLOAD_FILE_DONE = 10003;
    protected static final int DOWNLOAD_FILE_FAIL = 10004;
    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";
    private String mUrl;
    private int mMethod;
    private int mTimeOutMs;
    private Map<String, String> mPostParams;
    private int mRequestType;

    public HttpRequestParam(String url) {
        this(url, METHOD_GET);
    }

    public HttpRequestParam(String url, int method) {
        mUrl = url;
        mMethod = method;
        mTimeOutMs = DEFAULT_TIME_OUT_MS;
        mPostParams = null;
        mRequestType = TYPE_GET_PARAM;
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

    public int getRequestType() {
        return mRequestType;
    }

    public void setRequestType(int type) {
        mRequestType = type;
    }

    public byte[] getBody() {
        Map<String, String> params = mPostParams;
        if (params != null && params.size() > 0) {
            return encodeParameter(params, getParamsEncoding());
        }
        return null;
    }

    public void setBody(Map<String, String> params) {
        mPostParams = params;
    }

    private byte[] encodeParameter(Map<String, String> params, String paramsEncoding) {
        try {
            StringBuilder encodedParams = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append("=");
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append("&");
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, e);
        }
    }

    private String getParamsEncoding() {
        return DEFAULT_PARAMS_ENCODING;
    }

    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }

}
package com.ksc.client.toolbox;

/**
 * Created by Alamusi on 2016/7/7.
 */
public class HttpError extends Exception {
    public final HttpResponse httpResponse;

    public HttpError() {
        httpResponse = null;
    }

    public HttpError(HttpResponse response) {
        httpResponse = response;
    }

    public HttpError(String exception) {
        super(exception);
        httpResponse = null;
    }

    public HttpError(String exceptionMessage, Throwable reason) {
        super(exceptionMessage, reason);
        httpResponse = null;
    }

    public HttpError(Throwable cause) {
        super(cause);
        httpResponse = null;
    }
}

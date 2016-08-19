package com.ksc.sdk.analytics.exceptions;

/**
 * 返回数据收集异常
 * Created by Alamusi on 2016/7/15.
 */
public class ResponseErrorException extends Exception {

    public ResponseErrorException(String error) {
        super(error);
    }

    public ResponseErrorException(Throwable throwable) {
        super(throwable);
    }

}

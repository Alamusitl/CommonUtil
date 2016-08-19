package com.ksc.sdk.analytics.exceptions;

/**
 * EventName, 数据错误
 * Created by Alamusi on 2016/7/15.
 */
public class InvalidDataException extends Exception {

    public InvalidDataException(String error) {
        super(error);
    }

    public InvalidDataException(Throwable throwable) {
        super(throwable);
    }

}

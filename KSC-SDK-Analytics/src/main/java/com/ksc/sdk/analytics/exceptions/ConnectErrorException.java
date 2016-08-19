package com.ksc.sdk.analytics.exceptions;

/**
 * 网络连接错误
 * Created by Alamusi on 2016/7/15.
 */
public class ConnectErrorException extends Exception {

    private int mRetry;

    public ConnectErrorException(String error) {
        super(error);
        mRetry = 30 * 1000;
    }

    public ConnectErrorException(String error, String retry) {
        super(error);
        try {
            mRetry = Integer.parseInt(retry);
        } catch (NumberFormatException e) {
            mRetry = 0;
        }
    }

    public ConnectErrorException(Throwable throwable) {
        super(throwable);
    }

    public int getRetry() {
        return mRetry;
    }
}

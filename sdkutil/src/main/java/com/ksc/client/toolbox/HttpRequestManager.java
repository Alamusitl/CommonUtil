package com.ksc.client.toolbox;

import android.os.Handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Alamusi on 2016/7/12.
 */
public class HttpRequestManager {

    public static final int DOWNLOAD_FILE_START = 10000;
    public static final int DOWNLOAD_FILE_TOTAL = 10001;
    public static final int DOWNLOAD_FILE_CURRENT = 10002;
    public static final int DOWNLOAD_FILE_DONE = 10003;
    public static final int DOWNLOAD_FILE_FAIL = 10004;
    private static ExecutorService mFixedThreadPool;

    public static void init() {
        if (mFixedThreadPool == null) {
            mFixedThreadPool = Executors.newFixedThreadPool(4);
        }
    }

    public static void execute(HttpRequestParam requestParam, HttpListener listener, HttpErrorListener errorListener) {
        HttpRequestRunnable request = new HttpRequestRunnable(requestParam, listener, errorListener);
        mFixedThreadPool.execute(request);
    }

    public static long execute(HttpRequestParam requestParam, HttpListener listener, HttpErrorListener errorListener, Handler handler) {
        HttpRequestThread request = new HttpRequestThread(requestParam, listener, errorListener);
        request.setHandler(handler);
        mFixedThreadPool.execute(request);
        return request.getId();
    }


    public static void destroy() {
        if (mFixedThreadPool != null && !mFixedThreadPool.isShutdown()) {
            mFixedThreadPool.shutdownNow();
        }
    }

}

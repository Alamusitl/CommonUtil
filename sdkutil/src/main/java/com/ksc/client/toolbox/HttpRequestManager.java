package com.ksc.client.toolbox;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Alamusi on 2016/7/12.
 */
public class HttpRequestManager {

    private static ExecutorService mFixedThreadPool;

    public static void init() {
        if (mFixedThreadPool == null) {
            mFixedThreadPool = Executors.newFixedThreadPool(4);
        }
    }

    public static void execute(HttpRequestParam requestParam, HttpListener listener, HttpErrorListener errorListener) {
        HttpRequest request = new HttpRequest(requestParam, listener, errorListener);
        mFixedThreadPool.execute(request);
    }

    public static void execute(HttpRequestParam requestParam, HttpListener listener, HttpErrorListener errorListener, final ProgressDialog dialog) {
        HttpRequest request = new HttpRequest(requestParam, listener, errorListener);
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HttpRequestParam.DOWNLOAD_FILE_START:
                        dialog.show();
                        break;
                    case HttpRequestParam.DOWNLOAD_FILE_TOTAL:
                        dialog.setMax((Integer) msg.obj);
                        break;
                    case HttpRequestParam.DOWNLOAD_FILE_CURRENT:
                        dialog.setProgress((Integer) msg.obj);
                        break;
                    case HttpRequestParam.DOWNLOAD_FILE_DONE:
                        dialog.cancel();
                        break;
                    case HttpRequestParam.DOWNLOAD_FILE_FAIL:
                        dialog.cancel();
                        break;
                }
            }
        };
        request.setHandler(handler);
        mFixedThreadPool.execute(request);
    }

    public static void destroy() {
        if (mFixedThreadPool != null && !mFixedThreadPool.isShutdown()) {
            mFixedThreadPool.shutdown();
        }
    }

}

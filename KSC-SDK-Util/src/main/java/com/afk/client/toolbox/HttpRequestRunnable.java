package com.afk.client.toolbox;

import android.os.Handler;

import com.afk.client.util.Logger;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alamusi on 2016/7/7.
 */
public class HttpRequestRunnable implements Runnable {

    private HttpRequestParam mRequestParams;
    private Map<String, String> mHeaders;
    private HttpListener mHttpListener;
    private HttpErrorListener mHttpErrorListener;
    private volatile boolean mIsRunning = false;
    private Handler mHandler = null;

    public HttpRequestRunnable(HttpRequestParam requestParam, HttpListener httpListener, HttpErrorListener httpErrorListener) {
        mRequestParams = requestParam;
        mHeaders = requestParam.getHeaders();
        mHttpListener = httpListener;
        mHttpErrorListener = httpErrorListener;
    }

    @Override
    public void run() {
        try {
            mIsRunning = true;
            String url = mRequestParams.getUrl();
            URL parseUrl = new URL(url);
            HttpURLConnection connection = HttpUtils.openConnection(parseUrl, mRequestParams);
            if (connection == null) {
                Logger.e("open connection fail, url: " + url);
                if (mHttpErrorListener != null) {
                    mHttpErrorListener.onErrorResponse(new HttpError("connection can not be null"));
                }
                return;
            }
            if (mHeaders != null && mHeaders.size() > 0) {
                for (String headerName : mHeaders.keySet()) {
                    connection.addRequestProperty(headerName, mHeaders.get(headerName));
                }
            }
            HttpUtils.setConnectionParametersForRequest(connection, mRequestParams);
            connection.connect();
            int responseCode = connection.getResponseCode();
            Logger.d("Connection response code : " + responseCode + ", url=" + url);
            if (responseCode == -1) {
                throw new IOException("Could not retrieve response code from HttpUrlConnection");
            }
            byte[] body = new byte[0];
            if (responseCode == HttpURLConnection.HTTP_OK) {
                if (mHandler != null) {
                    body = processDownloadFile(connection);
                } else {
                    body = processGetParam(connection);
                }
            }
            Map<String, String> responseHeaders = new HashMap<>();
            for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
                if (header.getKey() != null) {
                    responseHeaders.put(header.getKey(), header.getValue().get(0));
                }
            }
            if (mHttpListener != null) {
                mHttpListener.onResponse(new HttpResponse(responseCode, body, responseHeaders, false));
            }
            connection.disconnect();
            mIsRunning = false;
        } catch (MalformedURLException e) {
            mIsRunning = false;
            Logger.e("can not malformed url:" + mRequestParams.getUrl());
            if (mHttpErrorListener != null) {
                mHttpErrorListener.onErrorResponse(new HttpError(e.getMessage()));
            }
        } catch (IOException e) {
            mIsRunning = false;
            Logger.e("can not open connection, url:" + mRequestParams.getUrl());
            if (mHttpErrorListener != null) {
                mHttpErrorListener.onErrorResponse(new HttpError(e.getMessage()));
            }
        }
    }

    private byte[] processGetParam(HttpURLConnection connection) throws IOException {
        InputStream inputStream = connection.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buf = new byte[100];
        int rc;
        while ((rc = inputStream.read(buf, 0, 100)) > 0) {
            byteArrayOutputStream.write(buf, 0, rc);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private byte[] processDownloadFile(HttpURLConnection connection) throws IOException {
        int totalSize = connection.getContentLength();
        BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
        FileOutputStream fos = new FileOutputStream(new File(mRequestParams.getDownloadPath()));
        int length;
        int currentSize = 0;
        byte[] buf = new byte[1024 * 4];
        mHandler.sendMessage(mHandler.obtainMessage(HttpRequestManager.DOWNLOAD_FILE_START));
        while (((length = bis.read(buf)) != -1) && mIsRunning) {
            currentSize += length;
            fos.write(buf, 0, length);
            mHandler.sendMessage(mHandler.obtainMessage(HttpRequestManager.DOWNLOAD_FILE_CURRENT, currentSize));
        }
        if (currentSize == totalSize) {
            mHandler.sendMessage(mHandler.obtainMessage(HttpRequestManager.DOWNLOAD_FILE_DONE));
        } else {
            mHandler.sendMessage(mHandler.obtainMessage(HttpRequestManager.DOWNLOAD_FILE_FAIL));
        }
        fos.close();
        bis.close();
        String msg = "Download File Success";
        return msg.getBytes();
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public void stopThread() {
        mIsRunning = false;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public long getThreadId() {
        return Thread.currentThread().getId();
    }
}

package com.ksc.client.toolbox;

import android.os.Handler;

import com.ksc.client.util.KSCLog;
import com.ksc.client.util.KSCStorageUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alamusi on 2016/7/7.
 */
public class HttpRequestThread extends Thread {

    private HttpRequestParam mRequestParams;
    private Map<String, String> mHeaders;
    private HttpListener mHttpListener;
    private HttpErrorListener mHttpErrorListener;
    private Handler mHandler;
    private String mPath;

    public HttpRequestThread(HttpRequestParam requestParam, HttpListener httpListener, HttpErrorListener httpErrorListener) {
        mRequestParams = requestParam;
        mHeaders = requestParam.getHeaders();
        mHttpListener = httpListener;
        mHttpErrorListener = httpErrorListener;

    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void run() {
        try {
            KSCLog.i("Runnable Id : " + Thread.currentThread().getId());
            String url = mRequestParams.getUrl();
            URL parseUrl = new URL(url);
            HttpURLConnection connection = HttpUtils.openConnection(parseUrl, mRequestParams);
            if (connection == null) {
                KSCLog.e("open connection fail, url: " + url);
                mHttpErrorListener.onErrorResponse(new HttpError("connection can not be null"));
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
            KSCLog.i("Connection response code : " + responseCode);
            if (responseCode == -1) {
                throw new IOException("Could not retrieve response code from HttpUrlConnection");
            }
            byte[] body = new byte[0];
            if (responseCode == HttpURLConnection.HTTP_OK) {
                body = processDownloadFile(connection);
            }
            Map<String, String> responseHeaders = new HashMap<>();
            for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
                if (header.getKey() != null) {
                    responseHeaders.put(header.getKey(), header.getValue().get(0));
                }
            }
            mHttpListener.onResponse(new HttpResponse(responseCode, body, responseHeaders, false));
            connection.disconnect();
        } catch (MalformedURLException e) {
            KSCLog.e("can not malformed url:" + mRequestParams.getUrl());
            mHttpErrorListener.onErrorResponse(new HttpError(e.getMessage()));
        } catch (IOException e) {
            KSCLog.e("can not open connection, url:" + mRequestParams.getUrl());
            mHttpErrorListener.onErrorResponse(new HttpError(e.getMessage()));
        }
    }

    private byte[] processDownloadFile(HttpURLConnection connection) throws IOException {
        int totalSize = connection.getContentLength();
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(HttpRequestManager.DOWNLOAD_FILE_TOTAL, totalSize));
        }

        BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
        mPath = KSCStorageUtils.getDownloadDir(totalSize);
        if (mPath == null) {
            String msg = "Download File Failed, Download Space is null";
            return msg.getBytes();
        }
        String[] list = connection.getURL().toString().split("/");
        String name = list[list.length - 1];
        mPath = mPath + File.separator + name;
        KSCLog.i(mPath);
        File file = new File(mPath);
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    KSCLog.e("mkdirs dir " + file.getParentFile().getName() + " failed!");
                }
            }
            if (!file.createNewFile()) {
                KSCLog.e("create file " + file.getName() + " failed!");
            }
        }
        FileOutputStream fos = new FileOutputStream(file);
        int length;
        int currentSize = 0;
        byte[] buf = new byte[1024 * 4];
        mHandler.sendMessage(mHandler.obtainMessage(HttpRequestManager.DOWNLOAD_FILE_START));
        while (((length = bis.read(buf)) != -1) && !isInterrupted()) {
            currentSize += length;
            fos.write(buf, 0, length);
            mHandler.sendMessage(mHandler.obtainMessage(HttpRequestManager.DOWNLOAD_FILE_CURRENT, currentSize));
        }
        if (currentSize == totalSize) {
            mHandler.sendMessage(mHandler.obtainMessage(HttpRequestManager.DOWNLOAD_FILE_DONE, file.getAbsolutePath()));
        } else {
            mHandler.sendMessage(mHandler.obtainMessage(HttpRequestManager.DOWNLOAD_FILE_FAIL));
        }
        fos.close();
        bis.close();
        String msg = "Download File Success";
        return msg.getBytes();
    }

    public String getDownloadPath() {
        return mPath;
    }
}

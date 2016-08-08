package com.ksc.client.toolbox;

import com.ksc.client.util.KSCLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    public HttpRequestRunnable(HttpRequestParam requestParam, HttpListener httpListener, HttpErrorListener httpErrorListener) {
        mRequestParams = requestParam;
        mHeaders = requestParam.getHeaders();
        mHttpListener = httpListener;
        mHttpErrorListener = httpErrorListener;
    }

    public void stop() {
        mIsRunning = false;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    @Override
    public void run() {
        try {
            mIsRunning = true;
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
                body = processGetParam(connection);
            }
            Map<String, String> responseHeaders = new HashMap<>();
            for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
                if (header.getKey() != null) {
                    responseHeaders.put(header.getKey(), header.getValue().get(0));
                }
            }
            mHttpListener.onResponse(new HttpResponse(responseCode, body, responseHeaders, false));
            connection.disconnect();
            mIsRunning = false;
        } catch (MalformedURLException e) {
            KSCLog.e("can not malformed url:" + mRequestParams.getUrl());
            mHttpErrorListener.onErrorResponse(new HttpError(e.getMessage()));
        } catch (IOException e) {
            KSCLog.e("can not open connection, url:" + mRequestParams.getUrl());
            mHttpErrorListener.onErrorResponse(new HttpError(e.getMessage()));
        }
    }

    private byte[] processGetParam(HttpURLConnection connection) throws IOException {
        byte[] body;
        BufferedReader bfr = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder result = new StringBuilder();
        while (((line = bfr.readLine()) != null) && mIsRunning) {
            result.append(line);
        }
        body = result.toString().getBytes();
        bfr.close();
        return body;
    }
}

package com.ksc.client.toolbox;

import android.os.Handler;
import android.os.Message;

import com.ksc.client.util.KSCLog;
import com.ksc.client.util.KSCStorageUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
public class HttpRequest implements Runnable {

    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private HttpRequestParam mRequestParams;
    private Map<String, String> mHeaders;
    private HttpListener mHttpListener;
    private HttpErrorListener mHttpErrorListener;
    private Handler mHandler;

    public HttpRequest(HttpRequestParam requestParam, HttpListener httpListener, HttpErrorListener httpErrorListener) {
        this(requestParam, null, httpListener, httpErrorListener);
    }

    public HttpRequest(HttpRequestParam requestParam, Map<String, String> headers, HttpListener httpListener, HttpErrorListener httpErrorListener) {
        mRequestParams = requestParam;
        mHeaders = headers;
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
            HttpURLConnection connection = openConnection(parseUrl, mRequestParams);
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
            setConnectionParametersForRequest(connection, mRequestParams);
            connection.connect();
            int responseCode = connection.getResponseCode();
            KSCLog.i("Connection response code : " + responseCode);
            if (responseCode == -1) {
                throw new IOException("Could not retrieve response code from HttpUrlConnection");
            }
            byte[] body = new byte[0];
            if (responseCode == HttpURLConnection.HTTP_OK) {
                switch (mRequestParams.getRequestType()) {
                    case HttpRequestParam.TYPE_GET_PARAM:
                        body = processGetParam(connection);
                        break;
                    case HttpRequestParam.TYPE_DOWNLOAD_FILE:
                        body = processDownloadFile(connection);
                        break;
                    default:
                        break;
                }
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

    /**
     * Opens an {@link HttpURLConnection} with parameters
     *
     * @param parseUrl     target url
     * @param requestParam target {@link HttpRequestParam}
     * @return an opening {@link HttpURLConnection
     * @throws IOException
     */
    private HttpURLConnection openConnection(URL parseUrl, HttpRequestParam requestParam) throws IOException {
        HttpURLConnection connection;
        connection = (HttpURLConnection) parseUrl.openConnection();
        connection.setConnectTimeout(requestParam.getTimeOutMs());
        connection.setReadTimeout(requestParam.getTimeOutMs());
        connection.setUseCaches(false);
        connection.setDoInput(true);
        return connection;
    }

    /**
     * set Params on {@link HttpURLConnection} with {@link HttpRequestParam}
     *
     * @param connection   target {@link HttpURLConnection}
     * @param requestParam source {@link HttpRequestParam}
     * @throws IOException
     */
    private void setConnectionParametersForRequest(HttpURLConnection connection, HttpRequestParam requestParam) throws IOException {
        switch (requestParam.getMethod()) {
            case HttpRequestParam.METHOD_GET:
                connection.setRequestMethod("GET");
                break;
            case HttpRequestParam.METHOD_POST:
                connection.setRequestMethod("POST");
                addBodyIfExists(connection, requestParam);
                break;
            default:
                throw new IllegalArgumentException("Unknown method type.");
        }
    }

    /**
     * 附加参数
     *
     * @param connection   请求的connection
     * @param requestParam 参数
     * @throws IOException
     */
    private void addBodyIfExists(HttpURLConnection connection, HttpRequestParam requestParam) throws IOException {
        byte[] body = requestParam.getBody();
        if (body != null) {
            connection.setDoOutput(true);
            connection.addRequestProperty(HEADER_CONTENT_TYPE, requestParam.getBodyContentType());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(body);
            out.flush();
            out.close();
        }
    }

    private byte[] processDownloadFile(HttpURLConnection connection) throws IOException {
        int totalSize = connection.getContentLength();
        if (mHandler != null) {
            Message message = mHandler.obtainMessage();
            message.what = HttpRequestParam.DOWNLOAD_FILE_TOTAL;
            message.obj = 100;
        }

        BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
        String path = KSCStorageUtils.getDownloadDir(totalSize);
        if (path == null) {
            String msg = "Download File Failed, Download Space is null";
            return msg.getBytes();
        }
        String[] list = connection.getURL().toString().split("/");
        String name = list[list.length - 1];
        path = path + File.separator + name;
        KSCLog.i(path);
        File file = new File(path);
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
        mHandler.sendMessage(mHandler.obtainMessage(HttpRequestParam.DOWNLOAD_FILE_START));
        while ((length = bis.read(buf)) != -1) {
            currentSize += length;
            fos.write(buf, 0, length);
            int present = (int) ((currentSize * 100) / (float) totalSize);
            KSCLog.i(present + "");
            mHandler.sendMessage(mHandler.obtainMessage(HttpRequestParam.DOWNLOAD_FILE_CURRENT, present));
        }
        if (currentSize == totalSize) {
            mHandler.sendMessage(mHandler.obtainMessage(HttpRequestParam.DOWNLOAD_FILE_DONE, file.getAbsolutePath()));
        } else {
            mHandler.sendMessage(mHandler.obtainMessage(HttpRequestParam.DOWNLOAD_FILE_FAIL));
        }
        fos.close();
        bis.close();
        String msg = "Download File Success";
        return msg.getBytes();
    }

    private byte[] processGetParam(HttpURLConnection connection) throws IOException {
        byte[] body;
        BufferedReader bfr = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = bfr.readLine()) != null) {
            result.append(line);
        }
        body = result.toString().getBytes();
        bfr.close();
        return body;
    }
}

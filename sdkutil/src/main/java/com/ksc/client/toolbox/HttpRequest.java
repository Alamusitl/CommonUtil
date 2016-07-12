package com.ksc.client.toolbox;

import com.ksc.client.util.KSCLog;

import java.io.BufferedReader;
import java.io.DataOutputStream;
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
public class HttpRequest extends Thread {

    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private HttpRequestParam mRequestParams;
    private Map<String, String> mHeaders;
    private HttpListener mHttpListener;
    private HttpErrorListener mHttpErrorListener;

    public HttpRequest(HttpRequestParam requestParam, HttpListener httpListener, HttpErrorListener httpErrorListener) {
        this(requestParam, null, httpListener, httpErrorListener);
    }

    public HttpRequest(HttpRequestParam requestParam, Map<String, String> headers, HttpListener httpListener, HttpErrorListener httpErrorListener) {
        mRequestParams = requestParam;
        mHeaders = headers;
        mHttpListener = httpListener;
        mHttpErrorListener = httpErrorListener;
    }

    @Override
    public void run() {
        try {
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
            if (responseCode == -1) {
                throw new IOException("Could not retrieve response code from HttpUrlConnection");
            }
            byte[] body = new byte[0];
            Map<String, String> responseHeaders = new HashMap<>();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStreamReader isr = new InputStreamReader(connection.getInputStream());
                BufferedReader bfr = new BufferedReader(isr);
                String line;
                StringBuilder result = new StringBuilder();
                while ((line = bfr.readLine()) != null) {
                    result.append(line);
                }
                body = result.toString().getBytes();
                isr.close();
                bfr.close();
            }
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

    public void performRequest() {
        start();
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
}

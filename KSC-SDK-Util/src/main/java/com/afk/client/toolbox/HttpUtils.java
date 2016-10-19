package com.afk.client.toolbox;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Alamusi on 2016/7/27.
 */
public class HttpUtils {

    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * Opens an {@link HttpURLConnection} with parameters
     *
     * @param parseUrl     target url
     * @param requestParam target {@link HttpRequestParam}
     * @return an opening {@link HttpURLConnection
     * @throws IOException
     */
    public static synchronized HttpURLConnection openConnection(URL parseUrl, HttpRequestParam requestParam) throws IOException {
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
    public static synchronized void setConnectionParametersForRequest(HttpURLConnection connection, HttpRequestParam requestParam) throws IOException {
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
    private static void addBodyIfExists(HttpURLConnection connection, HttpRequestParam requestParam) throws IOException {
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

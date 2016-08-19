package com.sensorsdata.analytics.android.sdk.java_websocket;

import com.sensorsdata.analytics.android.sdk.java_websocket.drafts.Draft;

import java.net.Socket;
import java.util.List;

public interface WebSocketFactory {
    WebSocket createWebSocket(WebSocketAdapter a, Draft d, Socket s);

    WebSocket createWebSocket(WebSocketAdapter a, List<Draft> drafts, Socket s);

}

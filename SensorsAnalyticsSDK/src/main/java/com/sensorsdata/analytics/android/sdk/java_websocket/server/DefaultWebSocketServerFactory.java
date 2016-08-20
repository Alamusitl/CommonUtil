package com.sensorsdata.analytics.android.sdk.java_websocket.server;

import com.sensorsdata.analytics.android.sdk.java_websocket.WebSocketAdapter;
import com.sensorsdata.analytics.android.sdk.java_websocket.WebSocketImpl;
import com.sensorsdata.analytics.android.sdk.java_websocket.drafts.Draft;
import com.sensorsdata.analytics.android.sdk.java_websocket.server.WebSocketServer.WebSocketServerFactory;

import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

public class DefaultWebSocketServerFactory implements WebSocketServerFactory {
    @Override
    public WebSocketImpl createWebSocket(WebSocketAdapter a, Draft d, Socket s) {
        return new WebSocketImpl(a, d);
    }

    @Override
    public WebSocketImpl createWebSocket(WebSocketAdapter a, List<Draft> d, Socket s) {
        return new WebSocketImpl(a, d);
    }

    @Override
    public SocketChannel wrapChannel(SocketChannel channel, SelectionKey key) {
        return channel;
    }
}
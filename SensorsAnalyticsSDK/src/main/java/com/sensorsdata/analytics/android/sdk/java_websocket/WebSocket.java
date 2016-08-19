package com.sensorsdata.analytics.android.sdk.java_websocket;

import com.sensorsdata.analytics.android.sdk.java_websocket.drafts.Draft;
import com.sensorsdata.analytics.android.sdk.java_websocket.framing.Framedata;
import com.sensorsdata.analytics.android.sdk.java_websocket.framing.Framedata.Opcode;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;

public interface WebSocket {
    int DEFAULT_PORT = 80;
    int DEFAULT_WSS_PORT = 443;

    void close(int code, String message);

    void close(int code);

    void close();

    void closeConnection(int code, String message);

    void send(String text) throws NotYetConnectedException;

    void send(ByteBuffer bytes) throws IllegalArgumentException, NotYetConnectedException;

    void send(byte[] bytes) throws IllegalArgumentException, NotYetConnectedException;

    void sendFrame(Framedata framedata);

    void sendFragmentedFrame(Opcode op, ByteBuffer buffer, boolean fin);

    boolean hasBufferedData();

    InetSocketAddress getRemoteSocketAddress();

    InetSocketAddress getLocalSocketAddress();

    boolean isConnecting();

    boolean isOpen();

    boolean isClosing();

    boolean isFlushAndClose();

    boolean isClosed();

    Draft getDraft();

    READYSTATE getReadyState();

    String getResourceDescriptor();

    enum Role {
        CLIENT, SERVER
    }

    enum READYSTATE {
        NOT_YET_CONNECTED, CONNECTING, OPEN, CLOSING, CLOSED
    }
}
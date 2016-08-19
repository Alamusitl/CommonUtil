package com.sensorsdata.analytics.android.sdk.java_websocket;

import com.sensorsdata.analytics.android.sdk.java_websocket.drafts.Draft;
import com.sensorsdata.analytics.android.sdk.java_websocket.exceptions.InvalidDataException;
import com.sensorsdata.analytics.android.sdk.java_websocket.framing.Framedata;
import com.sensorsdata.analytics.android.sdk.java_websocket.handshake.ClientHandshake;
import com.sensorsdata.analytics.android.sdk.java_websocket.handshake.Handshakedata;
import com.sensorsdata.analytics.android.sdk.java_websocket.handshake.ServerHandshake;
import com.sensorsdata.analytics.android.sdk.java_websocket.handshake.ServerHandshakeBuilder;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Implemented by <tt>WebSocketClient</tt> and <tt>WebSocketServer</tt>.
 * The methods within are called by <tt>WebSocket</tt>.
 * Almost every method takes a first parameter conn which represents the source of the respective event.
 */
public interface WebSocketListener {


    ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException;

    void onWebsocketHandshakeReceivedAsClient(WebSocket conn, ClientHandshake request, ServerHandshake response) throws InvalidDataException;

    void onWebsocketHandshakeSentAsClient(WebSocket conn, ClientHandshake request) throws InvalidDataException;

    void onWebsocketMessage(WebSocket conn, String message);

    void onWebsocketMessage(WebSocket conn, ByteBuffer blob);

    void onWebsocketMessageFragment(WebSocket conn, Framedata frame);

    void onWebsocketOpen(WebSocket conn, Handshakedata d);

    void onWebsocketClose(WebSocket ws, int code, String reason, boolean remote);

    void onWebsocketClosing(WebSocket ws, int code, String reason, boolean remote);

    void onWebsocketCloseInitiated(WebSocket ws, int code, String reason);

    void onWebsocketError(WebSocket conn, Exception ex);

    void onWebsocketPing(WebSocket conn, Framedata f);

    void onWebsocketPong(WebSocket conn, Framedata f);

    String getFlashPolicy(WebSocket conn) throws InvalidDataException;

    void onWriteDemand(WebSocket conn);

    InetSocketAddress getLocalSocketAddress(WebSocket conn);

    InetSocketAddress getRemoteSocketAddress(WebSocket conn);
}

package com.sensorsdata.analytics.android.sdk.java_websocket.framing;

import com.sensorsdata.analytics.android.sdk.java_websocket.exceptions.InvalidFrameException;

import java.nio.ByteBuffer;

public interface Framedata {
    boolean isFin();

    boolean getTransfereMasked();

    Opcode getOpcode();

    ByteBuffer getPayloadData();// TODO the separation of the application data and the extension data is yet to be done

    void append(Framedata nextframe) throws InvalidFrameException;

    enum Opcode {
        CONTINUOUS, TEXT, BINARY, PING, PONG, CLOSING
        // more to come
    }
}

package com.sensorsdata.analytics.android.sdk.java_websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

import javax.net.ssl.SSLException;

public interface WrappedByteChannel extends ByteChannel {
    boolean isNeedWrite();

    void writeMore() throws IOException;

    boolean isNeedRead();

    int readMore(ByteBuffer dst) throws SSLException;

    boolean isBlocking();
}

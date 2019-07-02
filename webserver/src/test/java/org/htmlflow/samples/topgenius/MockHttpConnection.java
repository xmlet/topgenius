package org.htmlflow.samples.topgenius;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.GoAway;
import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.net.SocketAddress;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;

public class MockHttpConnection implements HttpConnection {
    @Override
    public HttpConnection goAway(long errorCode, int lastStreamId, Buffer debugData) {
        return null;
    }

    @Override
    public HttpConnection goAwayHandler(Handler<GoAway> handler) {
        return null;
    }

    @Override
    public HttpConnection shutdownHandler(Handler<Void> handler) {
        return null;
    }

    @Override
    public HttpConnection shutdown() {
        return null;
    }

    @Override
    public HttpConnection shutdown(long timeoutMs) {
        return null;
    }

    @Override
    public HttpConnection closeHandler(Handler<Void> handler) {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public Http2Settings settings() {
        return null;
    }

    @Override
    public HttpConnection updateSettings(Http2Settings settings) {
        return null;
    }

    @Override
    public HttpConnection updateSettings(Http2Settings settings, Handler<AsyncResult<Void>> completionHandler) {
        return null;
    }

    @Override
    public Http2Settings remoteSettings() {
        return null;
    }

    @Override
    public HttpConnection remoteSettingsHandler(Handler<Http2Settings> handler) {
        return null;
    }

    @Override
    public HttpConnection ping(Buffer data, Handler<AsyncResult<Buffer>> pongHandler) {
        return null;
    }

    @Override
    public HttpConnection pingHandler(Handler<Buffer> handler) {
        return null;
    }

    @Override
    public HttpConnection exceptionHandler(Handler<Throwable> handler) {
        return null;
    }

    @Override
    public SocketAddress remoteAddress() {
        return null;
    }

    @Override
    public SocketAddress localAddress() {
        return null;
    }

    @Override
    public boolean isSsl() {
        return false;
    }

    @Override
    public SSLSession sslSession() {
        return null;
    }

    @Override
    public X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException {
        return new X509Certificate[0];
    }

    @Override
    public String indicatedServerName() {
        return null;
    }
}

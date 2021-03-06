package org.htmlflow.samples.topgenius;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpFrame;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerFileUpload;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.StreamPriority;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class MockHttpServerRequest implements HttpServerRequest {
    Map<String, String> params = new HashMap<>();
    Map<String, String> headers = new HashMap<>();

    public MockHttpServerRequest add(String key, String val) {
        params.put(key, val);
        return this;
    }

    @Override
    public HttpServerRequest exceptionHandler(Handler<Throwable> handler) {
        return null;
    }

    @Override
    public HttpServerRequest handler(Handler<Buffer> handler) {
        return null;
    }

    @Override
    public HttpServerRequest pause() {
        return null;
    }

    @Override
    public HttpServerRequest resume() {
        return null;
    }

    @Override
    public HttpServerRequest fetch(long amount) {
        return null;
    }

    @Override
    public HttpServerRequest endHandler(Handler<Void> endHandler) {
        return null;
    }

    @Override
    public HttpVersion version() {
        return null;
    }

    @Override
    public HttpMethod method() {
        return null;
    }

    @Override
    public String rawMethod() {
        return null;
    }

    @Override
    public boolean isSSL() {
        return false;
    }

    @Override
    public String scheme() {
        return null;
    }

    @Override
    public String uri() {
        return null;
    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public String query() {
        return null;
    }

    @Override
    public String host() {
        return null;
    }

    @Override
    public long bytesRead() {
        return 0;
    }

    @Override
    public HttpServerResponse response() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MultiMap headers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHeader(String headerName) {
        return headers.get(headerName);
    }

    @Override
    public String getHeader(CharSequence headerName) {
        return headers.get(headerName.toString());
    }

    @Override
    public MultiMap params() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getParam(String paramName) {
        return params.get(paramName);
    }

    @Override
    public SocketAddress remoteAddress() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SocketAddress localAddress() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SSLSession sslSession() {
        throw new UnsupportedOperationException();
    }

    @Override
    public X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String absoluteURI() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NetSocket netSocket() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerRequest setExpectMultipart(boolean expect) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isExpectMultipart() {
        return false;
    }

    @Override
    public HttpServerRequest uploadHandler(Handler<HttpServerFileUpload> uploadHandler) {
        return null;
    }

    @Override
    public MultiMap formAttributes() {
        return null;
    }

    @Override
    public String getFormAttribute(String attributeName) {
        return null;
    }

    @Override
    public ServerWebSocket upgrade() {
        return null;
    }

    @Override
    public boolean isEnded() {
        return false;
    }

    @Override
    public HttpServerRequest customFrameHandler(Handler<HttpFrame> handler) {
        return null;
    }

    @Override
    public HttpConnection connection() {
        return new MockHttpConnection();
    }

    @Override
    public HttpServerRequest streamPriorityHandler(Handler<StreamPriority> handler) {
        return null;
    }
}

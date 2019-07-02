package org.htmlflow.samples.topgenius;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;

import java.util.concurrent.CompletableFuture;

public class MockHttpServerResponse implements HttpServerResponse {
    private final CompletableFuture<String> complete;
    private final StringBuffer buf = new StringBuffer();

    public MockHttpServerResponse(CompletableFuture<String> complete) {
        this.complete = complete;
    }

    @Override
    public HttpServerResponse exceptionHandler(Handler<Throwable> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse write(Buffer data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse write(Buffer data, Handler<AsyncResult<Void>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse setWriteQueueMaxSize(int maxSize) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean writeQueueFull() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse drainHandler(Handler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatusCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse setStatusCode(int statusCode) {
        return this;
    }

    @Override
    public String getStatusMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse setStatusMessage(String statusMessage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse setChunked(boolean chunked) {
        return this;
    }

    @Override
    public boolean isChunked() {
        return false;
    }

    @Override
    public MultiMap headers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse putHeader(String name, String value) {
        return this;
    }

    @Override
    public HttpServerResponse putHeader(CharSequence name, CharSequence value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse putHeader(String name, Iterable<String> values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse putHeader(CharSequence name, Iterable<CharSequence> values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MultiMap trailers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse putTrailer(String name, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse putTrailer(CharSequence name, CharSequence value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse putTrailer(String name, Iterable<String> values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse putTrailer(CharSequence name, Iterable<CharSequence> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse closeHandler(Handler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse endHandler(Handler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse write(String chunk, String enc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse write(String chunk, String enc, Handler<AsyncResult<Void>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse write(String chunk) {
        buf.append(chunk);
        return this;
    }

    @Override
    public HttpServerResponse write(String chunk, Handler<AsyncResult<Void>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse writeContinue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void end(String chunk) {
        buf.append(chunk);
        complete.complete(buf.toString());
    }

    @Override
    public void end(String chunk, Handler<AsyncResult<Void>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void end(String chunk, String enc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void end(String chunk, String enc, Handler<AsyncResult<Void>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void end(Buffer chunk) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void end(Buffer chunk, Handler<AsyncResult<Void>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void end() {
        complete.complete(buf.toString());
    }

    @Override
    public void end(Handler<AsyncResult<Void>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse sendFile(String filename, long offset, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse sendFile(String filename, long offset, long length, Handler<AsyncResult<Void>> resultHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean ended() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean closed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean headWritten() {
                throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse headersEndHandler(Handler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse bodyEndHandler(Handler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long bytesWritten() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int streamId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse push(HttpMethod method, String host, String path, Handler<AsyncResult<HttpServerResponse>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse push(HttpMethod method, String path, MultiMap headers, Handler<AsyncResult<HttpServerResponse>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse push(HttpMethod method, String path, Handler<AsyncResult<HttpServerResponse>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse push(HttpMethod method, String host, String path, MultiMap headers, Handler<AsyncResult<HttpServerResponse>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset(long code) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse writeCustomFrame(int type, int flags, Buffer payload) {
        throw new UnsupportedOperationException();
    }
}

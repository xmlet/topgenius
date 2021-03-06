package org.htmlflow.samples.topgenius.controllers;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.htmlflow.samples.topgenius.LastfmWebApiSessions;
import org.htmlflow.samples.topgenius.model.Track;
import org.htmlflow.samples.topgenius.views.ViewsHtmlFlow;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static org.htmlflow.samples.topgenius.views.ViewsHtmlFlow.context;

public class ControllerHtmlFlow {

    private final LastfmWebApiSessions lastfm;
    private final Vertx worker = Vertx
        .vertx(new VertxOptions()
            .setWorkerPoolSize(40)
            .setMaxWorkerExecuteTime(Long.MAX_VALUE));

    public ControllerHtmlFlow(LastfmWebApiSessions lastfm) {
        this.lastfm = lastfm;
    }

    public void toptracksHandler(RoutingContext ctx) {
        long begin = currentTimeMillis();
        HttpServerRequest req = ctx.request();
        HttpServerResponse resp = ctx.response();
        resp.putHeader("content-type", "text/html");
        /**
         * Parse query-string parameters
         */
        String ctr = req.getParam("country");
        String str = req.getParam("limit");
        int limit = str != null ? parseInt(str) : 10000;
        String country = ctr != null ? ctr : "";
        boolean hasSession = lastfm.hasSession(ctx);
        worker.<HttpResponsePrinter>executeBlocking(future -> {
            Stream<Track> tracks = country == null || country.isBlank()
                ? Stream.empty()
                : lastfm
                    .from(ctx)
                    .countryTopTracks(country, limit, hasSession);
            resp.setChunked(true);
            HttpResponsePrinter out = new HttpResponsePrinter(resp, req.connection(), future);
            ViewsHtmlFlow
                .toptracks
                .setPrintStream(out)
                .write(context(country, limit, hasSession, tracks, begin));
            if(!future.isComplete())
                future.complete(out);
        }, asyncRes -> {
            if(asyncRes.failed())
                resp.end(stackTrace(asyncRes.cause()));
            else
                asyncRes.result().close(); // flush + close
        });
    }

    static class HttpResponsePrinter extends PrintStream {
        final HttpServerResponse resp;
        final Future<HttpResponsePrinter> future;
        Buffer buffer;
        static final int MAX_SIZE = 1024*16;
        int index;


        public HttpResponsePrinter(
            HttpServerResponse resp,
            HttpConnection connection,
            Future<HttpResponsePrinter> future)
        {
            super(new NullOutputStream());
            this.resp = resp;
            this.future = future;
            this.buffer = Buffer.buffer(MAX_SIZE);

            connection.exceptionHandler(thr -> {
                if(!future.isComplete())
                    future.fail("Connection reset by peer!");
            });
        }

        @Override
        public void write(byte[] buf, int off, int len) {
            buffer.appendBytes(buf, off, len);
            index += len;
            if(index >= MAX_SIZE) flushBuffer();
        }

        @Override
        public void close() {
            flushBuffer();
            resp.end();
            resp.close();
        }

        void flushBuffer() {
            try{
                resp.write(buffer);
                buffer = Buffer.buffer(MAX_SIZE);
                index = 0;
            } catch (Exception e){
                resp.close();
                future.fail(e);
            }
        }
    }

    static class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            throw new UnsupportedOperationException();
        }
    }

    private static String stackTrace(Throwable err) {
        StringWriter sw = new StringWriter();
        err.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}

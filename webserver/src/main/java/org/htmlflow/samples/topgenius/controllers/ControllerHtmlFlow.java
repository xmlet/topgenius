package org.htmlflow.samples.topgenius.controllers;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.htmlflow.samples.topgenius.LastfmWebApi;
import org.htmlflow.samples.topgenius.model.Track;
import org.htmlflow.samples.topgenius.views.ViewsHtmlFlow;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static org.htmlflow.samples.topgenius.views.ViewsHtmlFlow.context;

public class ControllerHtmlFlow {

    private final LastfmWebApi lastfm;
    private Vertx worker = Vertx.vertx(new VertxOptions().setWorkerPoolSize(40));

    public ControllerHtmlFlow(LastfmWebApi lastfm) {
        this.lastfm = lastfm;
    }

    public void toptracksHandler(RoutingContext ctx) {
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
        worker.<HttpResponsePrinter>executeBlocking(future -> {
            Stream<Track> tracks = lastfm
                .geographicTopTracks(country)
                .limit(limit);
            resp.setChunked(true);
            HttpResponsePrinter out = new HttpResponsePrinter(resp, req.connection(), future);
            ViewsHtmlFlow
                .toptracks
                .setPrintStream(out)
                .write(context(country, limit, tracks));
            if(!future.isComplete())
                future.complete(out);
        }, asyncRes -> {
            if(asyncRes.failed())
                resp.end(asyncRes.cause().toString());
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
}

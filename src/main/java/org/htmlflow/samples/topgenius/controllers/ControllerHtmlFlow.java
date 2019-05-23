package org.htmlflow.samples.topgenius.controllers;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.htmlflow.samples.topgenius.LastfmWebApi;
import org.htmlflow.samples.topgenius.model.Track;
import org.htmlflow.samples.topgenius.views.ViewsHtmlFlow;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;
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
        worker.executeBlocking(future -> {
            Stream<Track> tracks = lastfm
                .geographicTopTracks(country)
                .limit(limit);
            resp.setChunked(true);
            ViewsHtmlFlow
                .toptracks
                .setPrintStream(new HttpResponsePrinter(resp, req.connection(), future))
                .write(context(country, limit, tracks));
            resp.end();
            future.complete();
        }, asyncRes -> {
            // !!! TO DO: check for errors!
        });
    }

    static class HttpResponsePrinter extends PrintStream {

        public HttpResponsePrinter(HttpServerResponse resp, HttpConnection connection, Future<Object> future) {
            super(responseOutputStream(resp, connection, future));
        }

        private static OutputStream responseOutputStream(HttpServerResponse resp, HttpConnection connection, Future<Object> future) {
            AtomicBoolean connectionFailed = new AtomicBoolean(false);
            connection.exceptionHandler(thr -> {
                if(!future.isComplete())
                    future.fail("Connection reset by peer!");
                connectionFailed.set(true);
            });
            return new OutputStream() {
                @Override
                public void write(int b) {
                    if(connectionFailed.get())
                        return;
                    char c = (char) b;
                    try{
                        resp.write(String.valueOf(c));
                    } catch (Exception e){
                        resp.close();
                    }
                }
            };
        }
    }
}

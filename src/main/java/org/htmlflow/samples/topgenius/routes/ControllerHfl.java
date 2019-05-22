package org.htmlflow.samples.topgenius.routes;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.htmlflow.samples.topgenius.LastfmWebApi;
import org.htmlflow.samples.topgenius.model.Track;
import org.htmlflow.samples.topgenius.views.JingleRxViews;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;

public class ControllerHfl {

    private final LastfmWebApi lastfm;
    private Vertx io = Vertx.vertx(new VertxOptions().setWorkerPoolSize(40));

    public ControllerHfl(LastfmWebApi lastfm) {
        this.lastfm = lastfm;
    }

    public void tracksHandler(RoutingContext ctx) {
        HttpServerRequest req = ctx.request();
        // !!!! TO DO: set an End handler !!!!!
        HttpServerResponse resp = ctx.response();
        resp.putHeader("content-type", "text/html");

        String mbid = req.getParam("mbid");
        String str = req.getParam("limit");
        int limit = str != null ? parseInt(str) : 50;


        io.executeBlocking(future -> {
            Stream<Track> tracks = lastfm
                .getTracks(mbid)
                .limit(limit);
            resp.setChunked(true);
            JingleRxViews
                .artists
                .setPrintStream(new HttpResponsePrinter(resp, req.connection(), future))
                .write(tracks);
            resp.end();
            future.complete();
        }, res -> {
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

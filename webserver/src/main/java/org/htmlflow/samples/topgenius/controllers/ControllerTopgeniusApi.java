package org.htmlflow.samples.topgenius.controllers;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.htmlflow.samples.topgenius.LastfmWebApiSessions;

import java.io.PrintWriter;
import java.io.StringWriter;

import static java.lang.Integer.parseInt;

public class ControllerTopgeniusApi {
    private final LastfmWebApiSessions lastfm;
    private final Router router;

    private final Vertx worker = Vertx
        .vertx(new VertxOptions()
            .setWorkerPoolSize(40)
            .setMaxWorkerExecuteTime(Long.MAX_VALUE));

    public ControllerTopgeniusApi(LastfmWebApiSessions lastfm, Vertx vertx) {
        this.router = Router.router(vertx);
        this.lastfm = lastfm;
        router.route(HttpMethod.POST, "/accept").handler(this::acceptHandler);
        router.route(HttpMethod.GET, "/toptracks").handler(this::topTracksHandler);
        router.route().handler(BodyHandler.create());
        router.route(HttpMethod.POST, "/clearcache").handler(this::clearcacheHandler);
    }

    public Router router() {
        return router;
    }

    public void acceptHandler(RoutingContext ctx) {
        HttpServerResponse resp = ctx.response();
        try{
            if(!lastfm.hasSession(ctx))
                lastfm.newSession(ctx);
        } catch(Exception err) {
            resp.setStatusCode(500).end(stackTrace(err));
        } finally {
            resp.end();
        }
    }

    public void clearcacheHandler(RoutingContext ctx) {
        HttpServerRequest req = ctx.request();
        HttpServerResponse resp = ctx.response();
        resp.putHeader("Access-Control-Allow-Origin", "http://localhost:3001");
        /**
         * Parse query-string parameters
         */
        String country = req.getParam("country");
        try{
            if(country != null && !country.isBlank()) {
                lastfm.from(ctx).clearCacheAndCancelRequests(country);
            }
        } catch(Exception err) {
            resp.setStatusCode(500).end(stackTrace(err));
        } finally {
            resp.end();
        }
    }
    public void topTracksHandler(RoutingContext ctx) {
        HttpServerRequest req = ctx.request();
        HttpServerResponse resp = ctx.response();
        resp.putHeader("content-type", "application/stream+json");
        resp.putHeader("Access-Control-Allow-Origin", "http://localhost:3001");
        resp.setChunked(true);
        /**
         * Parse query-string parameters
         */
        String ctr = req.getParam("country");
        String str = req.getParam("limit");
        int limit = str != null ? parseInt(str) : 50;
        worker.executeBlocking(future -> {
            lastfm
                .from(ctx)
                .countryTopTracksInJsonPages(ctr, limit, lastfm.hasSession(ctx))
                .forEach(json -> { resp.write(json); resp.write("\n"); });
            future.complete();
        }, ares -> {
            if(ares.failed())
                resp.setStatusCode(500).end(stackTrace(ares.cause()));
            else
                resp.end(); // flush + close
        });
    }

    private static String stackTrace(Throwable err) {
        StringWriter sw = new StringWriter();
        err.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}

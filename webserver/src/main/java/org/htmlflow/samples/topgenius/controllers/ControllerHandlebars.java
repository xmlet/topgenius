package org.htmlflow.samples.topgenius.controllers;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine;
import org.htmlflow.samples.topgenius.LastfmWebApi;
import org.htmlflow.samples.topgenius.LastfmWebApiSessions;
import org.htmlflow.samples.topgenius.model.Track;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class ControllerHandlebars {

    private final LastfmWebApiSessions lastfm;
    private final HandlebarsTemplateEngine engine;
    private final Vertx worker = Vertx
        .vertx(new VertxOptions()
            .setWorkerPoolSize(40)
            .setMaxWorkerExecuteTime(Long.MAX_VALUE));

    public ControllerHandlebars(LastfmWebApiSessions lastfm, Vertx vertx) {
        this.lastfm = lastfm;
        this.engine = HandlebarsTemplateEngine.create(vertx);
    }

    public void toptracksHandler(RoutingContext ctx) {
        long begin = currentTimeMillis();
        HttpServerRequest req = ctx.request();
        HttpServerResponse resp = ctx.response();
        resp.putHeader("content-type", "text/html");
        /**
         * Parse query-string parameters
         */
        String country = req.getParam("country");
        String str = req.getParam("limit");
        int limit = str != null ? parseInt(str) : 10000;
        boolean hasSession = lastfm.hasSession(ctx);
        worker.<Buffer>executeBlocking(future -> {
            List<Track> tracks = country == null || country.isBlank()
                ? emptyList()
                : lastfm
                    .from(ctx)
                    .countryTopTracks(country, limit, hasSession)
                    .collect(toList());
            engine.render(toptracksContext(tracks), "templates/toptracks.hbs", view -> {
                if(view.succeeded())
                    future.complete(view.result());
                else
                    future.fail(view.cause());
            });
        }, ares -> {
            if(ares.succeeded())
                sendTopTracks(resp, country, hasSession, limit, ares.result(), begin);
            else
                resp.setStatusCode(500).end(stackTrace(ares.cause()));
        });
    }

    private void sendTopTracks(
        HttpServerResponse resp,
        String country,
        boolean hasSession,
        int limit,
        Buffer toptracks,
        long begin)
    {
        Map<String, Object> data = mainContext(country, limit, hasSession, toptracks.toString(), begin);
        engine.render(data, "templates/main.hbs", view -> {
                if(view.succeeded())
                    resp.end(view.result());
                else
                    resp.setStatusCode(500).end(stackTrace(view.cause()));
            });
    }

    static Map<String, Object> mainContext(
        String country,
        int limit,
        boolean hasSession,
        String toptracks,
        long begin)
    {
        Map<String, Object> data = new HashMap<>();
        double serverDuration = (currentTimeMillis() - begin) / 1000.0;
        data.put("country", country);
        data.put("limit", limit);
        data.put("hasSession", hasSession);
        data.put("toptracks", toptracks);
        data.put("serverDuration", serverDuration);
        return data;
    }

    static Map<String, Object> toptracksContext(List<Track> tracks) {
        Map<String, Object> data = new HashMap<>();
        data.put("tracks", tracks);
        return data;
    }

    private static String stackTrace(Throwable err) {
        StringWriter sw = new StringWriter();
        err.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}

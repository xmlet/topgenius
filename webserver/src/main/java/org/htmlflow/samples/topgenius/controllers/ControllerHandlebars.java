package org.htmlflow.samples.topgenius.controllers;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine;
import org.htmlflow.samples.topgenius.LastfmWebApi;
import org.htmlflow.samples.topgenius.model.Track;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class ControllerHandlebars {

    private final LastfmWebApi lastfm;
    private final HandlebarsTemplateEngine engine;
    private final Vertx worker = Vertx
        .vertx(new VertxOptions()
            .setWorkerPoolSize(40)
            .setMaxWorkerExecuteTime(Long.MAX_VALUE));

    public ControllerHandlebars(LastfmWebApi lastfm, Vertx vertx) {
        this.lastfm = lastfm;
        this.engine = HandlebarsTemplateEngine.create(vertx);
    }

    public void toptracksHandler(RoutingContext ctx) {
        HttpServerRequest req = ctx.request();
        HttpServerResponse resp = ctx.response();
        resp.putHeader("content-type", "text/html");
        /**
         * Parse query-string parameters
         */
        String country = req.getParam("country");
        String str = req.getParam("limit");
        int limit = str != null ? parseInt(str) : 10000;
        worker.<Buffer>executeBlocking(future -> {
            List<Track> tracks = country == null
                ? emptyList()
                : lastfm
                    .countryTopTracks(country)
                    .limit(limit)
                    .collect(toList());
            Map<String, Object> data = context(country, limit, tracks);
            engine.render(data, "templates/toptracks.hbs", view -> {
                if(view.succeeded())
                    future.complete(view.result());
                else
                    future.fail(view.cause());
            });
        }, ares -> {
            if(ares.succeeded())
                resp.end(ares.result());
            else
                resp.setStatusCode(500).end(stackTrace(ares.cause()));
        });
    }

    private static String stackTrace(Throwable err) {
        StringWriter sw = new StringWriter();
        err.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    static Map<String, Object> context(String country, int limit, List<Track> tracks) {
        Map<String, Object> data = new HashMap<>();
        data.put("country", country);
        data.put("limit", limit);
        data.put("tracks", tracks);
        return data;
    }

    private void render(String path, Map<String, Object>  ctx, HttpServerResponse resp) {
        engine.render(ctx, "templates" + path, view -> {
            if(view.succeeded())
                resp.end(view.result());
            else
                resp.setStatusCode(500).end(view.cause().toString());
        });
    }
}

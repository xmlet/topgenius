package org.htmlflow.samples.topgenius.controllers;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine;
import org.htmlflow.samples.topgenius.LastfmWebApi;
import org.htmlflow.samples.topgenius.model.Track;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;

public class ControllerHandlebars {

    private final LastfmWebApi lastfm;
    private final HandlebarsTemplateEngine engine;
    private Vertx worker = Vertx.vertx(new VertxOptions().setWorkerPoolSize(40));

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
        worker.executeBlocking(future -> {
            List<Track> tracks = lastfm
                .geographicTopTracks(country)
                .limit(limit)
                .collect(toList());
            Map<String, Object> data = context(country, limit, tracks);
            render("/toptracks.hbs", data, resp);
        }, ayncRes -> {
            // !!! TO DO: check for errors!
        });
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

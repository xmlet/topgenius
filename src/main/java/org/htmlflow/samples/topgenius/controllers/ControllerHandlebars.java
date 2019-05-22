package org.htmlflow.samples.topgenius.controllers;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.HandlebarsTemplateEngine;
import io.vertx.ext.web.templ.TemplateEngine;
import org.htmlflow.samples.topgenius.LastfmWebApi;
import org.htmlflow.samples.topgenius.model.Track;

import java.util.List;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class ControllerHandlebars {

    private final LastfmWebApi lastfm;
    private final TemplateEngine engine = HandlebarsTemplateEngine.create();
    private Vertx worker = Vertx.vertx(new VertxOptions().setWorkerPoolSize(40));

    public ControllerHandlebars(LastfmWebApi lastfm) {
        this.lastfm = lastfm;
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
        int limit = str != null ? parseInt(str) : 50;
        worker.executeBlocking(future -> {
            List<Track> tracks = lastfm
                .geographicTopTracks(country)
                .limit(limit)
                .collect(toList());
            context(ctx, country, limit, tracks);
            render("/toptracks.hbs", ctx, resp);
        }, ayncRes -> {
            // !!! TO DO: check for errors!
        });
    }

    static RoutingContext context(RoutingContext ctx, String country, int limit, List<Track> tracks) {
        ctx.put("country", country);
        ctx.put("limit", limit);
        ctx.put("tracks", tracks);
        return ctx;
    }

    private void render(String path, RoutingContext ctx, HttpServerResponse resp) {
        engine.render(ctx, "templates", path, view -> {
            if(view.succeeded())
                resp.end(view.result());
            else
                ctx.fail(view.cause());
        });
    }
}

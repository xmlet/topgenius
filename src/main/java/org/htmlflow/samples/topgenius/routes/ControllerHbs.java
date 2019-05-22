package org.htmlflow.samples.topgenius.routes;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.HandlebarsTemplateEngine;
import io.vertx.ext.web.templ.TemplateEngine;
import org.htmlflow.samples.topgenius.LastfmWebApi;
import org.htmlflow.samples.topgenius.model.Track;

import java.util.List;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;

public class ControllerHbs {

    private final LastfmWebApi lastfm;
    private final TemplateEngine engine = HandlebarsTemplateEngine.create();

    public ControllerHbs(LastfmWebApi lastfm) {
        this.lastfm = lastfm;
    }

    public void searchHandler(RoutingContext ctx) {
        HttpServerResponse resp = ctx.response();
        resp.putHeader("content-type", "text/html");
        resp.sendFile("templates/search.html");
    }

    public void tracksHandler(RoutingContext ctx) {
        HttpServerRequest req = ctx.request();
        HttpServerResponse resp = ctx.response();
        resp.putHeader("content-type", "text/html");

        String mbid = req.getParam("mbid");
        String str = req.getParam("limit");
        int limit = str != null ? parseInt(str) : 50;
        List<Track> tracks = lastfm
            .getTracks(mbid)
            .limit(limit)
            .collect(toList());
        ctx.put("tracks", tracks);

        engine.render(ctx, "templates", "/tracks.hbs", view -> {
            if(view.succeeded())
                resp.end(view.result());
            else
                ctx.fail(view.cause());
        });
    }


}

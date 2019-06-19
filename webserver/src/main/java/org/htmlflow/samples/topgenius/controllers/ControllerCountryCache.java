package org.htmlflow.samples.topgenius.controllers;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.htmlflow.samples.topgenius.LastfmWebApi;

public class ControllerCountryCache {
    final Router router;
    final LastfmWebApi lastfm;

    public ControllerCountryCache(LastfmWebApi lastfm, Vertx vertx) {
        this.router = Router.router(vertx);
        this.lastfm = lastfm;
        router.route().handler(BodyHandler.create());
        router.route(HttpMethod.POST, "/:from").handler(this::clearcacheHandler);
    }


    public Router router() {
        return router;
    }

    public void clearcacheHandler(RoutingContext ctx) {
        HttpServerRequest req = ctx.request();
        HttpServerResponse resp = ctx.response();
        /**
         * Parse query-string parameters
         */
        String country = req.getParam("country");
        String from = req.getParam("from");
        if(country != null) lastfm.clearCacheAndCancelRequests(country);
        resp.putHeader("location", "/" + from).setStatusCode(303).end();
    }

}

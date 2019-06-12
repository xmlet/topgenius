package org.htmlflow.samples.topgenius.controllers;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.htmlflow.samples.topgenius.LastfmWebApiMock;
import org.htmlflow.samples.topgenius.model.Track;

import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;
import static org.htmlflow.samples.topgenius.LastfmWebApiMock.jsonGeographicTopTracks;

public class ControllerLastfmMock {

    private static final List<Track> australiaTopTracks = new LastfmWebApiMock()
            .geographicTopTracks("australia")
            .collect(toList());

    public static void geographicTopTracks(RoutingContext ctx) {

        HttpServerRequest req = ctx.request();
        HttpServerResponse resp = ctx.response();
        resp.putHeader("content-type", "application/json");
        /**
         * Parse query-string parameters
         */
        String ctr = req.getParam("country");
        String str = req.getParam("page");
        int page = str != null ? parseInt(str) : 1;
        str = req.getParam("limit");
        int limit = str != null ? parseInt(str) : australiaTopTracks.size();
        if(limit > australiaTopTracks.size()) limit = australiaTopTracks.size();
        /**
         * Send response
         */
        if(page > 0) // send a single page
            resp.end(jsonGeographicTopTracks(ctr, page));
        else // Send all pages to reach limit
            jsonAustraliaTopTracks(resp, ctr, limit);
    }

    public static void jsonAustraliaTopTracks(HttpServerResponse resp, String country, int limit) {
        resp.setChunked(true);
        IntStream
            .rangeClosed(1, limit/50)
            .mapToObj(page -> jsonGeographicTopTracks(country, page))
            .forEach(json -> resp.write(json + '\n'));
        resp.end();
    }
}

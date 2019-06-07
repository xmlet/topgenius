package org.htmlflow.samples.topgenius.controllers;

import com.google.gson.Gson;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.htmlflow.samples.topgenius.LastfmWebApiMock;
import org.htmlflow.samples.topgenius.model.MockGeographicTopTracks;
import org.htmlflow.samples.topgenius.model.Track;

import java.util.List;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;

public class ControllerLastfmMock {

    private static final List<Track> australiaTopTracks = new LastfmWebApiMock()
            .geographicTopTracks("australia")
            .collect(toList());

    private static final Gson gson = new Gson();

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
        if(page > 0)
            resp.end(LastfmWebApiMock.jsonGeographicTopTracks(ctr, page));
        else
            resp.end(jsonAustraliaTopTracks(limit));
    }

    public static String jsonAustraliaTopTracks(int limit) {
        List<Track> tracks = australiaTopTracks.subList(0, limit);
        return gson.toJson(new MockGeographicTopTracks(tracks));
    }
}

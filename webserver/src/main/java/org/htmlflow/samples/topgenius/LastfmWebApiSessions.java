package org.htmlflow.samples.topgenius;

import io.vertx.ext.web.RoutingContext;

public interface LastfmWebApiSessions {

    /**
     * Generates a new UUID session and update RoutingContext
     * with corresponding TopGenius cookie.
     */
    void newSession(RoutingContext ctx);

    /**
     * Look for TopGenius cookie session in RoutingContext
     * ans returns corresponding LastfmWebApi form sessions map.
     * Otherwise creates new freshly instance of LastfmWebApi.
     */
    LastfmWebApi from(RoutingContext ctx);

    /**
     * Look for TopGenius cookie and corresponding UUID
     * in sessions map.
     * If none exists returns false.
     */
    boolean hasSession(RoutingContext ctx);
}

package org.htmlflow.samples.topgenius;

import io.vertx.ext.web.RoutingContext;

public interface LastfmWebApiSessions {

    public LastfmWebApi from(RoutingContext ctx);

    boolean hasSession(RoutingContext ctx);
}

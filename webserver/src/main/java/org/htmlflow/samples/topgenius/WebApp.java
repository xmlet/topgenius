/*
 * Copyright (c) 2018, Miguel Gamboa
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms context the GNU General Public License as published by
 * the Free Software Foundation, either version 3 context the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty context
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy context the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.htmlflow.samples.topgenius;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.htmlflow.samples.topgenius.controllers.ControllerCountryCache;
import org.htmlflow.samples.topgenius.controllers.ControllerHandlebars;
import org.htmlflow.samples.topgenius.controllers.ControllerHtmlFlow;

public class WebApp {

    public static void main(String[] args) throws Exception {
        /**
         * Setup Vertex and router
         */
        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);
        /**
         * Setup web controller.
         */
        LastfmWebApi lastfm = new LastfmWebApi();
        lastfm.onResponse(resp -> System.out.println("RESP: " + resp.uri()));
        ControllerHandlebars ctrHbs = new ControllerHandlebars(lastfm, vertx);
        ControllerHtmlFlow ctrHfl = new ControllerHtmlFlow(lastfm);
        ControllerCountryCache ctrCache = new ControllerCountryCache(lastfm, vertx);
        /**
         * Mount controllers.
         */
        router.route("/*").handler(StaticHandler.create("public"));
        router.route(HttpMethod.GET, "/handlebars").handler(ctrHbs::toptracksHandler);
        router.route(HttpMethod.GET, "/htmlflow").handler(ctrHfl::toptracksHandler);
        router.mountSubRouter("/clearcache", ctrCache.router());
        /**
         * Create and run HTTP server.
         */
        String port = System.getProperty("server.port", "3000");
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(Integer.parseInt(port));
    }

}

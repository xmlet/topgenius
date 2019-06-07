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
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.htmlflow.samples.topgenius.controllers.ControllerHandlebars;
import org.htmlflow.samples.topgenius.controllers.ControllerHtmlFlow;
import org.htmlflow.samples.topgenius.controllers.ControllerLastfmMock;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class WebApp {
    private static final String APP_PROPERTIES_PATH = "application.properties";
    private static final String ENV_PRODUCTION = "production";

    public static void main(String[] args) throws Exception {
        /**
         * Setup Vertex and router
         */
        final String ENV = readAppProperty("topgenius.env"); // production || mock
        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);
        /**
         * Setup web controller.
         */
        LastfmWebApi lastfm = ENV.equals(ENV_PRODUCTION)
                                ? new LastfmWebApi()
                                : new LastfmWebApiMock();
        ControllerHandlebars ctrHbs = new ControllerHandlebars(lastfm, vertx);
        ControllerHtmlFlow ctrHfl = new ControllerHtmlFlow(lastfm);
        /**
         * Mount controllers.
         */
        router.route("/*").handler(StaticHandler.create("public"));
        router.route("/handlebars").handler(ctrHbs::toptracksHandler);
        router.route("/htmlflow").handler(ctrHfl::toptracksHandler);
        router.route("/lastfmmock").handler(ControllerLastfmMock::geographicTopTracks);
        router.route("/env").handler((ctx) -> { ctx
                .response()
                .putHeader("content-type", "application/json")
                .end(String.format("{ \"env\": \"%s\"}", ENV));
        });
        /**
         * Create and run HTTP server.
         */
        String port = System.getProperty("server.port", "3000");
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(Integer.parseInt(port));
    }

    private static String readAppProperty(String key) throws IOException {
        try(InputStream in = WebApp.class.getClassLoader().getResourceAsStream(APP_PROPERTIES_PATH)){
            Properties props = new Properties();
            props.load(in);
            String env = props.getProperty(key);
            System.out.println(env);
            return env;
        }
    }
}

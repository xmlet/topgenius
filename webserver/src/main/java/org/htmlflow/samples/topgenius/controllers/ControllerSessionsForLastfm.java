package org.htmlflow.samples.topgenius.controllers;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.htmlflow.samples.topgenius.LastfmWebApi;
import org.htmlflow.samples.topgenius.LastfmWebApiSessions;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Consumer;

import static io.vertx.ext.web.Cookie.cookie;
import static java.lang.System.currentTimeMillis;

public class ControllerSessionsForLastfm implements LastfmWebApiSessions {

    static final String SESSION_KEY = "topgeniusSession";
    static final int MAX_ENTRIES = 100;
    static final int WATERMARK = 50;

    /**
     * Key is an UUID.
     */
    final Map<String, LastfmWebApi> sessions = new HashMap<>();
    /**
     * Key is insertion time in milis and value is the UUID
     */
    final SortedMap<Long, String> sessionsLive = new TreeMap<>();

    private Consumer<HttpResponse<String>> onResponseCons;
    private Consumer<String> onRequestCons;

    final Router router;

    /**
     * It will register routes /init and /clear to initialize and clear the per user cache.
     * Unfortunately we are not taking advantage of PUT and DELETE verbs because these routes
     * are requested by HTML form submission without AJAX, which does not supports those methods.
     */
    public ControllerSessionsForLastfm(Vertx vertx) {
        this.router = Router.router(vertx);
        router.route(HttpMethod.POST, "/init").handler(this::initHandler);
        router.route().handler(BodyHandler.create());
        router.route(HttpMethod.POST, "/clear/:from").handler(this::clearcacheHandler);
    }

    public Router router() {
        return router;
    }

    /**
     * Registers a Consumer handler that is invoked whenever an HTTP
     * requeste is completed.
     * The consumer is invoked on response completion.
     */
    public synchronized void onResponse(Consumer<HttpResponse<String>> cons) {
        this.onResponseCons = cons;
    }

    /**
     * Registers a Consumer handler that is invoked whenever an HTTP
     * requeste is performed.
     */
    public synchronized void onRequest(Consumer<String> cons) {
        this.onRequestCons = cons;
    }

    /**
     * From interface LastfmWebApiSessions.
     * If there is a topgenius session cookie then it will try to get it from
     * sessions Map.
     * Otherwise it will return a new freshly created LastfmWebApi instance.
     */
    public synchronized LastfmWebApi from(RoutingContext ctx) {
        Cookie cookie= ctx.getCookie(SESSION_KEY);
        if(cookie != null)
            return sessions.get(cookie.getValue());
        else
            return create();
    }

    /**
     * From interface LastfmWebApiSessions.
     * Generates a new UUID session and update RoutingContext
     * with corresponding TopGenius cookie.
     */
    public void newSession(RoutingContext ctx) {
        String uuid = UUID.randomUUID().toString();
        ctx.addCookie(setTopgeniusCookie(cookie(SESSION_KEY, uuid)));
        insertSession(uuid);
    }

    private synchronized void insertSession(String uuid) {
        if(sessions.size() > MAX_ENTRIES) {
            while(sessions.size() > WATERMARK) {
                Long time = sessionsLive.firstKey();
                String firstUuid = sessionsLive.remove(time);
                sessions.remove(firstUuid);
            }
        }
        sessions.put(uuid, create());
        sessionsLive.put(currentTimeMillis(), uuid);
    }

    /**
     * From interface LastfmWebApiSessions.
     * Returns true if there is a topgenius session cookie.
     */
    @Override
    public synchronized boolean hasSession(RoutingContext ctx) {
        Cookie cookie = ctx.getCookie(SESSION_KEY);
        if(cookie == null)
            return false;
        if(sessions.containsKey(cookie.getValue())) {
            setTopgeniusCookie(cookie);
            return true;
        }
        ctx.removeCookie(cookie.getName());
        return false;
    }


    public void initHandler(RoutingContext ctx) {
        if(!hasSession(ctx))
            newSession(ctx);
        String referer = ctx
            .request()
            .getHeader(HttpHeaders.REFERER)
            .split("\\?")[0];
        ctx
            .response()
            .putHeader("location", referer)
            .setStatusCode(303)
            .end();
    }


    public synchronized void clearcacheHandler(RoutingContext ctx) {
        HttpServerRequest req = ctx.request();
        HttpServerResponse resp = ctx.response();
        /**
         * Parse query-string parameters
         */
        String country = req.getParam("country");
        String from = req.getParam("from");
        Cookie cookie= ctx.getCookie(SESSION_KEY);
        if(cookie != null && country != null && !country.isBlank()){
            LastfmWebApi lastfm = sessions.get(cookie.getValue());
            if(lastfm != null)
                lastfm.clearCacheAndCancelRequests(country);
        }
        resp.putHeader("location", "/" + from).setStatusCode(303).end();
    }

    private Cookie setTopgeniusCookie(Cookie cookie) {
        cookie.setPath("/");
        cookie.setMaxAge(60*10); // 10 minutes
        return cookie;
    }

    /**
     * Creates a new LastfmWebApi instance and registers the onResponse consumer
     * if exists one.
     */
    private LastfmWebApi create() {
        LastfmWebApi api = new LastfmWebApi();
        if(onResponseCons != null)
            api.onResponse(onResponseCons);
        if(onRequestCons!= null)
            api.onRequest(onRequestCons);
        return api;
    }
}

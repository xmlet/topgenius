package org.htmlflow.samples.topgenius;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Locale;
import io.vertx.ext.web.ParsedHeaderValues;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MockRountingContext implements RoutingContext {
    private final MockHttpServerRequest req = new MockHttpServerRequest();
    private CompletableFuture<String> complete = new CompletableFuture<>();
    Map<String, Cookie> cookies = new HashMap<>();

    public CompletableFuture<String> complete() {
        return complete;
    }

    @Override
    public HttpServerRequest request() {
        return req;
    }

    @Override
    public HttpServerResponse response() {
        complete = new CompletableFuture<>();
        return new MockHttpServerResponse(complete);
    }

    @Override
    public void next() {

    }

    @Override
    public void fail(int statusCode) {

    }

    @Override
    public void fail(Throwable throwable) {

    }

    @Override
    public void fail(int statusCode, Throwable throwable) {

    }

    @Override
    public RoutingContext put(String key, Object obj) {
        return null;
    }

    @Override
    public <T> T get(String key) {
        return null;
    }

    @Override
    public <T> T remove(String key) {
        return null;
    }

    @Override
    public Map<String, Object> data() {
        return null;
    }

    @Override
    public Vertx vertx() {
        return null;
    }

    @Override
    public String mountPoint() {
        return null;
    }

    @Override
    public Route currentRoute() {
        return null;
    }

    @Override
    public String normalisedPath() {
        return null;
    }

    @Override
    public Cookie getCookie(String name) {
        return cookies.get(name);
    }

    @Override
    public RoutingContext addCookie(Cookie cookie) {
        cookies.put(cookie.getName(), cookie);
        return this;
    }

    @Override
    public Cookie removeCookie(String name, boolean invalidate) {
        return null;
    }

    @Override
    public int cookieCount() {
        return 0;
    }

    @Override
    public Set<Cookie> cookies() {
        return null;
    }

    @Override
    public String getBodyAsString() {
        return null;
    }

    @Override
    public String getBodyAsString(String encoding) {
        return null;
    }

    @Override
    public JsonObject getBodyAsJson() {
        return null;
    }

    @Override
    public JsonArray getBodyAsJsonArray() {
        return null;
    }

    @Override
    public Buffer getBody() {
        return null;
    }

    @Override
    public Set<FileUpload> fileUploads() {
        return null;
    }

    @Override
    public Session session() {
        return null;
    }

    @Override
    public User user() {
        return null;
    }

    @Override
    public Throwable failure() {
        return null;
    }

    @Override
    public int statusCode() {
        return 0;
    }

    @Override
    public String getAcceptableContentType() {
        return null;
    }

    @Override
    public ParsedHeaderValues parsedHeaders() {
        return null;
    }

    @Override
    public int addHeadersEndHandler(Handler<Void> handler) {
        return 0;
    }

    @Override
    public boolean removeHeadersEndHandler(int handlerID) {
        return false;
    }

    @Override
    public int addBodyEndHandler(Handler<Void> handler) {
        return 0;
    }

    @Override
    public boolean removeBodyEndHandler(int handlerID) {
        return false;
    }

    @Override
    public boolean failed() {
        return false;
    }

    @Override
    public void setBody(Buffer body) {

    }

    @Override
    public void setSession(Session session) {

    }

    @Override
    public void setUser(User user) {

    }

    @Override
    public void clearUser() {

    }

    @Override
    public void setAcceptableContentType(String contentType) {

    }

    @Override
    public void reroute(HttpMethod method, String path) {

    }

    @Override
    public List<Locale> acceptableLocales() {
        return null;
    }

    @Override
    public Map<String, String> pathParams() {
        return null;
    }

    @Override
    public String pathParam(String name) {
        return null;
    }

    @Override
    public MultiMap queryParams() {
        return null;
    }

    @Override
    public List<String> queryParam(String query) {
        return null;
    }

    public MockRountingContext add(String key, String val) {
        req.add(key, val);
        return this;
    }

    public String join() {
        return complete.join();
    }

    public MockRountingContext addHeader(String key, String val) {
        req.headers.put(key, val);
        return this;
    }
}

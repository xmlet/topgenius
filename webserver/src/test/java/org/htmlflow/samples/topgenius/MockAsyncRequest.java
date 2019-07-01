package org.htmlflow.samples.topgenius;

import org.javaync.io.AsyncFiles;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class MockAsyncRequest implements AsyncRequest {

    @Override
    public CompletableFuture<HttpResponse<String>> get(String url) {
        Map<String, String> args = parseQuery(url);
        String country = args.get("country");
        String page = args.get("page");
        if(country == null) throw new IllegalArgumentException("Missing country on query string!");
        if(page == null) throw new IllegalArgumentException("Missing country on query string!");
        String path = format("lastfm-toptracks/lastfm-%s-page-%s.json", country, page);
        URL file = MockAsyncRequest.class.getClassLoader().getResource(path);
        if(file == null) throw new IllegalArgumentException("No mock file found for " + path);
        try {
            URI uri = file.toURI();
            return AsyncFiles
                .readAll(Paths.get(uri))
                .thenApply(body -> new MockHttpResponse(body, uri));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    Map<String, String> parseQuery(String url) {
        Pattern pt = Pattern.compile("[\\?&]([^&=]+)=([^&=]+)");
        Matcher m = pt.matcher(url);
        Map<String, String> res = new HashMap<>();
        while(m.find())
            res.put(m.group(1), m.group(2));
        return res;
    }
}

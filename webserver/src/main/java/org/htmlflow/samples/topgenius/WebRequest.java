package org.htmlflow.samples.topgenius;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;

public class WebRequest implements AsyncRequest {
    final HttpClient httpClient = HttpClient.newHttpClient();
    final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();

    HttpRequest request(String url) {
        return requestBuilder.uri(URI.create(url)).build();
    }

    @Override
    public CompletableFuture<HttpResponse<String>> get(String path) {
        return httpClient
            .sendAsync(request(path), BodyHandlers.ofString());
    }
}

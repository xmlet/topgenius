package org.htmlflow.samples.topgenius;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public interface AsyncRequest {
    CompletableFuture<HttpResponse<String>> get(String path);
}

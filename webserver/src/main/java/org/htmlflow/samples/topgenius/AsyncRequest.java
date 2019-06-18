package org.htmlflow.samples.topgenius;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public interface AsyncRequest {
    CompletableFuture<HttpResponse<InputStream>> get(String path);
}
